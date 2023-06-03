package com.n0n5ense.labindicator.database.table

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

object UserTable : UUIDTable() {
    val discordId = text("discord_id").nullable().default(null)
    val name = text("name")
    val isActive = bool("is_active").default(true)
    val grade = text("grade")
}

class User(uuid: EntityID<UUID>) : UUIDEntity(uuid) {
    var discordId by UserTable.discordId
    var name by UserTable.name
    var isActive by UserTable.isActive
    var grade by UserTable.grade
}