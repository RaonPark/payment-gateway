package com.example.springcloud.extension

import com.example.springcloud.payment.entity.mongo.PaymentOutbox
import com.example.springcloud.payment.enums.PaymentStatus
import com.example.springcloud.payment.support.MachineIdGenerator
import com.example.springcloud.payment.support.SnowflakeIdGenerator
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.where
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import reactor.test.StepVerifier
import kotlin.test.assertEquals

@Testcontainers
@DataMongoTest
class MongoTest {
    companion object {
        @Container
        private val mongoDBContainer = MongoDBContainer(
            DockerImageName.parse("mongo:8.0-rc")).withExposedPorts(27017)

        @JvmStatic
        @DynamicPropertySource
        fun setProperties(registry: DynamicPropertyRegistry) {
            mongoDBContainer.start()
            registry.add("spring.data.mongodb.host") { mongoDBContainer.host }
            registry.add("spring.data.mongodb.port") { mongoDBContainer.getMappedPort(27017) }
        }

        private val snowflakeIdGenerator = SnowflakeIdGenerator(MachineIdGenerator.machineId())
    }

    @Autowired
    lateinit var mongoOperations: ReactiveMongoOperations

    @BeforeEach
    fun before() {
        mongoOperations.dropCollection("payment_outbox").`as` { StepVerifier.create(it) }.verifyComplete()
    }

    @Test
    fun `find One payment Outbox`() {
        val paymentOutbox = PaymentOutbox(
            id = snowflakeIdGenerator.nextId(),
            paymentId = 1234L,
            paymentStatus = PaymentStatus.REQUESTED
        )

        StepVerifier.create(
            mongoOperations.insert(paymentOutbox)
        ).expectNextCount(1L).verifyComplete()

        assertEquals(runBlocking {
            mongoOperations.find(Query(where(PaymentOutbox::paymentId).`is`(1234L)), PaymentOutbox::class.java)
                .awaitSingle()
        }.paymentId, 1234L)
    }
}