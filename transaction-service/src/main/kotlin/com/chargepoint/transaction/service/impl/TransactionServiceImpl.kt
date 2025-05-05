package com.chargepoint.transaction.service.impl

import com.chargepoint.common.core.model.AuthenticationRequestMessage
import com.chargepoint.common.core.model.AuthorizationStatus
import com.chargepoint.common.core.model.AuthorizeRequest
import com.chargepoint.common.core.model.AuthorizeResponse
import com.chargepoint.common.core.model.RequestType
import com.chargepoint.transaction.service.TransactionService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.Disposable
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverRecord
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class TransactionServiceImpl(
    private val kafkaSender: KafkaSender<String, String>,
    private val kafkaReceiver: KafkaReceiver<String, String>,
    private val objectMapper: ObjectMapper,
    @Value("\${kafka.producer.topic}")
    private val producerTopic: String
) : TransactionService {

    private val log = LoggerFactory.getLogger(javaClass)

    private val responseMap = ConcurrentHashMap<String, Sinks.One<AuthorizeResponse>>()

    private lateinit var cosumerDisposable: Disposable

    override fun processAuthorizationRequest(authorizeRequest: AuthorizeRequest): Mono<AuthorizeResponse> {

        val requestId = UUID.randomUUID().toString()

        val authenticateRequest = AuthenticationRequestMessage(
            requestId, RequestType.AUTHENTICATION_REQUEST, authorizeRequest.driverInfo.id
        )

        val message = objectMapper.writeValueAsString(authenticateRequest)

        val senderRecord = SenderRecord.create(
            ProducerRecord(producerTopic, requestId, message), requestId
        )

        kafkaSender.send(Mono.just(senderRecord))
            .doOnNext { metadata ->
                log.info("Message sent successfully: $metadata")
            }
            .doOnError { error -> log.error("Error sending message : ", error) }
            .subscribe()

        return waitForProcessing(requestId)
            .timeout(
                Duration.ofSeconds(1),
                Mono.just(AuthorizeResponse(AuthorizationStatus.INVALID))
            )
    }

    private fun waitForProcessing(requestId: String): Mono<AuthorizeResponse> {
        val sink = Sinks.one<AuthorizeResponse>()
        responseMap.put(requestId, sink)
        return sink.asMono()
    }

    @PostConstruct
    fun consumeResponse(): Disposable {
        return kafkaReceiver.receive()
            .doOnNext { record -> processResponse(record) }
            .subscribe()
    }

    private fun processResponse(record: ReceiverRecord<String, String>) {
        val requestId = record.key()
        val message = record.value()
        val authorizeResponse: AuthorizeResponse = objectMapper.readValue(message)
        log.info("Received message: $message")
        val responseSink = responseMap[requestId]
        if (responseSink != null) {
            responseSink.tryEmitValue(authorizeResponse)
            responseMap.remove(requestId)
        } else {
            log.warn("No sink found for requestId: $requestId")
        }
        record.receiverOffset().acknowledge()
    }

    @PreDestroy
    fun dispose() {
        kafkaSender.close()
        cosumerDisposable.dispose()
    }

}