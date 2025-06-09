package com.example.springcloud.payment.entity.postgre

import com.example.springcloud.payment.audit.BaseTimeEntity
import com.example.springcloud.payment.enums.TransactionStatus
import jakarta.persistence.*
import org.springframework.data.domain.Persistable
import java.util.*

@Entity
@Table(name = "card_transaction")
class CardTransaction(
    @Id
    private val id: Long? = null,

    @ManyToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val payment: Payment,

    @Column(nullable = false)
    val cardNumberMasked: String,

    @Column(nullable = false)
    val cardCompany: String,

    @Column(nullable = false)
    val approveNo: String,

    @Column(nullable = false)
    val installmentMonths: Int,

    @Column(nullable = false)
    val status: TransactionStatus,
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