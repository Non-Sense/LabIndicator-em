package com.n0n5ense.labindicator.server.api

import com.n0n5ense.labindicator.database.dto.User
import com.n0n5ense.labindicator.database.repository.UserRepository
import com.n0n5ense.labindicator.server.Security
import com.n0n5ense.labindicator.server.model.AuthIdentity
import com.n0n5ense.labindicator.server.model.ResponseMessage
import com.n0n5ense.labindicator.server.model.RefreshToken
import com.n0n5ense.labindicator.server.model.TokenPair
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.LoggerFactory

internal fun Route.authApi() {
    val logger = LoggerFactory.getLogger("AuthApi")!!
    route("authorize") {
        post {
            val identity = call.receive<AuthIdentity>()
            val user = UserRepository.get(identity.id).getOrElse {
                logger.warn(it.stackTraceToString())
                call.respond(InternalServerError, ResponseMessage("server error"))
                return@post
            }
            if(user?.password == null || !user.isActive) {
                call.respond(Unauthorized, ResponseMessage("unauthorized"))
                return@post
            }
            if(!BCrypt.checkpw(identity.password, user.password)) {
                call.respond(Unauthorized, ResponseMessage("unauthorized"))
                return@post
            }
            val tokenPair = makeTokenPair(user)
            if(tokenPair == null) {
                call.respond(Unauthorized, ResponseMessage("unauthorized"))
                return@post
            }
            call.respond(OK, tokenPair)
        }

        post("refresh") {
            val receivedToken = call.receive<RefreshToken>()
            val userId = Security.validateRefreshToken(receivedToken)
            if(userId == null) {
                call.respond(Unauthorized, ResponseMessage("unauthorized"))
                return@post
            }
            val user = UserRepository.get(userId.id).getOrElse {
                logger.warn(it.stackTraceToString())
                call.respond(InternalServerError, ResponseMessage("server error"))
                return@post
            }
            if(user == null || !user.isActive) {
                call.respond(Unauthorized, ResponseMessage("unauthorized"))
                return@post
            }
            val tokenPair = makeTokenPair(user)
            if(tokenPair == null) {
                call.respond(Unauthorized, ResponseMessage("unauthorized"))
                return@post
            }
            call.respond(OK, tokenPair)
        }
    }
}

private fun makeTokenPair(user: User): TokenPair? {
    val refreshToken = Security.createRefreshToken(user)
    val accessToken = Security.createAccessToken(user)
    if(refreshToken == null || accessToken == null) {
        return null
    }
    return TokenPair(refresh = refreshToken.refresh, access = accessToken.access)
}