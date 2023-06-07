package com.n0n5ense.labindicator.server

import io.ktor.server.application.*
import io.ktor.server.routing.*

internal fun Application.configureRouting(

) {
    routing {
        route("/api") {
            statusApi()
            authApi()
        }
    }
}