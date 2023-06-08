package com.n0n5ense.labindicator.server

import com.n0n5ense.labindicator.server.api.authApi
import com.n0n5ense.labindicator.server.api.statusApi
import com.n0n5ense.labindicator.server.api.userApi
import io.ktor.server.application.*
import io.ktor.server.routing.*

internal fun Application.configureRouting(

) {
    routing {
        route("/api") {
            statusApi()
            authApi()
            userApi()
        }
    }
}