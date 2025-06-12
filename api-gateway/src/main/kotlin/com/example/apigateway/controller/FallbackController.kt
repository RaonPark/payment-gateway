package com.example.apigateway.controller

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono

@Controller
class FallbackController {
    @GetMapping("/fallback")
    fun fallback(): Mono<ResponseEntity<String>> {
        return Mono.just(ResponseEntity.ok("This is fallback"))
    }
}