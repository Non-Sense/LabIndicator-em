package com.n0n5ense.labindicator.database.repository

import com.n0n5ense.labindicator.common.Permissions
import com.n0n5ense.labindicator.database.dto.Status
import com.n0n5ense.labindicator.database.dto.StatusToDisplay
import com.n0n5ense.labindicator.database.dto.User
import com.n0n5ense.labindicator.database.table.StatusTable
import com.n0n5ense.labindicator.database.table.UserPermissionTable
import com.n0n5ense.labindicator.database.table.UserTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

private fun ResultRow.toUser(): User {
    return User(
        userId = this[UserTable.id].value,
        name = this[UserTable.name],
        grade = this[UserTable.grade],
        discordId = this[UserTable.discordId],
        password = this[UserTable.password],
        isActive = this[UserTable.isActive],
        display = this[UserTable.display]
    )
}

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

        fun add(user: User): Result<Boolean> {
            return kotlin.runCatching {
                transaction {
                    val insertedCount = UserTable.insertIgnore {
                        it.from(user)
                    }.insertedCount
                    if(user.permissions.isNotEmpty()) {
                        UserPermissionTable.batchInsert(user.permissions) {
                            this[UserPermissionTable.permissionId] = it
                            this[UserPermissionTable.userId] = user.userId
                        }
                    }
                    insertedCount != 0
                }
            }
        }

        fun exists(userId: UUID): Result<Boolean> {
            return kotlin.runCatching {
                transaction {
                    UserTable.select { UserTable.id eq userId }.limit(1).count()
                } != 0L
            }
        }

        fun existsByDiscordId(discordId: String): Result<Boolean> {
            return kotlin.runCatching {
                transaction {
                    UserTable.select { UserTable.discordId eq discordId }.limit(1).count()
                } != 0L
            }
        }

        fun get(userId: UUID): Result<User?> {
            return kotlin.runCatching {
                transaction {
                    UserTable.select { UserTable.id eq userId }
                        .firstOrNull()?.toUser()
                }
            }
        }

        fun getByDiscordId(discordId: String): Result<User?> {
            return kotlin.runCatching {
                transaction {
                    UserTable.select { UserTable.discordId eq discordId }
                        .firstOrNull()?.toUser()
                }
            }
        }

        fun getUserIdByDiscordId(discordId: String): Result<UUID?> {
            return kotlin.runCatching {
                transaction {
                    UserTable.select { UserTable.discordId eq discordId }
                        .adjustSlice { UserTable.slice(UserTable.id) }
                        .firstOrNull()?.get(UserTable.id)?.value
                }
            }
        }

        fun getUserPermissions(userId: UUID): Result<List<Permissions>> {
            return kotlin.runCatching {
                transaction {
                    UserPermissionTable.select { UserPermissionTable.userId eq userId }
                        .map { it[UserPermissionTable.permissionId] }
                }
            }
        }

        fun hasPermission(userId: UUID, permission: Permissions): Result<Boolean> {
            return kotlin.runCatching {
                transaction {
                    UserPermissionTable.select {
                        (UserPermissionTable.userId eq userId) and (UserPermissionTable.permissionId eq permission)
                    }.count() != 0L
                }
            }
        }

        fun hasPermissionByDiscordId(discordId: String, permission: Permissions): Result<Boolean> {
            return kotlin.runCatching {
                val user = getByDiscordId(discordId).getOrThrow()!!
                transaction {
                    UserPermissionTable.select {
                        (UserPermissionTable.userId eq user.userId) and (UserPermissionTable.permissionId eq permission)
                    }.count() != 0L
                }
            }
        }

        fun update(user: User): Result<Int> {
            return kotlin.runCatching {
                transaction {
                    UserTable.update(where = { UserTable.id eq user.userId }) {
                        it.from(user)
                    }
                }
            }
        }
    }
}

class StatusRepository {
    companion object {
        fun add(status: Status): Result<Boolean> {
            return kotlin.runCatching {
                transaction {
                    StatusTable.insert {
                        it[userId] = status.userId
                        it[this.status] = status.status
                        it[time] = status.time
                    }.insertedCount != 0
                }
            }
        }

//        fun getAll(): Result<List<Status>> = kotlin.runCatching {
//            transaction {
//                StatusTable.innerJoin(UserTable).selectAll().toStatusList()
//            }
//        }

        fun getLatest(): Result<List<StatusToDisplay>> {
            return kotlin.runCatching {
                transaction {
                    val maxId = StatusTable.id.max().alias("maxid")
                    val u1 = StatusTable
                        .slice(StatusTable.userId, maxId)
                        .selectAll()
                        .groupBy(StatusTable.userId)
                        .alias("u1")

                    val u2 = u1.join(
                        joinType = JoinType.INNER,
                        onColumn = maxId.aliasOnlyExpression(),
                        otherTable = StatusTable,
                        otherColumn = StatusTable.id
                    )
                        .slice(StatusTable.columns)
                        .selectAll()
                        .alias("u2")

                    u2.join(
                        joinType = JoinType.INNER,
                        otherTable = UserTable,
                        onColumn = u2[StatusTable.userId],
                        otherColumn = UserTable.id
                    )
                        .slice(StatusTable.columns + UserTable.columns)
                        .select { UserTable.display eq true }
                        .map {
                            StatusToDisplay(
                                user = it.toUser(),
                                status = it[u2[StatusTable.status]],
                                time = it[u2[StatusTable.time]]
                            )
                        }
                }
            }

        }

    }
}