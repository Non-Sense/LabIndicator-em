package com.n0n5ense.labindicator.server.api

import com.n0n5ense.labindicator.common.Permissions
import com.n0n5ense.labindicator.common.RoomStatus
import com.n0n5ense.labindicator.database.dto.Status
import com.n0n5ense.labindicator.database.repository.StatusRepository
import com.n0n5ense.labindicator.database.repository.UserRepository
import com.n0n5ense.labindicator.server.checkPermissionFromPayload
import com.n0n5ense.labindicator.server.getJwtPayload
import com.n0n5ense.labindicator.server.model.PostStatus
import com.n0n5ense.labindicator.server.model.ResponseMessage
import com.n0n5ense.labindicator.server.runIf
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import org.slf4j.LoggerFactory

internal fun Route.userApi() {
    val logger = LoggerFactory.getLogger("UserApi")!!
    authenticate {
        route("/users") {
            route("/{userId}/statuses") {
                post {
                    val status = call.receive<PostStatus>()
                    val userId = getUserIdParameter()
                    val payload = getJwtPayload()
                    checkPermissionFromPayload(payload, Permissions.EditOtherUser).getOrElse {
                        logger.warn(it.stackTraceToString())
                        call.respond(InternalServerError, ResponseMessage("server error"))
                        return@post
                    }.runIf({ !it }) {
                        call.respond(Forbidden, ResponseMessage("Insufficient permission"))
                        return@post
                    }

                    val enumStatus = kotlin.runCatching { RoomStatus.valueOf(status.status) }.getOrElse {
                        call.respond(BadRequest, ResponseMessage("invalid status name"))
                        return@post
                    }
                    UserRepository.exists(userId).getOrElse {
                        logger.warn(it.stackTraceToString())
                        call.respond(InternalServerError, ResponseMessage("server error"))
                        return@post
                    }.runIf({ !it }) {
                        call.respond(NotFound, ResponseMessage("user not found"))
                        return@post
                    }
                    StatusRepository.add(Status(userId, enumStatus)).getOrElse {
                        logger.warn(it.stackTraceToString())
                        call.respond(InternalServerError, ResponseMessage("server error"))
                        return@post
                    }
                    call.respond(OK, ResponseMessage("ok"))
                }
            }

        }
    }
}

private fun PipelineContext<Unit, ApplicationCall>.getUserIdParameter(): String {
    return call.parameters["userId"]!!
}