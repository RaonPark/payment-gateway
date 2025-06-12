package com.example.apigateway.supports

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient

class TokenProviderForTest {
    private val oAuth2ResourceServerProperties = OAuth2ResourceServerProperties()

    fun getToken():String {
        val restClient = RestClient.builder().build()
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val map = LinkedMultiValueMap<String, String>()
        map["grant_type"] = "client_credentials"
        map["client_id"] = "oauth2-client"
        map["client_secret"] = "gg6HBEP6dtrj6tYGQQUtE8NkpWRHQmP3"

        val authServerUrl = "${oAuth2ResourceServerProperties.jwt.issuerUri}/protocol/openid-connect/token"

        val token = restClient.post()
            .uri(authServerUrl)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(map)
            .retrieve()
            .body(KeyCloakToken::class.java)

        return token?.accessToken ?: "null token"
    }

    data class KeyCloakToken(@JsonProperty("access_token") val accessToken: String)
}