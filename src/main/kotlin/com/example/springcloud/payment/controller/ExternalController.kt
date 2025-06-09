package com.example.springcloud.payment.controller

import com.example.springcloud.payment.dto.ExternalPaymentServiceSuccessResponse
import com.example.springcloud.payment.dto.PaymentRequest
import com.example.springcloud.payment.dto.PaymentSuccessFromMerchant
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.Instant
import java.time.ZoneId

@Controller
class ExternalController {
    @PostMapping("/kakaopay/request")
    fun kakaopayRequest(@RequestBody paymentRequest: PaymentRequest): Mono<ResponseEntity<ExternalPaymentServiceSuccessResponse>> {
        return ResponseEntity.ok().body(
            ExternalPaymentServiceSuccessResponse("payment가 성공했습니다.", Instant.now().atZone(
            ZoneId.of("Asia/Seoul")).toString())
        ).toMono()
    }

    @PostMapping("/api/orderPaymentGateway")
    fun orderPaymentGateway(@RequestBody paymentRequest: PaymentRequest): Mono<ResponseEntity<PaymentSuccessFromMerchant>> {
        return ResponseEntity.ok().body(PaymentSuccessFromMerchant("Payment Success!")).toMono()
    }
}