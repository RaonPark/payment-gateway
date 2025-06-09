package com.example.springcloud.payment.service

import com.example.springcloud.payment.dto.PaymentFailedFromMerchant
import com.example.springcloud.payment.dto.PaymentResponse
import com.example.springcloud.payment.dto.PaymentSuccessFromMerchant
import com.example.springcloud.payment.enums.PaymentStatus
import com.example.springcloud.payment.repository.PaymentRepository
import com.raonpark.avro.PaymentCompleted
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange

@Service
class WebhookService(
    private val webClientBuilder: WebClient.Builder,
    private val paymentRepository: PaymentRepository,
) {
    companion object {
        val log = KotlinLogging.logger { }
    }
    @KafkaListener(topics = ["payment-completed"],
        groupId = "payment_groups",
        containerFactory = "paymentCompletedKafkaListenerContainerFactory",
    )
    suspend fun paymentResponse(paymentCompleted: PaymentCompleted, ack: Acknowledgment) {
        log.info { "paymentResponse incoming: $paymentCompleted" }

        val paymentResponse = PaymentResponse(
            merchantId = paymentCompleted.merchantId,
            status = paymentCompleted.status,
            paidAt = paymentCompleted.paidAt,
        )

        val pgResult = webClientBuilder.build()
            .post()
            .uri(paymentCompleted.responseURI)
            .body(BodyInserters.fromValue(paymentResponse))
            .awaitExchange { clientResponse ->
                if (clientResponse.statusCode().is2xxSuccessful) {
                    log.info { "Payment Request Is Completed" }
                    clientResponse.awaitBody<PaymentSuccessFromMerchant>()
                } else {
                    paymentRepository
                    log.info { "Payment Request Is Failed" }
                    clientResponse.awaitBody<PaymentFailedFromMerchant>()
                }
            }

        log.info { "Webhook for Payment Response : $pgResult" }

        val paymentInfo = withContext(Dispatchers.IO) {
            paymentRepository.findPaymentById(paymentCompleted.paymentId)
        }

        if (pgResult is PaymentSuccessFromMerchant) {
            paymentInfo.status = PaymentStatus.APPROVED
        } else {
            paymentInfo.status = PaymentStatus.FAILED
        }

        ack.acknowledge()
    }
}