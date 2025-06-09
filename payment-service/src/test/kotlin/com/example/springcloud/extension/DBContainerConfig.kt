package com.example.springcloud.extension

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.DynamicPropertyRegistrar
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait

@TestConfiguration(proxyBeanMethods = false)
class DBContainerConfig {
    companion object {
        val postgreNetwork = Network.newNetwork()
    }
    @Bean
    fun postgresContainer(): PostgreSQLContainer<Nothing> {
        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:17.5")
        postgresContainer.apply {
            withUsername("root")
            withPassword("1234")
            withDatabaseName("PaymentDB")
            withExposedPorts(5432)
            withNetwork(postgreNetwork)
            withNetworkAliases("postgres")
            withReuse(false)
        }

        return postgresContainer
    }

    @Bean
    fun mongoDBContainer(): GenericContainer<Nothing> {
        val mongoDBContainer = GenericContainer<Nothing>("mongo:8.0")
        mongoDBContainer.apply {
            withExposedPorts(27017)
            withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
            withEnv("MONGO_INITDB_ROOT_PASSWORD", "1234")
            withEnv("MONGO_INITDB_DATABASE", "PaymentDB")
            withNetwork(postgreNetwork)
            waitingFor(Wait.forHttp("/subjects").forStatusCode(200))
        }

        return mongoDBContainer
    }

    @Bean
    fun redisContainer():

    @Bean
    fun dbProperties(mongoDBContainer: GenericContainer<Nothing>, postgresContainer: PostgreSQLContainer<Nothing>): DynamicPropertyRegistrar {
        return DynamicPropertyRegistrar { registry ->
            // mongoDB
            registry.add("spring.data.mongodb.uri") { "mongodb://root:1234@${mongoDBContainer.host}:${mongoDBContainer.firstMappedPort}/PaymentDB?authSource=admin" }

            // PostgreSQL
            registry.add("spring.datasource.driver-class-name") { postgresContainer.driverClassName }
            registry.add("spring.datasource.url") { postgresContainer.jdbcUrl }
            registry.add("spring.datasource.username") { postgresContainer.username }
            registry.add("spring.datasource.password") { postgresContainer.password }

            registry.add("spring.jpa.hibernate.ddl-auto") { "create" }

            registry.add("spring.flyway.enabled") { "false" }
        }
    }
}