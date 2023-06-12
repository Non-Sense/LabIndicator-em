package com.n0n5ense.labindicator.database.repository

import com.n0n5ense.labindicator.database.dto.User
import com.n0n5ense.labindicator.database.table.UserTable
import org.jetbrains.exposed.sql.ResultRow

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