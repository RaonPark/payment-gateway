package com.example.springcloud.payment.controller

import com.example.springcloud.payment.dto.PaymentIntermediateResponse
import com.example.springcloud.payment.dto.PaymentRequest
import com.example.springcloud.payment.service.PaymentService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Controller
class PaymentController(
    private val discoveryClient: DiscoveryClient,
    private val webClientBuilder: WebClient.Builder,
    private val paymentService: PaymentService
) {
    companion object {
        val log = KotlinLogging.logger {}
    }

    @GetMapping("/orders")
    fun getOrders(): Mono<ResponseEntity<String>> {
        return ResponseEntity.ok().body("hello world").toMono()
    }

    @PostMapping("/payment")
    @ResponseBody
    suspend fun createPayment(@RequestBody paymentRequest: PaymentRequest, @RequestHeader("Authorization") jwt: String): Mono<ResponseEntity<PaymentIntermediateResponse>> {
        return ResponseEntity.ok()
            .body(paymentService.insertPayment(paymentRequest))
            .toMono()
    }

    @GetMapping("/helloToken")
    @ResponseStatus(HttpStatus.CREATED)
    fun helloToken(@RequestHeader(value = "Authorization") bearerToken: String): Mono<ResponseEntity<String>> {
        log.info { "jwt token from keycloak : $bearerToken" }
        return ResponseEntity.status(HttpStatus.CREATED).body("hello world").toMono()
    }

    @PostMapping("/refund")
    @ResponseBody
    fun refund() {

    }
}