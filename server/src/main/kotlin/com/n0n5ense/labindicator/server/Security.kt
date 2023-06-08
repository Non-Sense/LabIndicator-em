package com.n0n5ense.labindicator.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.n0n5ense.labindicator.database.dto.User
import com.n0n5ense.labindicator.server.model.AccessToken
import com.n0n5ense.labindicator.server.model.RefreshToken
import com.n0n5ense.labindicator.server.model.ResponseMessage
import com.n0n5ense.labindicator.server.model.UserId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import java.util.*

private lateinit var algorithm: Algorithm
private const val audience = "LabIndicator"

fun Application.configureSecurity(secret: String) {
    algorithm = Algorithm.HMAC256(secret)
    install(Authentication) {
        jwt {
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ResponseMessage("unauthorized"))
            }
            verifier(
                JWT
                    .require(algorithm)
                    .withAudience(audience)
                    .build()
            )
            validate {
                if(it.payload.getClaim("acs").asBoolean()) {
                    JWTPrincipal(it.payload)
                } else null
            }
        }
    }
}

class Security {
    companion object {
        private val refreshTokenVerifier = JWT
            .require(algorithm)
            .withClaim("ref", true)
            .build()

        fun validateRefreshToken(token: RefreshToken): UserId? {
            val jwt = kotlin.runCatching {
                refreshTokenVerifier.verify(token.refresh)!!
            }.getOrElse { return null }
            return jwt.claims["id"]?.asString()?.let { UserId(it) }
        }

        fun createRefreshToken(user: User): RefreshToken? {
            if(!user.isActive)
                return null
            return JWT.create()
                .withExpiresAt(Date(Date().time + 86400000L))
                .withIssuedAt(Date())
                .withClaim("id", user.userId)
                .withClaim("ref", true)
                .withAudience(audience)
                .sign(algorithm)
                .let { RefreshToken(it) }
        }

        fun createAccessToken(user: User): AccessToken? {
            if(!user.isActive)
                return null
            return JWT.create()
                .withExpiresAt(Date(Date().time + 300000L))
                .withIssuedAt(Date())
                .withClaim("acs", true)
                .withClaim("id", user.userId)
                .withAudience(audience)
                .sign(algorithm)
                .let { AccessToken(it) }
        }
    }
}