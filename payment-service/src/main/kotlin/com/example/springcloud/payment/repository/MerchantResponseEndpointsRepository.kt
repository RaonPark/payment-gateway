package com.example.springcloud.payment.repository

import com.example.springcloud.payment.entity.mongo.MerchantResponseEndpoints
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface MerchantResponseEndpointsRepository: ReactiveMongoRepository<MerchantResponseEndpoints, Long> {
    fun findByMerchantId(merchantId: String): Mono<MerchantResponseEndpoints>
}