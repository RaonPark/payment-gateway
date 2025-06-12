package com.example.apigateway.config

import org.springframework.cloud.gateway.filter.factory.SpringCloudCircuitBreakerResilience4JFilterFactory
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.filters
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URI

@Configuration
class RouterConfig (
    private val resilience4JFilterFactory: SpringCloudCircuitBreakerResilience4JFilterFactory
) {
//    @Bean
//    fun routeLocator(builder: RouteLocatorBuilder): RouteLocator {
//        return builder.routes {
//            route("PAYMENT-SERVICE") {
//                uri("lb://PAYMENT-SERVICE")
//                path("/payment-service/**")
//                filters {
//                    filter(
//                        resilience4JFilterFactory.apply {
//                            it.name = "payment-service-resilience"
//                            it.fallbackUri = URI.create("forward:/fallback")
//                        }
//                    )
//                }
//            }
//        }
//    }
}