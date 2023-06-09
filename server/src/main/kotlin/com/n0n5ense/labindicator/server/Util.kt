package com.n0n5ense.labindicator.server

import com.auth0.jwt.interfaces.Payload
import com.n0n5ense.labindicator.common.Permissions
import com.n0n5ense.labindicator.database.repository.UserRepository
import com.n0n5ense.labindicator.server.model.Status
import com.n0n5ense.labindicator.server.model.StatusList
import com.n0n5ense.labindicator.server.model.UserInfo
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.util.pipeline.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal fun Instant.toServeString() = this.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_INSTANT)

internal fun List<com.n0n5ense.labindicator.database.dto.StatusToDisplay>.toStatusList(): StatusList {
    val list = this.map {
        Status(
            user = UserInfo(it.user.name, it.user.grade),
            status = it.status.name,
            time = it.time.toServeString()
        )
    }
    return StatusList(list)
}

internal fun PipelineContext<Unit, ApplicationCall>.getJwtPayload() = call.principal<JWTPrincipal>()?.payload!!

internal fun checkPermissionFromPayload(payload: Payload, permission: Permissions): Result<Boolean> {
    return UserRepository.hasPermission(payload.getClaim("id").asString(), permission)
}
