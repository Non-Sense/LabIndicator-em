package com.n0n5ense.labindicator.database.repository

import com.n0n5ense.labindicator.database.dto.Status
import com.n0n5ense.labindicator.database.dto.StatusToDisplay
import com.n0n5ense.labindicator.database.table.StatusTable
import com.n0n5ense.labindicator.database.table.UserTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class StatusRepository {
    companion object {
        fun add(status: Status): Result<Boolean> {
            return kotlin.runCatching {
                transaction {
                    StatusTable.insert {
                        it[StatusTable.userId] = status.userId
                        it[StatusTable.status] = status.status
                        it[StatusTable.time] = status.time
                        it[StatusTable.backHour] = status.backHour
                        it[StatusTable.backMinute] = status.backMinute
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
                        .slice(u2.columns + UserTable.columns)
                        .select { (UserTable.display eq true) and (UserTable.isActive eq true) }
                        .map {
                            StatusToDisplay(
                                user = it.toUser(),
                                status = it[u2[StatusTable.status]],
                                time = it[u2[StatusTable.time]],
                                backHour = it[u2[StatusTable.backHour]],
                                backMinute = it[u2[StatusTable.backMinute]]
                            )
                        }
                }
            }

        }

    }
}