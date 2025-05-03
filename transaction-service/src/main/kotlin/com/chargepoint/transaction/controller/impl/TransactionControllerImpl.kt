package com.chargepoint.transaction.controller.impl

import com.chargepoint.common.core.model.AuthorizeRequest
import com.chargepoint.common.core.model.AuthorizeResponse
import com.chargepoint.transaction.controller.TransactionController
import com.chargepoint.transaction.service.TransactionService
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class TransactionControllerImpl(val transactionService: TransactionService) : TransactionController {

    override fun authorize(@RequestBody authorizeRequest: AuthorizeRequest): Mono<AuthorizeResponse> {
        return transactionService.processAuthorizationRequest(authorizeRequest)
    }
}