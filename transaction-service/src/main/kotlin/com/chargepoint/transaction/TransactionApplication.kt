package com.chargepoint.transaction

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.chargepoint.transaction", "com.chargepoint.common.core"])
class TransactionApplication

fun main(args: Array<String>) {
    runApplication<TransactionApplication>(*args)
}
