package com.n0n5ense.labindicator.database.repository

import com.n0n5ense.labindicator.database.table.ConfigTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class ConfigRepository {
    companion object {
        fun set(key: String, value: String): Result<Unit> {
            return kotlin.runCatching {
                transaction {
                    if(ConfigTable.select { ConfigTable.id eq key }.count() == 0L) {
                        ConfigTable.insert {
                            it[ConfigTable.id] = key
                            it[ConfigTable.value] = value
                        }
                        return@transaction
                    }
                    ConfigTable.update({ ConfigTable.id eq key}) {
                        it[ConfigTable.id] = key
                        it[ConfigTable.value] = value
                    }
                }
            }
        }

        fun get(key: String): Result<String?> {
            return kotlin.runCatching {
                transaction {
                    ConfigTable.select { ConfigTable.id eq key }.firstOrNull()?.let {
                        it[ConfigTable.value]
                    }
                }
            }
        }

        fun remove(key: String): Result<Unit> {
            return kotlin.runCatching {
                transaction {
                    ConfigTable.deleteWhere { ConfigTable.id eq key }
                }
            }
        }
    }
}