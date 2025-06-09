package com.example.springcloud.payment.dto

data class PaymentResponse(
    val merchantId: String,
    val status: String,
    val paidAt: String,
)