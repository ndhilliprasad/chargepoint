package com.chargepoint.transaction.controller

import com.chargepoint.common.core.model.AuthorizeRequest
import com.chargepoint.common.core.model.AuthorizeResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import reactor.core.publisher.Mono

//Interface will be useful for configuring the swagger and other documentation

interface TransactionController {
    /**
     * Authorizes a transaction based on the provided request.
     *
     * @param authorizeRequest The request containing transaction details.
     * @return A Mono emitting the authorization response.
     */
    @PostMapping("/authorize", consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun authorize(authorizeRequest: AuthorizeRequest): Mono<AuthorizeResponse>
}