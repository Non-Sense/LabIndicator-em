package com.n0n5ense.labindicator.database.repository

import com.n0n5ense.labindicator.database.entity.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository {
    companion object {
        fun add(user: User): Result<User> {
            return kotlin.runCatching {
                transaction {
                    UserTable.insert(user)
                }
            }
        }

        fun exists(id: String): Result<Boolean> {
            return kotlin.runCatching {
                transaction {
                    UserTable.select { UserTable.id eq id }.limit(1).count()
                } != 0L
            }
        }

        fun get(id: String): Result<User?> {
            return kotlin.runCatching {
                transaction {
                    UserTable.select { UserTable.id eq id }
                        .firstOrNull()?.toUser()
                }
            }
        }

        fun update(user: User): Result<Int> {
            return kotlin.runCatching {
                transaction {
                    UserTable.update(where = { UserTable.id eq user.id }) {
                        it.from(user)
                    }
                }
            }
        }
    }
}

class StatusRepository {
    companion object {
        fun add(status: Status): Result<Status> {
            return kotlin.runCatching {
                transaction {
                    StatusTable.insert(status)
                }
            }
        }

        fun getAll(): Result<List<Status>> = kotlin.runCatching {
            transaction {
                StatusTable.innerJoin(UserTable).selectAll().toStatusList()
            }
        }

        fun getLatest(): Result<List<Status>> {
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
                        .selectAll()
                        .map {
                            Status(
                                user = it.toUser(),
                                status = it[u2[StatusTable.status]],
                                id = it[u2[StatusTable.id]],
                                time = it[u2[StatusTable.time]]
                            )
                        }
                }
            }

        }

    }
}