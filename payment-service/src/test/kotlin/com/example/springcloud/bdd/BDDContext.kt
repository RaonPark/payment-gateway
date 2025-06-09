package com.example.springcloud.bdd

interface GivenScope
interface WhenScope
interface ThenScope

object BDDSyntax {
    fun given(description: String, invoker: GivenScope.() -> Unit) {
        println("Given: $description")
        val givenScope = object: GivenScope {}
        givenScope.invoker()
    }

    fun GivenScope.`when`(description: String, invoker: WhenScope.() -> Unit) {
        println("When: $description")
        val whenScope = object: WhenScope {}
        whenScope.invoker()
    }

    fun WhenScope.then(description: String, invoker: ThenScope.() -> Unit) {
        println("Then: $description")
        val thenScope = object: ThenScope {}
        thenScope.invoker()
    }
}