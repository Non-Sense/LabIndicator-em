package com.n0n5ense.labindicator.bot

internal sealed interface CommandResult {
    data class Success(val message: String): CommandResult
    data class Failure(val message: String): CommandResult
}