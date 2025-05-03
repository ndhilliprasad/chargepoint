package com.chargepoint.common.core.model

data class AuthorizeRequest(
    var stationUuid: String,
    var driverInfo: DriverInfo
)

data class DriverInfo(
    var id: String
)
