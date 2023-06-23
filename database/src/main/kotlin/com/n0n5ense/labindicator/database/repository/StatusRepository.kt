package com.n0n5ense.labindicator.database.repository

import com.n0n5ense.labindicator.database.dto.Status
import com.n0n5ense.labindicator.database.dto.StatusToDisplay
import com.n0n5ense.labindicator.database.table.StatusTable
import com.n0n5ense.labindicator.database.table.UserTable
import org.jetbrains.exposed.sql.*

class StatusRepository {
    companion object {
        fun add(status: Status): Result<Boolean> = transactionRunCatching {
            StatusTable.insert {
                it[StatusTable.userId] = status.userId
                it[StatusTable.status] = status.status
                it[StatusTable.time] = status.time
                it[StatusTable.backHour] = status.backHour
                it[StatusTable.backMinute] = status.backMinute
                it[StatusTable.note] = status.note
            }.insertedCount != 0
        }

        fun getLatest(): Result<List<StatusToDisplay>> = transactionRunCatching {
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
                .orderBy(UserTable.name)
                .map {
                    StatusToDisplay(
                        user = it.toUser(),
                        status = it[u2[StatusTable.status]],
                        note = it[u2[StatusTable.note]],
                        time = it[u2[StatusTable.time]],
                        backHour = it[u2[StatusTable.backHour]],
                        backMinute = it[u2[StatusTable.backMinute]],
                    )
                }
        }
    }
}