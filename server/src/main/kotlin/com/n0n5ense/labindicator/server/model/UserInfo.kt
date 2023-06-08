package com.n0n5ense.labindicator.server.model

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val id: String,
    val name: String,
    val grade: String
)