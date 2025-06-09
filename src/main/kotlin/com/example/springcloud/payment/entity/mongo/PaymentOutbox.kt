package com.example.springcloud.payment.entity.mongo

import com.example.springcloud.payment.enums.PaymentStatus
import jakarta.persistence.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "payment_outbox")
data class PaymentOutbox @PersistenceCreator constructor (
    @Id
    val id: Long?,
    val paymentId: Long,
    val paymentStatus: PaymentStatus,
)