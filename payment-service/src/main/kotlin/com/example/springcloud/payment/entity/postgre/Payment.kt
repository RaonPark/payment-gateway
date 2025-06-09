package com.example.springcloud.payment.entity.postgre

import com.example.springcloud.payment.audit.BaseTimeEntity
import com.example.springcloud.payment.enums.PaymentMethod
import com.example.springcloud.payment.enums.PaymentStatus
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.domain.Persistable
import java.math.BigDecimal
import kotlin.jvm.Transient

@Entity
@Table(name = "payment")
class Payment @PersistenceCreator constructor(
    @Id
    private val id: Long?,

    @Column(nullable = false)
    val merchantId: String,

    @Column(nullable = false)
    val amount: BigDecimal,

    @Column(nullable = false)
    val currency: String,

    @Column(nullable = false)
    var status: PaymentStatus,

    @Column(nullable = false)
    val method: PaymentMethod,

    @Type(JsonType::class)
    @Column(nullable = false, columnDefinition = "jsonb")
    val userData: UserData
): Persistable<Long>, BaseTimeEntity() {
    override fun getId(): Long? = id

    @Transient
    private var _isNew = true

    override fun isNew(): Boolean = _isNew

    @PostPersist
    @PostLoad
    protected fun load() {
        _isNew = false
    }

    override fun toString(): String {
        return "merchantId: $merchantId, amount: $amount, currency: $currency, status: $status, method: $method, userData: $userData"
    }
}