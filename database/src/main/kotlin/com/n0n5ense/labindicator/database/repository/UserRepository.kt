package com.n0n5ense.labindicator.database.repository

import com.n0n5ense.labindicator.common.Permissions
import com.n0n5ense.labindicator.database.ConflictException
import com.n0n5ense.labindicator.database.dto.User
import com.n0n5ense.labindicator.database.table.UserPermissionTable
import com.n0n5ense.labindicator.database.table.UserTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.util.*

class UserRepository {
    companion object {
        private fun <T> UpdateBuilder<T>.from(user: User) {
            this[UserTable.id] = user.userId
            this[UserTable.name] = user.name
            this[UserTable.grade] = user.grade
            this[UserTable.discordId] = user.discordId
            this[UserTable.password] = user.password
            this[UserTable.isActive] = user.isActive
            this[UserTable.display] = user.display
        }

        fun add(user: User): Result<User> = transactionRunCatching {
            val insertedResult = UserTable.insertIgnore {
                it.from(user)
            }
            if(insertedResult.insertedCount == 0) {
                throw ConflictException()
            }
            if(user.permissions.isNotEmpty()) {
                UserPermissionTable.batchInsert(user.permissions) {
                    this[UserPermissionTable.permissionId] = it
                    this[UserPermissionTable.userId] = user.userId
                }
            }
            insertedResult.resultedValues?.first()?.toUser()!!
        }

        fun exists(userId: UUID): Result<Boolean> = transactionRunCatching {
            UserTable.select { UserTable.id eq userId }.limit(1).count() != 0L
        }

        fun existsByDiscordId(discordId: String): Result<Boolean> = transactionRunCatching {
            UserTable.select { UserTable.discordId eq discordId }.limit(1).count() != 0L
        }

        fun get(userId: UUID): Result<User?> = transactionRunCatching {
            UserTable.select { UserTable.id eq userId }
                .firstOrNull()?.toUser()
        }

        fun getByDiscordId(discordId: String): Result<User?> = transactionRunCatching {
            UserTable.select { UserTable.discordId eq discordId }
                .firstOrNull()?.toUser()
        }

        fun getUserIdByDiscordId(discordId: String): Result<UUID?> = transactionRunCatching {
            UserTable.select { UserTable.discordId eq discordId }
                .adjustSlice { UserTable.slice(UserTable.id) }
                .firstOrNull()?.get(UserTable.id)?.value
        }

        fun getUserPermissions(userId: UUID): Result<List<Permissions>> = transactionRunCatching {
            UserPermissionTable.select { UserPermissionTable.userId eq userId }
                .map { it[UserPermissionTable.permissionId] }
        }

        fun hasPermission(userId: UUID, permission: Permissions): Result<Boolean> = transactionRunCatching {
            UserPermissionTable.select {
                (UserPermissionTable.userId eq userId) and (UserPermissionTable.permissionId eq permission)
            }.count() != 0L
        }

        fun hasPermissionByDiscordId(discordId: String, permission: Permissions): Result<Boolean> =
            transactionRunCatching {
                val user = getByDiscordId(discordId).getOrThrow()!!
                UserPermissionTable.select {
                    (UserPermissionTable.userId eq user.userId) and (UserPermissionTable.permissionId eq permission)
                }.count() != 0L
            }


        fun update(user: User): Result<Boolean> = transactionRunCatching {
            UserTable.update(where = { UserTable.id eq user.userId }) {
                it.from(user)
            } != 0
        }
    }
}