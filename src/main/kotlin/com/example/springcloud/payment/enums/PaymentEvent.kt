package com.example.springcloud.payment.enums

enum class PaymentEvent {
    REQUESTED,
    APPROVED,
    CANCEL_REQUEST,
    CANCELLED,
    REFUND_REQUESTED,
    REFUNDED,
    FAILED_PG_CALL,
    PG_CALLBACK
}