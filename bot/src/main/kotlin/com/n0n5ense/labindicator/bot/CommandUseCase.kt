package com.n0n5ense.labindicator.bot

import com.n0n5ense.labindicator.common.RoomStatus
import com.n0n5ense.labindicator.common.runIf
import com.n0n5ense.labindicator.database.dto.Status
import com.n0n5ense.labindicator.database.dto.User
import com.n0n5ense.labindicator.database.repository.StatusRepository
import com.n0n5ense.labindicator.database.repository.UserRepository
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.LoggerFactory

internal class CommandUseCase(
    private val statusBoard: StatusBoard
) {
    private val logger = LoggerFactory.getLogger("CommandUseCase")

    fun updateStatus(
        discordUserId: String,
        status: String,
        note: String?
    ): CommandResult {
        val uuid = UserRepository.getUserIdByDiscordId(discordUserId).getOrElse {
            logger.warn(it.stackTraceToString())
            return CommandResult.Failure("Server error.")
        } ?: return CommandResult.Failure("Account is not exists.")

        val roomStatus =
            RoomStatus.findValue(status) ?: return CommandResult.Failure("Invalid option. \"${status}\"")

        val newStatus = Status(
            userId = uuid,
            status = roomStatus,
            note = note
        )

        StatusRepository.add(newStatus)

        statusBoard.update(uuid)
        return CommandResult.Success("ok")
    }

    fun updateStatusToWillReturn(
        discordUserId: String,
        note: String?,
        hour: Int,
        minute: Int
    ): CommandResult {
        val uuid = UserRepository.getUserIdByDiscordId(discordUserId).getOrElse {
            logger.warn(it.stackTraceToString())
            return CommandResult.Failure("Server error.")
        } ?: return CommandResult.Failure("Account is not exists.")

        val newStatus = Status(
            userId = uuid,
            status = RoomStatus.WillReturnAt,
            note = note,
            backHour = hour,
            backMinute = minute
        )

        StatusRepository.add(newStatus)

        statusBoard.update(uuid)
        return CommandResult.Success("ok")
    }

    fun addUser(
        discordUserId: String,
        name: String,
        grade: String,
        display: Boolean
    ): CommandResult {
        UserRepository.existsByDiscordId(discordUserId).getOrElse {
            logger.warn(it.stackTraceToString())
            return CommandResult.Failure("Server error.")
        }.runIf({ it }) {
            return CommandResult.Failure("Account is already exists.")
        }
        val newUser = User(
            name = name,
            grade = grade,
            discordId = discordUserId,
            display = display
        )

        val created = UserRepository.add(
            newUser
        ).getOrElse {
            logger.warn(it.stackTraceToString())
            return CommandResult.Failure("Server error.")
        }
        StatusRepository.add(
            Status(
                userId = created.userId,
                status = RoomStatus.Unknown
            )
        )
        statusBoard.update(created.userId)
        return CommandResult.Success("Account was created.\nID: ${created.userId}")
    }

    fun updateUser(
        discordUserId: String,
        name: String?,
        grade: String?,
        display: Boolean?,
        isActive: Boolean?,
        password: String?
    ): CommandResult {
        val user = UserRepository.getByDiscordId(discordUserId).getOrElse {
            logger.warn(it.stackTraceToString())
            return CommandResult.Failure("Server error.")
        } ?: return CommandResult.Failure("Account is not exists.")

        val newPassword = password?.let { BCrypt.hashpw(it, BCrypt.gensalt()) }
        val newUser = user.copy(
            name = name ?: user.name,
            grade = grade ?: user.grade,
            display = display ?: user.display,
            isActive = isActive ?: user.isActive,
            password = newPassword ?: user.password
        )
        UserRepository.update(newUser).getOrElse {
            logger.warn(it.stackTraceToString())
            return CommandResult.Failure("Server error.")
        }.runIf({ !it }) {
            return CommandResult.Failure("Account is not exists.")
        }
        statusBoard.update(user.userId)
        return CommandResult.Success("Account info was updated.")
    }

    fun setup(channelId: Long): CommandResult {
        return statusBoard.setup(channelId)
    }
}