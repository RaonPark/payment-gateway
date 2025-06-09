package com.example.springcloud.payment.dto

import java.math.BigDecimal

data class PaymentRequest(
    val merchantId: String,
    val amount: BigDecimal,
    val currency: String,
    val status: String,
    val method: String,
)