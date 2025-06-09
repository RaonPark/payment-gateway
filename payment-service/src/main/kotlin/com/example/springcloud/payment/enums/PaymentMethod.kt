package com.example.springcloud.payment.enums

enum class PaymentMethod(val value: String, val requestURI: String) {
    CARD("CARD", ""),
    VIRTUAL_ACCOUNT("VIRTUAL_ACCOUNT", ""),
    BANK_TRANSFER("BANK_TRANSFER", ""),
    TOSSPAY("TOSSPAY", "/tosspay/request"),
    KAKAOPAY("KAKAOPAY", "/kakaopay/request"),;

    companion object {
        fun findMethod(value: String): PaymentMethod {
            return entries.find { it.value == value }!!
        }
    }
}