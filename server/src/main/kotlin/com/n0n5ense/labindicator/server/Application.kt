package com.n0n5ense.labindicator.server

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json


fun startServer(
    port: Int,
    tokenSecret: String
) {
    embeddedServer(
        factory = Netty,
        port = port
    ) {
        configureSecurity(tokenSecret)
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = false
                isLenient = false
            })
        }
        install(Routing)
        configureRouting()
    }.start(true)

}