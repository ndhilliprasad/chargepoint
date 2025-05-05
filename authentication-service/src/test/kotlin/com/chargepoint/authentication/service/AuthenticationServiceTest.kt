package com.chargepoint.authentication.service

import com.chargepoint.common.core.model.AuthenticationRequestMessage
import com.chargepoint.common.core.model.RequestType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderRecord
import reactor.test.StepVerifier
import java.util.*
import kotlin.test.Ignore
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@EmbeddedKafka(
    partitions = 1, topics = ["request-topic", "response-topic"],
    ports = [9092],
    brokerProperties = ["listeners=PLAINTEXT://localhost:9092", "port=9092"]
)
class AuthenticationServiceTest {

    @Value("\${kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    val objectMapper: ObjectMapper = jacksonObjectMapper()
        .setPropertyNamingStrategy(PropertyNamingStrategies.SnakeCaseStrategy())

    @Test
    @Ignore
    fun testAuthenticateDrive() {

        val requestId = UUID.randomUUID().toString()

        val authenticateRequest = AuthenticationRequestMessage(
            requestId, RequestType.AUTHENTICATION_REQUEST, UUID.randomUUID().toString()
        )

        val message = objectMapper.writeValueAsString(authenticateRequest)

        val senderRecord = SenderRecord.create(
            ProducerRecord("request-topic", requestId, message), requestId
        )

        kafkaSender().send(Mono.just(senderRecord))
            .doOnNext { metadata ->
                println("Message sent successfully: $metadata")
            }
            .doOnError { error -> println("Error sending message") }
            .subscribe()

        val flux = kafkaReceiver().receive()
            .doOnNext { record ->
                val key = record.key()
                val message = record.value()
                println("Received message: $message")
                assert(key == requestId)
                record.receiverOffset().acknowledge()
            }

        StepVerifier.create(flux)
            .verifyComplete()

    }

    fun kafkaSender(): KafkaSender<String, String> {
        val producerConfig = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all"
        )
        return KafkaSender.create(SenderOptions.create(producerConfig))
    }

    fun kafkaReceiver(): KafkaReceiver<String, String> {
        val consumerConfig = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.GROUP_ID_CONFIG to "test-group"
        )
        val receiverOptions = ReceiverOptions.create<String, String>(consumerConfig)
            .subscription(listOf("response-topic"))
        return KafkaReceiver.create(receiverOptions)
    }
}