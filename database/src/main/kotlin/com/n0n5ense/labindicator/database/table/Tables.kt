package com.n0n5ense.labindicator.database.table

import com.n0n5ense.labindicator.common.Permissions
import com.n0n5ense.labindicator.common.RoomStatus
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

internal open class StringIdTable(
    name: String = "",
    columnName: String = "id",
    defaultValueFun: (() -> String)? = null
): IdTable<String>(name) {
    override val id: Column<EntityID<String>> = if(defaultValueFun != null) {
        text(columnName).clientDefault(defaultValueFun).uniqueIndex().entityId()
    } else {
        text(columnName).uniqueIndex().entityId()
    }
    override val primaryKey by lazy { super.primaryKey ?: PrimaryKey(id) }
}

internal object UserTable: UUIDTable("user") {
    val name = text("name")
    val grade = text("grade")
    val discordId = text("discordId").uniqueIndex().nullable()
    val password = text("password").nullable()
    val isActive = bool("isActive").default(true)
    val display = bool("display").default(true)
}

internal object PermissionTable: IdTable<Permissions>("permission") {
    override val id: Column<EntityID<Permissions>> =
        enumerationByName<Permissions>("id", 127).uniqueIndex().entityId()
    override val primaryKey: PrimaryKey = PrimaryKey(id)
}

internal object UserPermissionTable: Table("user_permissions") {
    val userId = uuid("text_id").references(UserTable.id)
    val permissionId = enumerationByName<Permissions>("permission_id", 127).references(PermissionTable.id)
    override val primaryKey: PrimaryKey = PrimaryKey(userId, permissionId)
}

internal object StatusTable: LongIdTable("status") {
    val userId = uuid("user_id").references(UserTable.id)
    val status = enumerationByName<RoomStatus>("status", 32)
    val time = timestamp("time").clientDefault { Instant.now() }
    val backHour = integer("back_hour").nullable().default(null)
    val backMinute = integer("back_minute").nullable().default(null)
    val note = text("note").nullable().default(null)
}

internal object StatusMessageTable: LongIdTable("status_message") {
    val index = integer("index")
    val messageId = long("message_id")
    val userId = uuid("user_id")
}

internal object ConfigTable: StringIdTable("config") {
    val value = text("value")
}