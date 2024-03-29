package com.n0n5ense.labindicator.database

import com.n0n5ense.labindicator.common.Permissions
import com.n0n5ense.labindicator.database.table.*
import com.n0n5ense.labindicator.database.table.PermissionTable
import com.n0n5ense.labindicator.database.table.StatusMessageTable
import com.n0n5ense.labindicator.database.table.StatusTable
import com.n0n5ense.labindicator.database.table.UserPermissionTable
import com.n0n5ense.labindicator.database.table.UserTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.exists
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
        val permissionTableCreated = !PermissionTable.exists()
        SchemaUtils.create(UserTable, PermissionTable, UserPermissionTable, StatusTable, StatusMessageTable, ConfigTable)
        if(permissionTableCreated) {
            PermissionTable.batchInsert(Permissions.values().asSequence()) {
                this[PermissionTable.id] = it
            }
        }
    }
}