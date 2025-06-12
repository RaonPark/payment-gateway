package com.example.apigateway

import com.redis.testcontainers.RedisContainer
import dasniko.testcontainers.keycloak.KeycloakContainer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.springframework.test.context.DynamicPropertyRegistrar
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
    companion object {
        val testContainerNetwork = Network.newNetwork()
        val log = KotlinLogging.logger {}
    }

    @Bean
    @ServiceConnection
    fun kafkaContainer(): ConfluentKafkaContainer {
        return ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0")).apply {
            withNetwork(testContainerNetwork)
            withListener("kafka1:19092")
        }
    }

    @Bean
    @ServiceConnection
    fun mongoDbContainer(): MongoDBContainer {
        return MongoDBContainer(DockerImageName.parse("mongo:8.0"))
    }

    @Bean(name = ["postgresContainer"])
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer<Nothing> {
        return PostgreSQLContainer<Nothing>(DockerImageName.parse("postgres:17.5")).apply {
            withNetwork(testContainerNetwork)
            withExposedPorts(5432)
            withUsername("root")
            withPassword("1234")
            withDatabaseName("ApiGateway")
            withNetworkAliases("postgres")
            withReuse(false)
        }
    }

    @Bean
    @ServiceConnection
    fun redisContainer(): RedisContainer {
        return RedisContainer("redis:8.0").apply {
            withExposedPorts(6379)
            withReuse(false)
            withNetwork(testContainerNetwork)
        }
    }

    @Bean
    @DependsOn("postgresContainer")
    fun keycloakContainer(): KeycloakContainer {
        val keycloak = KeycloakContainer("quay.io/keycloak/keycloak:26.2.4").apply {
            withRealmImportFile("/realm-export.json")
            withAdminUsername("admin")
            withAdminPassword("admin")
            withEnv("KC_DB", "postgres")
            withEnv("KC_DB_URL", "jdbc:postgresql://postgres:5432/ApiGateway")
            withEnv("KC_DB_USERNAME", "root")
            withEnv("KC_DB_PASSWORD", "1234")
            withNetwork(testContainerNetwork)
        }

        return keycloak
    }

    @Bean
    fun setProperties(postgresContainer: PostgreSQLContainer<Nothing>): DynamicPropertyRegistrar {
        log.info { "PostgreSQLContainer: ${postgresContainer.jdbcUrl}" }
        return DynamicPropertyRegistrar {

        }
    }
}
