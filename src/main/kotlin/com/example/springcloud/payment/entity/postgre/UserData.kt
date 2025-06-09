package com.example.springcloud.payment.entity.postgre

import org.springframework.data.jpa.domain.AbstractPersistable_.id
import java.io.Serializable

class UserData(
    val username: String,
    val age: Int,
    val city: String,
): Serializable {
    override fun toString(): String {
        return "UserData{username='$username', age=$age, city='$city', id=$id}"
    }
}