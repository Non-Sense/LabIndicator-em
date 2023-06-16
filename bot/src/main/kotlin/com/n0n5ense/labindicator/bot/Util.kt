package com.n0n5ense.labindicator.bot

sealed interface CommandResult<out T> {
    data class Success<out T>(val value: T): CommandResult<T>
    data class Failure<out T>(val message: String): CommandResult<T>


}