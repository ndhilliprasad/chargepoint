plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "chargepoint"

include("transaction-service")
include("authentication-service")
include("common-core")