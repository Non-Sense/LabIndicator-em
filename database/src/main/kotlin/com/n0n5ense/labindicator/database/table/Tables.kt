package com.n0n5ense.labindicator.database.table

import com.n0n5ense.labindicator.common.Permissions
import com.n0n5ense.labindicator.common.RoomStatus
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.LongIdTable
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

internal object UserTable: StringIdTable("user") {
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
    val userId = text("text_id").references(UserTable.id)
    val permissionId = enumerationByName<Permissions>("permission_id", 127).references(PermissionTable.id)
    override val primaryKey: PrimaryKey = PrimaryKey(userId, permissionId)
}

internal object StatusTable: LongIdTable("status") {
    val userId = text("user_id").references(UserTable.id)
    val status = enumerationByName<RoomStatus>("status", 32)
    val time = timestamp("time").default(Instant.now())
}