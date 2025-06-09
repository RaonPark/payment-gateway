package com.example.springcloud.extension

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.DependsOn
import org.springframework.test.context.DynamicPropertyRegistrar
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.kafka.ConfluentKafkaContainer

@TestConfiguration(proxyBeanMethods = false)
class KafkaContainerConfig {
    companion object {
        val kafkaNetwork = Network.newNetwork()
    }

    @Bean
    @ServiceConnection
    fun kafkaContainer(): ConfluentKafkaContainer {
        val kafkaContainer = ConfluentKafkaContainer("confluentinc/cp-kafka:7.5.0")
            .apply {
                withNetwork(kafkaNetwork)
                withListener("kafka:19092")
            }
        return kafkaContainer
    }

    @Bean
    @DependsOn("kafkaContainer")
    fun schemaRegistryContainer(kafkaContainer: ConfluentKafkaContainer): GenericContainer<Nothing> {
        val schemaRegistryContainer = GenericContainer<Nothing>("confluentinc/cp-schema-registry:7.5.0")
            .apply {
                withExposedPorts(8085)
                withNetwork(kafkaNetwork)
                withNetworkAliases("schema-registry")
                withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
                withEnv("SCHEMA_REGISTRY_CUB_KAFKA_MIN_BROKERS", "1")
                withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:19092")
                withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8085")
                waitingFor(Wait.forHttp("/subjects").forStatusCode(200))
            }

        return schemaRegistryContainer
    }

    @Bean
    fun setKafkaProperties(kafkaContainer: ConfluentKafkaContainer, schemaRegistryContainer: GenericContainer<Nothing>): DynamicPropertyRegistrar {
        return DynamicPropertyRegistrar { registry ->
            registry.add("spring.kafka.bootstrap-servers") { kafkaContainer.bootstrapServers }
            registry.add("kafka.schema-registry-url") { "http://${schemaRegistryContainer.host}:${schemaRegistryContainer.firstMappedPort}" }
            registry.add("schema.registry.url") { "http://${schemaRegistryContainer.host}:${schemaRegistryContainer.firstMappedPort}" }
        }
    }
}