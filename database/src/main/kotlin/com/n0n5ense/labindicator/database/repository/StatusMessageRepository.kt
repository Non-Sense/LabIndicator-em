package com.n0n5ense.labindicator.database.repository

import com.n0n5ense.labindicator.database.dto.StatusMessage
import com.n0n5ense.labindicator.database.table.StatusMessageTable
import com.n0n5ense.labindicator.database.table.StatusTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class StatusMessageRepository {
    companion object {
        fun add(statusMessage: StatusMessage): Result<Boolean> = transactionRunCatching {
            StatusMessageTable.insert {
                it[messageId] = statusMessage.messageId
                it[index] = statusMessage.index
            }.insertedCount != 0
        }


        fun deleteAll(): Result<Unit> = transactionRunCatching { StatusMessageTable.deleteAll() }

        fun getAll(): Result<List<StatusMessage>> = transactionRunCatching {
            StatusMessageTable.selectAll()
                .orderBy(StatusMessageTable.index, SortOrder.ASC)
                .map {
                    StatusMessage(
                        index = it[StatusMessageTable.index],
                        messageId = it[StatusMessageTable.messageId]
                    )
                }
        }
    }
}