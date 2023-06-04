package com.n0n5ense.labindicator.database.entity

import com.n0n5ense.labindicator.common.Permissions
import com.n0n5ense.labindicator.common.RoomStatus
import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class User(
    @Id
    val id: String,
    val name: String,
    val grade: String,
    val isActive: Boolean,
    @ManyToMany
    @JoinTable(name = "user_permissions")
    val permissions: List<Permission>
)

@Entity
data class Permission(
    @Id @GeneratedValue
    val id: Long? = null,
    @Enumerated(EnumType.STRING)
    val permission: Permissions
)

@Entity
data class Status(
    @Id @GeneratedValue
    val id: Long,
    val time: LocalDateTime,
    @ManyToOne
    @JoinColumn
    val user: User,
    @Enumerated(EnumType.STRING)
    val status: RoomStatus
)