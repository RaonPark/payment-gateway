package com.example.springcloud.extension

import com.example.springcloud.bdd.BDDSyntax
import com.example.springcloud.bdd.BDDSyntax.given
import com.example.springcloud.bdd.BDDSyntax.then
import com.example.springcloud.bdd.BDDSyntax.`when`
import com.example.springcloud.payment.dto.PaymentIntermediateResponse
import com.example.springcloud.payment.dto.PaymentRequest
import com.example.springcloud.payment.entity.mongo.MerchantResponseEndpoints
import com.example.springcloud.payment.enums.PaymentStatus
import com.example.springcloud.payment.repository.MerchantResponseEndpointsRepository
import com.example.springcloud.payment.service.WebhookService
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.awaitility.Awaitility
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.reactive.function.BodyInserters
import org.testcontainers.junit.jupiter.Testcontainers
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock
import org.wiremock.spring.InjectWireMock
import reactor.kotlin.core.publisher.toMono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@Import(
    KeycloakContainerConfig::class,
    DBContainerConfig::class,
    KafkaContainerConfig::class,
)
@EnableWireMock(
    ConfigureWireMock(
        name = "localhost",
        port = 8081
    ))
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient(timeout = "30000")
class PaymentTest {

    @Autowired
    private lateinit var webhookService: WebhookService

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var oAuth2ResourceServerProperties: OAuth2ResourceServerProperties

//    @MockitoBean
//    private lateinit var discoveryClient: DiscoveryClient
//
//    @MockitoBean
//    private lateinit var webClientBuilder: WebClient.Builder
//
//    @MockitoBean
//    private lateinit var paymentService: PaymentService

    @InjectWireMock("localhost")
    lateinit var wireMock: WireMockServer

    @MockitoBean
    lateinit var merchantResponseEndpointsRepository: MerchantResponseEndpointsRepository

    @BeforeEach
    fun setUp() {
        wireMock.start()
        webTestClient.mutate().responseTimeout(Duration.ofSeconds(30))

        wireMock.stubFor(
            post("/api/orderPaymentGateway").willReturn(
                okJson("{\"message\": \"Payment Is Completed!\"}")
            )
        )

        wireMock.stubFor(
            post("/kakaopay/request").willReturn(
                okJson(
                    "{\"message\": \"kakaopay completed\", \"paidAt\": \"${
                        Instant.now().atZone(ZoneId.systemDefault()).toInstant()
                    }\"}"
                )
            )
        )
    }

    @Test
    fun `should return PaymentIntermediateResponse when inserting payment`() {
        // GIVEN
        given("given payment information") {
            val token = getToken()

            Mockito.`when`(merchantResponseEndpointsRepository.findByMerchantId("test_merchant"))
                .thenReturn(
                    MerchantResponseEndpoints(
                        id = "1",
                        merchantId = "test_merchant",
                        responseEndpoint = "/api/orderPaymentGateway"
                    ).toMono()
                )

            val paymentRequest = PaymentRequest(
                merchantId = "test_merchant",
                currency = "KRW",
                amount = BigDecimal("100000.00"),
                method = "KAKAOPAY",
                status = "REQUESTED"
            )

            `when`("call post(\"payment\")") {
                val response = webTestClient.post()
                    .uri("/payment")
                    .header("Authorization", "Bearer $token")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(BodyInserters.fromValue(paymentRequest))
                    .exchange()
                    .expectStatus().isOk
                    .returnResult(PaymentIntermediateResponse::class.java)
                    .responseBody
                    .log()

                then("return REQUESTED of paymentStatus which is intermediate result") {
                    StepVerifier.create(response)
                        .expectNext(PaymentIntermediateResponse(status = PaymentStatus.REQUESTED.value))
                        .verifyComplete()
                    Awaitility.await().atMost(5, TimeUnit.SECONDS)
                        .until {
                            wireMock.findAll(postRequestedFor(urlEqualTo("/api/orderPaymentGateway")))
                                .isNotEmpty()
                        }
                }
            }
        }
    }

    @Test
    fun tokenTest() {
        val token = getToken()

        webTestClient.get()
            .uri("/helloToken")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isCreated
            .expectBody(String::class.java)
            .isEqualTo("hello world")
    }

    private fun getToken():String {
        val restClient = RestClient.builder().build()
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val map = LinkedMultiValueMap<String, String>()
        map["grant_type"] = "client_credentials"
        map["client_id"] = "oauth2-client"
        map["client_secret"] = "gg6HBEP6dtrj6tYGQQUtE8NkpWRHQmP3"

        val authServerUrl = "${oAuth2ResourceServerProperties.jwt.issuerUri}/protocol/openid-connect/token"

        val token = restClient.post()
            .uri(authServerUrl)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(map)
            .retrieve()
            .body(KeyCloakToken::class.java)

        return token?.accessToken ?: "null token"
    }

    data class KeyCloakToken(@JsonProperty("access_token") val accessToken: String)

    companion object {
        @JvmStatic
        @AfterAll
        fun tearDown(): Unit {


        }
    }
}