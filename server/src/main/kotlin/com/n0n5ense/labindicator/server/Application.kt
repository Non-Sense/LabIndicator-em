package com.n0n5ense.labindicator.api

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json


fun Application.configureSecurity(environment: ApplicationEnvironment) {

    authentication {
        jwt {
            audience = environment.config.property("jwt.audience").getString()
            algorithm = Algorithm.HMAC256(environment.config.property("jwt.secret").getString())
            issuer = environment.config.property("jwt.domain").getString()
            realm = environment.config.property("jwt.realm").getString()
            verifier(
                JWT
                    .require(algorithm)
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                if(credential.payload.audience.contains(audience)) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }
}

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