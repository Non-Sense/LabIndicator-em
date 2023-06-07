package com.n0n5ense.labindicator.server.model

import kotlinx.serialization.Serializable

@Serializable
data class AccessToken(
    val access: String
)

@Serializable
data class RefreshToken(
    val refresh: String
)

@Serializable
data class TokenPair(
    val refresh: String,
    val access: String
)