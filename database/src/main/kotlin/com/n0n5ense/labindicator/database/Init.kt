package com.n0n5ense.labindicator.database

import com.n0n5ense.labindicator.common.Permissions
import com.n0n5ense.labindicator.database.entity.Permission
import com.n0n5ense.labindicator.database.entity.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

fun connectToDatabase(databasePath: String) {
    Database.connect("jdbc:sqlite:$databasePath", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    initDatabase()
}

private fun initDatabase() {
    transaction {
        SchemaUtils.drop(PermissionTable)
        SchemaUtils.create(UserTable, PermissionTable, UserPermissionsTable, StatusTable)
        PermissionTable.batchInsert(Permissions.values().asSequence()) {
            from(Permission(permission = it))
        }
    }
}