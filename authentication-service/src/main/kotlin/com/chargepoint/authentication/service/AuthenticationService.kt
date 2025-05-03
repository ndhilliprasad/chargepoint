package com.chargepoint.authentication.service

import com.chargepoint.common.core.model.AuthenticationRequestMessage
import com.chargepoint.common.core.model.AuthorizationStatus
import com.chargepoint.common.core.model.AuthorizeResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.annotation.PostConstruct
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord

@Service
class AuthenticationService(
    private val kafkaSender: KafkaSender<String, String>,
    private val kafkaReceiver: KafkaReceiver<String, String>,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val driveIds = mutableSetOf(
        "6af827f3-a412-4868-bf93-b179d421b146",
        "6af827f3-a412-4868-bf93-b179d421b147",
        "6af827f3-a412-4868-bf93-b179d421b148",
        "6af827f3-a412-4868-bf93-b179d421b149",
    )

    @Value("\${kafka.producer.topic}")
    private lateinit var producerTopic: String

    @PostConstruct
    fun runConsumer() {

        kafkaReceiver.receive()
            .subscribe { record ->
                val requestId = record.key()
                val message = record.value()
                log.info("Received message: $message")
                val authenticationMessage: AuthenticationRequestMessage = objectMapper.readValue(message)

                val authorizeResponse = if (driveIds.contains(authenticationMessage.payload)) {
                    AuthorizeResponse(AuthorizationStatus.ACCEPTED)
                } else {
                    AuthorizeResponse(AuthorizationStatus.UNKNOWN)
                }

                val payload = objectMapper.writeValueAsString(authorizeResponse)

                val senderRecord = SenderRecord.create(
                    ProducerRecord(producerTopic, requestId, payload), requestId)

                kafkaSender.send(Mono.just(senderRecord))
                    .doOnNext { metadata ->
                        log.info("Response sent successfully: $metadata")
                    }
                    .doOnError { error -> log.error("Error sending response : ", error) }
                    .subscribe()
            }
    }

}