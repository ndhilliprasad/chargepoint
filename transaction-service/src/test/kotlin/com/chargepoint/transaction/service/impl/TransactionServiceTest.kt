package com.chargepoint.transaction.service.impl

import com.chargepoint.common.core.model.AuthorizationStatus
import com.chargepoint.common.core.model.AuthorizeRequest
import com.chargepoint.common.core.model.AuthorizeResponse
import com.chargepoint.common.core.model.DriverInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverRecord
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kafka.sender.SenderResult
import reactor.test.StepVerifier
import java.util.*
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class TransactionServiceTest {

    @MockK
    lateinit var kafkaSender: KafkaSender<String, String>

    @MockK
    lateinit var kafkaReceiver: KafkaReceiver<String, String>

    lateinit var transactionService: TransactionServiceImpl

    lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        objectMapper = jacksonObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SnakeCaseStrategy())
        transactionService = TransactionServiceImpl(kafkaSender, kafkaReceiver, objectMapper, "test-topic")
    }

    @Test
    fun `should timeout and return INVALID response`() {

        val authorizeRequest = AuthorizeRequest(
            UUID.randomUUID().toString(),
            DriverInfo(UUID.randomUUID().toString())
        )

        val capturedRecords = mutableListOf<SenderRecord<String, String, *>>()
        every { kafkaSender.send<Any>(any()) } answers {
            val inputMono = firstArg<Mono<SenderRecord<String, String, *>>>()

            // Capture the records as they're emitted
            inputMono
                .doOnNext { capturedRecords.add(it) }
                .map {
                    mockk<SenderResult<Any>>(relaxed = true) {
                        every { correlationMetadata() } returns it.correlationMetadata()
                    }
                }.flux()
        }

        every { kafkaSender.close() } returns Unit

        val authorizeResponseMono = transactionService.processAuthorizationRequest(authorizeRequest)

        val authorizeResponse = AuthorizeResponse(AuthorizationStatus.ACCEPTED)

        val receiverRecord = mockk<ReceiverRecord<String, String>> {
            every { value() } returns objectMapper.writeValueAsString(authorizeResponse)
            every { key() } returns UUID.randomUUID().toString()
            every { receiverOffset().acknowledge() } just Runs
        }

        every { kafkaReceiver.receive() } returns Flux.just(receiverRecord)

        val disposable = transactionService.consumeResponse()
        disposable.dispose()

        StepVerifier.create(authorizeResponseMono)
            .consumeNextWith { authorizeResponse ->
                assert(authorizeResponse.authorizationStatus == AuthorizationStatus.INVALID)
            }
            .verifyComplete()
    }

    @Test
    fun `should Authenticate the valid driver`() {

        val authorizeRequest = AuthorizeRequest(
            UUID.randomUUID().toString(),
            DriverInfo(UUID.randomUUID().toString())
        )

        val capturedRecords = mutableListOf<SenderRecord<String, String, *>>()
        every { kafkaSender.send<Any>(any()) } answers {
            val inputMono = firstArg<Mono<SenderRecord<String, String, *>>>()

            // Capture the records as they're emitted
            inputMono
                .doOnNext { capturedRecords.add(it) }
                .map {
                    mockk<SenderResult<Any>>(relaxed = true) {
                        every { correlationMetadata() } returns it.correlationMetadata()
                    }
                }.flux()
        }

        every { kafkaSender.close() } returns Unit

        val authorizeResponseMono = transactionService.processAuthorizationRequest(authorizeRequest)

        val authorizeResponse = AuthorizeResponse(AuthorizationStatus.ACCEPTED)

        val receiverRecord = mockk<ReceiverRecord<String, String>> {
            every { value() } returns objectMapper.writeValueAsString(authorizeResponse)
            every { key() } returns capturedRecords.map { it.key() }.first()
            every { receiverOffset().acknowledge() } just Runs
        }

        every { kafkaReceiver.receive() } returns Flux.just(receiverRecord)

        val disposable = transactionService.consumeResponse()
        disposable.dispose()

        StepVerifier.create(authorizeResponseMono)
            .consumeNextWith { authorizeResponse ->
                assert(authorizeResponse.authorizationStatus == AuthorizationStatus.ACCEPTED)
            }
            .verifyComplete()
    }


}