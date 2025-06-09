package com.example.springcloud.payment.entity.postgre

import com.example.springcloud.payment.audit.BaseTimeEntity
import com.example.springcloud.payment.enums.PaymentEvent
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import org.springframework.data.domain.Persistable

@Entity
@Table(name = "payment_logs")
class PaymentLogs(
    @Id
    private val id: Long? = null,

    @ManyToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val payment: Payment,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val paymentEvent: PaymentEvent,

    @Type(JsonType::class)
    @Column(columnDefinition = "jsonb")
    val eventData: EventData,

    @Column(nullable = false)
    val description: String
): Persistable<Long>, BaseTimeEntity() {
    override fun getId(): Long? = id

    private var _isNew = true

    override fun isNew(): Boolean = _isNew

    @PostPersist
    @PostLoad
    protected fun load() {
        _isNew = false
    }
}