package com.example.springcloud.payment.entity.mongo

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "merchant_response_endpoints")
class MerchantResponseEndpoints(
    @Id
    private val id: String? = null,

    val merchantId: String,
    val responseEndpoint: String
) {

}