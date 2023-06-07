package com.n0n5ense.labindicator.server.model

import kotlinx.serialization.Serializable

@Serializable
data class ErrorMessage(
    val message: String
)
