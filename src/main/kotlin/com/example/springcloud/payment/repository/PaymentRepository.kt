package com.example.springcloud.payment.repository

import com.example.springcloud.payment.entity.postgre.Payment
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository: JpaRepository<Payment, Long> {
    fun findPaymentByMerchantId(merchantId: String): Payment
    fun findPaymentById(id: Long): Payment
}