package com.n0n5ense.labindicator.database.repository

import com.n0n5ense.labindicator.database.table.ConfigTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

class ConfigRepository {
    companion object {
        fun set(key: String, value: String): Result<Unit> = transactionRunCatching {
            if(ConfigTable.select { ConfigTable.id eq key }.count() == 0L) {
                ConfigTable.insert {
                    it[ConfigTable.id] = key
                    it[ConfigTable.value] = value
                }
            } else {
                ConfigTable.update({ ConfigTable.id eq key }) {
                    it[ConfigTable.id] = key
                    it[ConfigTable.value] = value
                }
            }
        }

        fun get(key: String): Result<String?> = transactionRunCatching {
            ConfigTable.select { ConfigTable.id eq key }.firstOrNull()?.let {
                it[ConfigTable.value]
            }
        }

        fun remove(key: String): Result<Unit> = transactionRunCatching {
            ConfigTable.deleteWhere { ConfigTable.id eq key }
        }
    }
}