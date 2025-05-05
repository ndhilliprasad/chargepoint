package com.chargepoint.transaction.controller.impl

import com.chargepoint.common.core.model.AuthorizationStatus
import com.chargepoint.common.core.model.AuthorizeRequest
import com.chargepoint.common.core.model.AuthorizeResponse
import com.chargepoint.transaction.service.TransactionService
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@WebFluxTest(controllers = [TransactionControllerImpl::class])
class TransactionControllerTest {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @MockkBean
    lateinit var transactionService: TransactionService

    val objectMapper = jacksonObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SnakeCaseStrategy())

    @Test
    fun `should return ACCEPTED`() {

        val authorizeRequestBody = """
            {
                "station_uuid" : "08286161-4c11-406b-a089-ac2ecff507f1",
                "driver_info" : {
                    "id": "6af827f3-a412-4868-bf93-b179d421b14823"
                }
            }
        """.trimIndent()

        val authorizeRequest: AuthorizeRequest = objectMapper.readValue(authorizeRequestBody)
        val authorizeResponse = AuthorizeResponse(AuthorizationStatus.ACCEPTED)

        every { transactionService.processAuthorizationRequest(authorizeRequest) } returns Mono.just(authorizeResponse)

        webTestClient.post()
            .uri("/authorize")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(authorizeRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody(AuthorizeResponse::class.java)
            .consumeWith { response ->
                val body = response.responseBody
                assert(body != null)
                assert(body?.authorizationStatus?.name == "ACCEPTED")
            }
    }

    @Test
    fun `should return UNKNOW`() {

        val authorizeRequestBody = """
            {
                "station_uuid" : "08286161-4c11-406b-a089-ac2ecff507f1",
                "driver_info" : {
                    "id": "6af827f3-a412-4868-bf93-b179d421b14823"
                }
            }
        """.trimIndent()

        val authorizeRequest: AuthorizeRequest = objectMapper.readValue(authorizeRequestBody)
        val authorizeResponse = AuthorizeResponse(AuthorizationStatus.UNKNOWN)

        every { transactionService.processAuthorizationRequest(authorizeRequest) } returns Mono.just(authorizeResponse)

        webTestClient.post()
            .uri("/authorize")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(authorizeRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody(AuthorizeResponse::class.java)
            .consumeWith { response ->
                val body = response.responseBody
                assert(body != null)
                assert(body?.authorizationStatus?.name == "UNKNOWN")
            }
    }

    @Test
    fun `should fail when request body is missing`() {

        webTestClient.post()
            .uri("/authorize")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest
    }
}