package com.n0n5ense.labindicator.server.model

import com.n0n5ense.labindicator.common.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class UserInfo(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val name: String,
    val grade: String
)
