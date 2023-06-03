package com.n0n5ense.labindicator.server

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json


fun startServer(
    port: Int
) {
    embeddedServer(
        factory = Netty,
        port = port
    ) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = false
                isLenient = false
            })
        }
    }

}