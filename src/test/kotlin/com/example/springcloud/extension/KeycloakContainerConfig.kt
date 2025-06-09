package com.example.springcloud.extension

import com.example.springcloud.extension.DBContainerConfig.Companion.postgreNetwork
import dasniko.testcontainers.keycloak.KeycloakContainer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistrar
import org.testcontainers.junit.jupiter.Testcontainers

@TestConfiguration(proxyBeanMethods = false)
@Import(DBContainerConfig::class)
@Testcontainers
class KeycloakContainerConfig {

    @Bean
    @DependsOn("postgresContainer")
    fun keycloakContainer(): KeycloakContainer {
        val keycloak = KeycloakContainer("quay.io/keycloak/keycloak:26.2.4").apply {
            withRealmImportFile("/realm-export.json")
            withAdminUsername("admin")
            withAdminPassword("admin")
            withEnv("KC_DB", "postgres")
            withEnv("KC_DB_URL", "jdbc:postgresql://postgres:5432/PaymentDB")
            withEnv("KC_DB_USERNAME", "root")
            withEnv("KC_DB_PASSWORD", "1234")
            withNetwork(postgreNetwork)
        }

        return keycloak
    }

    @Bean
    fun keycloakContainerProperties(keycloak: KeycloakContainer): DynamicPropertyRegistrar {
        return DynamicPropertyRegistrar { registry ->
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri") { "${keycloak.authServerUrl}/realms/oauth2" }
        }
    }
}