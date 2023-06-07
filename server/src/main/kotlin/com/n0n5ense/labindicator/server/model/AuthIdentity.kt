package com.n0n5ense.labindicator.server.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthIdentity(
    val id: String,
    val password: String
)
