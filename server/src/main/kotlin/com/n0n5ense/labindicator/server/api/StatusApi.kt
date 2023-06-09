package com.n0n5ense.labindicator.server.api

import com.n0n5ense.labindicator.common.RoomStatus
import com.n0n5ense.labindicator.database.dto.Status
import com.n0n5ense.labindicator.database.repository.StatusRepository
import com.n0n5ense.labindicator.server.getJwtPayload
import com.n0n5ense.labindicator.server.model.ResponseMessage
import com.n0n5ense.labindicator.server.model.PostStatus
import com.n0n5ense.labindicator.server.toStatusList
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory
import java.util.*

internal fun Route.statusApi() {
    val logger = LoggerFactory.getLogger("StatusApi")!!
    authenticate {
        route("/statuses") {
            get("/latest") {
                val statuses = StatusRepository.getLatest().getOrElse {
                    logger.warn(it.stackTraceToString())
                    call.respond(InternalServerError, ResponseMessage("server error"))
                    return@get
                }.toStatusList()

                call.respond(OK, statuses)
            }

            post {
                val status = call.receive<PostStatus>()
                val payload = getJwtPayload()
                val userId = UUID.fromString(payload.getClaim("id").asString())
                val enumStatus = kotlin.runCatching { RoomStatus.valueOf(status.status) }.getOrElse {
                    call.respond(BadRequest, ResponseMessage("invalid status name"))
                    return@post
                }
                StatusRepository.add(Status(userId, enumStatus)).onFailure {

                }
                call.respond(OK, ResponseMessage("ok"))
            }

        }
    }
}