package com.example.springcloud.payment.config

import com.raonpark.avro.PaymentCompleted
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.*
import org.springframework.kafka.core.KafkaAdmin.NewTopics
import org.springframework.kafka.listener.ContainerProperties

@Configuration
class KafkaConfig {
    @Value("\${kafka.schema-registry-url}")
    private lateinit var schemaRegistryUrl: String

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Bean
    fun paymentCompletedConsumerFactory(): ConsumerFactory<String, PaymentCompleted> {
        val configMap = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to KafkaAvroDeserializer::class.java,
            ConsumerConfig.GROUP_ID_CONFIG to "payment-groups",
            KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG to "true",
            KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl,
            ConsumerConfig.ISOLATION_LEVEL_CONFIG to "read_committed",
        )

        return DefaultKafkaConsumerFactory(configMap)
    }

    @Bean
    fun paymentCompletedKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, PaymentCompleted> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, PaymentCompleted>()
        factory.consumerFactory = paymentCompletedConsumerFactory()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        factory.containerProperties.eosMode = ContainerProperties.EOSMode.V2

        return factory
    }

    @Bean
    fun paymentCompletedProducerFactory(): ProducerFactory<String, PaymentCompleted> {
        val configMap = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to KafkaAvroSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.RETRIES_CONFIG to 10,
            ProducerConfig.BATCH_SIZE_CONFIG to 10 * 1024 * 1024,
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to "true",
            ProducerConfig.TRANSACTIONAL_ID_CONFIG to "payment-completed-tx",
            ProducerConfig.TRANSACTION_TIMEOUT_CONFIG to "3000",
            KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl,
        )

        val producerFactory = DefaultKafkaProducerFactory<String, PaymentCompleted>(configMap)
        producerFactory.setTransactionIdSuffixStrategy(DefaultTransactionIdSuffixStrategy(5))

        return producerFactory
    }

    @Bean
    fun paymentCompletedKafkaTemplate(): KafkaTemplate<String, PaymentCompleted> {
        val template = KafkaTemplate(paymentCompletedProducerFactory())
        return template
    }

    @Bean
    fun kafkaAdmin(): KafkaAdmin {
        val config = mapOf(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            AdminClientConfig.CLIENT_ID_CONFIG to "kafka-admin",
            AdminClientConfig.SECURITY_PROTOCOL_CONFIG to "PLAINTEXT",
        )
        return KafkaAdmin(config)
    }

    @Bean
    fun kafkaTopics(): NewTopics {
        return NewTopics(
            TopicBuilder.name("payment-completed")
                .partitions(5)
                .replicas(1)
                .build()
        )
    }
}