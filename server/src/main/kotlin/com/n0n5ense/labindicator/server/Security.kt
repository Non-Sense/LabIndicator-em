package com.n0n5ense.labindicator.server

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.n0n5ense.labindicator.database.entity.User
import com.n0n5ense.labindicator.server.model.AccessToken
import com.n0n5ense.labindicator.server.model.RefreshToken
import com.n0n5ense.labindicator.server.model.UserId
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.*

private lateinit var algorithm: Algorithm

fun Application.configureSecurity(secret: String) {

    authentication {
        jwt {
            algorithm = Algorithm.HMAC256(secret)
            verifier(
                JWT
                    .require(algorithm)
                    .withClaim("acs", true)
                    .build()
            )
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
                .withClaim("id", user.id)
                .withClaim("ref", true)
                .sign(algorithm)
                .let { RefreshToken(it) }
        }

        fun createAccessToken(user: User): AccessToken? {
            if(!user.isActive)
                return null
            return JWT.create()
                .withExpiresAt(Date(Date().time + 300000L))
                .withClaim("acs", true)
                .withClaim("id", user.id)
                .sign(algorithm)
                .let { AccessToken(it) }
        }
    }
}