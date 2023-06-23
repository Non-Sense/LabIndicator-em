package com.n0n5ense.labindicator.database.repository

import com.n0n5ense.labindicator.database.dto.User
import com.n0n5ense.labindicator.database.table.UserTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.DeleteStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

internal fun ResultRow.toUser(): User {
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

internal fun <T> transactionRunCatching(block: () -> T) = runCatching {
    transaction {
        block()
    }
}

internal fun <T: Table> T.deleteWhere(
    limit: Int? = null,
    offset: Long? = null,
    op: SqlExpressionBuilder.() -> Op<Boolean>
) = DeleteStatement.where(
    TransactionManager.current(),
    this@deleteWhere,
    op(SqlExpressionBuilder),
    false,
    limit,
    offset
)