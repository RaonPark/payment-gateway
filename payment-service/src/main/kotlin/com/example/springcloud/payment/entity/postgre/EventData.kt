package com.example.springcloud.payment.entity.postgre

import com.example.springcloud.payment.enums.PaymentEvent
import java.io.Serializable

data class EventData(
    val bodyOfPGResponse: String,
    val errorCode: String,
    val paymentEventBefore: PaymentEvent,
    val paymentEventAfter: PaymentEvent,
): Serializable {
    override fun toString(): String {
        return "EventData{bodyOfPGResponse=$bodyOfPGResponse, " +
                "errorCode=$errorCode, " +
                "paymentEventBefore=$paymentEventBefore, " +
                "paymentEventAfter=$paymentEventAfter}"
    }
}