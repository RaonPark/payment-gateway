package com.example.springcloud.payment.config

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate

@Configuration
class ReactiveMongoConfig {
    @Value("\${spring.data.mongodb.uri}")
    private lateinit var mongoDBUri: String

    @Bean
    fun reactiveMongoTemplate(mongoClient: MongoClient): ReactiveMongoTemplate {
        return ReactiveMongoTemplate(mongoClient, "PaymentDB")
    }
}