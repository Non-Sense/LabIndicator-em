package com.n0n5ense.labindicator.server.model

import com.n0n5ense.labindicator.common.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class UserId(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID
)
