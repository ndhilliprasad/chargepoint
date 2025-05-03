package com.chargepoint.common.core.model

data class AuthorizeResponse(
    var authorizationStatus: AuthorizationStatus
)

enum class AuthorizationStatus(val status: String) {
    ACCEPTED("Accepted"),
    INVALID("Invalid"),
    UNKNOWN("Unknown"),
    REJECTED("Rejected")
}
