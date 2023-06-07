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
    val discordId: String? = null,
    val password: String? = null,
    @ManyToMany
    @JoinTable(name = "user_permissions")
    val permissions: List<Permission> = listOf(),
    val isActive: Boolean = true,
)

@Entity
data class Permission(
    @Id
    @Enumerated(EnumType.STRING)
    @Convert(converter = PermissionConverter::class)
    @Column(name = "id")
    val permission: Permissions,
)

@Converter
class PermissionConverter : AttributeConverter<Permissions, String> {
    override fun convertToDatabaseColumn(attribute: Permissions): String {
        return attribute.name
    }

    override fun convertToEntityAttribute(dbData: String): Permissions {
        return Permissions.valueOf(dbData)
    }
}

@Entity
data class Status(
    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User,
    @Enumerated(EnumType.STRING)
    val status: RoomStatus,
    @Id @GeneratedValue
    val id: Long? = null,
    val time: LocalDateTime = LocalDateTime.now(),
)