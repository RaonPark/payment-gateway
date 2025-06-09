package com.example.springcloud.extension

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.Network
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.kafka.KafkaContainer

class SchemaRegistryContainer: GenericContainer<SchemaRegistryContainer>("confluentinc/cp-schema-registry:7.5.0") {
    init {
        withExposedPorts(8085)
    }

    fun withKafka(kafka: ConfluentKafkaContainer): SchemaRegistryContainer {
        return withKafka(kafka.network!!, "${kafka.networkAliases[0]}:9092")
    }

    private fun withKafka(network: Network, bootstrapServers: String): SchemaRegistryContainer {
        withNetwork(network)
        withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
        withEnv("SCHEMA_REGISTRY_CUB_KAFKA_MIN_BROKERS", "1")
        withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://$bootstrapServers")
        withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8085")

        return this
    }
}