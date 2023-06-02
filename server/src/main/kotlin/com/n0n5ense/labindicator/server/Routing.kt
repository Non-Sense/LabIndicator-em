package com.n0n5ense.labindicator.api

import io.ktor.server.application.*
import io.ktor.server.routing.*

internal fun Application.configureRouting(

) {
    routing {

        route("/api") {
            route("/v1") {
                v1Api()
            }
        }
    }
}