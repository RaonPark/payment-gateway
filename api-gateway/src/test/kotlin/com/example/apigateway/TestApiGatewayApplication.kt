package com.example.apigateway

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<ApiGatewayApplication>().with(TestcontainersConfiguration::class).run(*args)
}
