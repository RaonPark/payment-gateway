package com.example.springcloud.payment.service

import com.example.springcloud.payment.dto.ExternalPaymentServiceSuccessResponse
import com.example.springcloud.payment.dto.PaymentIntermediateResponse
import com.example.springcloud.payment.dto.PaymentRequest
import com.example.springcloud.payment.entity.postgre.Payment
import com.example.springcloud.payment.entity.postgre.UserData
import com.example.springcloud.payment.enums.PaymentMethod
import com.example.springcloud.payment.enums.PaymentStatus
import com.example.springcloud.payment.repository.MerchantResponseEndpointsRepository
import com.example.springcloud.payment.repository.PaymentRepository
import com.example.springcloud.payment.support.MachineIdGenerator
import com.example.springcloud.payment.support.SnowflakeIdGenerator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.raonpark.avro.PaymentCompleted
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val webClientBuilder: WebClient.Builder,
    @Qualifier("kakaoWebClientBuilder") private val kakaoWebClientBuilder: WebClient.Builder,
    private val merchantResponseEndpointsRepository: MerchantResponseEndpointsRepository,
    private val paymentCompletedKafkaTemplate: KafkaTemplate<String, PaymentCompleted>,
) {
    val snowflakeIdGenerator = SnowflakeIdGenerator(MachineIdGenerator.machineId())

    private val objectMapper = ObjectMapper()

    companion object {
        private val log = KotlinLogging.logger {}
    }

    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private lateinit var issuerUri: String

    @Transactional
    suspend fun insertPayment(paymentRequest: PaymentRequest): PaymentIntermediateResponse {
        val paymentId = snowflakeIdGenerator.nextId()
        val payment = Payment(
            id = paymentId,
            merchantId = paymentRequest.merchantId,
            amount = paymentRequest.amount,
            currency = paymentRequest.currency,
            status = PaymentStatus.findStatus(paymentRequest.status),
            method = PaymentMethod.findMethod(paymentRequest.method),
            userData = UserData("raonpark", 29, "Seoul")
        )

        log.info { "payment insertion coming! : $payment" }

        val savedPayment = withContext(Dispatchers.IO) {
            paymentRepository.save(payment)
        }

        sendToExternalPaymentService(paymentRequest, paymentId)

        log.info { "PaymentIntermediateResponse: $savedPayment" }

        return PaymentIntermediateResponse(
            status = savedPayment.status.value,
        )
    }

    private suspend fun sendToExternalPaymentService(paymentRequest: PaymentRequest, paymentId: Long) {
        val paymentMethod = PaymentMethod.findMethod(paymentRequest.method)

        val paymentToken = generatePaymentToken()

        val externalPaymentServiceResponse = kakaoWebClientBuilder.build()
            .post()
            .uri(paymentMethod.requestURI)
            .header("Authorization", "Bearer $paymentToken")
            .header("Content-Type", "application/json")
            .body(BodyInserters.fromValue(paymentRequest))
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .awaitBody<ExternalPaymentServiceSuccessResponse>()

        log.info { "external payment service success: $externalPaymentServiceResponse" }

        val paymentCompleted = buildPaymentCompletedAvro(paymentId, paymentRequest, externalPaymentServiceResponse)
        paymentCompletedKafkaTemplate.executeInTransaction {
            it.send("payment-completed", paymentCompleted.responseURI, paymentCompleted)
        }
    }

    private fun generatePaymentToken(): String {
        val restClient = RestClient.builder().build()
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val map = LinkedMultiValueMap<String, String>()
        map["grant_type"] = "client_credentials"
        map["client_id"] = "oauth2-client"
        map["client_secret"] = "gg6HBEP6dtrj6tYGQQUtE8NkpWRHQmP3"

        val authServerUrl = "${issuerUri}/protocol/openid-connect/token"

        val token = restClient.post()
            .uri(authServerUrl)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(map)
            .retrieve()
            .body(KeyCloakToken::class.java)

        return token?.accessToken ?: "null token"
    }

    data class KeyCloakToken(@JsonProperty("access_token") val accessToken: String)

    private suspend fun buildPaymentCompletedAvro(paymentId: Long, paymentRequest: PaymentRequest, externalPaymentServiceSuccessResponse: ExternalPaymentServiceSuccessResponse): PaymentCompleted {
        val endpoint =
            merchantResponseEndpointsRepository.findByMerchantId(paymentRequest.merchantId).awaitSingle()

        val paymentCompleted = PaymentCompleted().apply {
            merchantId = paymentRequest.merchantId
            this.paymentId = paymentId
            status = paymentRequest.status
            paidAt = externalPaymentServiceSuccessResponse.paidAt
            responseURI = endpoint.responseEndpoint
        }

        log.info { "payment completed avro: $paymentCompleted" }

        return paymentCompleted
    }
}