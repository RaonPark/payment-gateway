package com.example.apigateway.circuitbreaker

import com.example.apigateway.TestcontainersConfiguration
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.*
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.junit.jupiter.Testcontainers
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock
import org.wiremock.spring.InjectWireMock
import reactor.test.StepVerifier

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(
    ConfigureWireMock(
        name = "localhost",
        port = 8082
    )
)
@Import(
    TestcontainersConfiguration::class
)
@Testcontainers
@TestPropertySource(properties = [
    "eureka.client.enabled=false",
    "spring.flyway.enabled=false"
])
@ActiveProfiles("test")
class CircuitBreakerTest {
    @Autowired
    lateinit var webTestClient: WebTestClient

    @InjectWireMock("localhost")
    lateinit var wireMockServer: WireMockServer

    @BeforeEach
    fun setup() {
        wireMockServer.stubFor(get(urlPathEqualTo("/payment-service/test/circuit"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("Success From Mock Server")
            )
        )
    }

    @Test
    fun `should success request when it is normal`() {

        val response = webTestClient.get().uri("/payment-service/test/circuit")
            .exchange()
            .expectStatus().isOk
            .returnResult(String::class.java)
            .responseBody
            .log()

        StepVerifier.create(response)
            .expectNext("Success From Mock Server")
            .verifyComplete()
    }
}