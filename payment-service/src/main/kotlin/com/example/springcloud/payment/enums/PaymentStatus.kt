package com.example.springcloud.payment.enums

enum class PaymentStatus(val value: String) {
    REQUESTED("REQUESTED"),
    APPROVED("APPROVED"),
    FAILED("FAILED"),
    CANCELLED("CANCELLED"),
    REFUNDED("REFUNDED");

    companion object {
        fun findStatus(value: String): PaymentStatus {
            return PaymentStatus.entries.first { it.value == value }
        }
    }
}