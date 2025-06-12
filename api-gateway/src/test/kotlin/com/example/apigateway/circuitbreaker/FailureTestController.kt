package com.example.apigateway.circuitbreaker

import org.apache.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.concurrent.atomic.AtomicBoolean

@Controller
class FailureTestController {
    private val isFailure = AtomicBoolean(false)

    @PostMapping("/test/toggle-failure")
    fun toggleFailure(): Mono<ResponseEntity<String>> {
        isFailure.set(!isFailure.get())
        return ResponseEntity.ok("Failure mode is now: ${isFailure.get()}").toMono()
    }

    @GetMapping("/test/circuit")
    fun circuitTest(): Mono<ResponseEntity<String>> {
        if(isFailure.get()) {
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .body("Simulate Failure from payment-service").toMono()
        }

        return ResponseEntity.ok().body("Success from payment-service").toMono()
    }
}