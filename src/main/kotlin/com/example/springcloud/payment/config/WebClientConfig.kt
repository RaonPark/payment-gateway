package com.example.springcloud.payment.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {
    @Bean
    fun webClientBuilder(): WebClient.Builder {
        return WebClient.builder().baseUrl("http://localhost:8081")
    }

    @Bean(name = ["kakaoWebClientBuilder"])
    fun kakaoWebClientBuilder(): WebClient.Builder {
        val builder = WebClient.builder()
        builder.baseUrl("http://localhost:8081")

        return builder
    }
}