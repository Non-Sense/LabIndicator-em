package com.n0n5ense.labindicator.server.model

import kotlinx.serialization.Serializable

@Serializable
data class Status(
    val user: UserInfo,
    val status: String,
    val time: String
)

@Serializable
data class StatusList(
    val statuses: List<Status>
)

@Serializable
data class PostStatus(
    val status: String
)