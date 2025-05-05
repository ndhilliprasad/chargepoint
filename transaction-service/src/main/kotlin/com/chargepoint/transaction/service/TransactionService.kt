package com.chargepoint.transaction.service

import com.chargepoint.common.core.model.AuthorizeRequest
import com.chargepoint.common.core.model.AuthorizeResponse
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
interface TransactionService {

    fun processAuthorizationRequest(authorizeRequest: AuthorizeRequest): Mono<AuthorizeResponse>

}