package com.n0n5ense.labindicator.database

//sealed interface TransactionResult<T> {
//    data class Success<T>(
//        val value: T
//    ): TransactionResult<T>
//
//    data class Error<T>(
//        val throwable: Throwable
//    ): TransactionResult<T>
//
//    companion object {
//        internal fun <T> Result<T>.toTransactionResult(): TransactionResult<T> =
//            this.fold(
//                onSuccess = { Success(it) },
//                onFailure = { Error(it) }
//            )
//    }
//}