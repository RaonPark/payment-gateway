package com.example.springcloud.payment.enums

enum class TransactionStatus {
    REQUESTED,
    APPROVED,
    FAILED,
    CANCELLED,
    REFUNDED;
}