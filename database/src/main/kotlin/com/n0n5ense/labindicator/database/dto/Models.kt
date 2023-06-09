package com.n0n5ense.labindicator.database.dto

import com.fasterxml.uuid.Generators
import com.n0n5ense.labindicator.common.Permissions
import com.n0n5ense.labindicator.common.RoomStatus
import java.time.Instant
import java.util.*

data class User(
    val userId: UUID = Generators.timeBasedReorderedGenerator().generate(),
    val name: String,
    val grade: String,
    val discordId: String? = null,
    val password: String? = null,
    val isActive: Boolean = true,
    val display: Boolean = true,
    val permissions: List<Permissions> = listOf()
)

data class Status(
    val userId: UUID,
    val status: RoomStatus,
    val time: Instant = Instant.now()
)

data class StatusToDisplay(
    val user: User,
    val status: RoomStatus,
    val time: Instant = Instant.now()
)