package com.example.springcloud.payment.dto

data class ExternalPaymentServiceSuccessResponse(
    val message: String,
    val paidAt: String,
)