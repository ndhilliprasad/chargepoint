package com.chargepoint.common.core.model

data class AuthenticationRequestMessage (
    val requestId: String,
    val type: RequestType,
    val payload: String,
)

enum class RequestType {
    AUTHENTICATION_REQUEST
}
