package com.n0n5ense.labindicator.bot

import com.n0n5ense.labindicator.common.RoomStatus
import com.n0n5ense.labindicator.common.runIf
import com.n0n5ense.labindicator.database.dto.Status
import com.n0n5ense.labindicator.database.dto.User
import com.n0n5ense.labindicator.database.repository.StatusRepository
import com.n0n5ense.labindicator.database.repository.UserRepository
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.slf4j.LoggerFactory

internal class CommandHandler(
    jda: JDA
) : CommandProcessor {
    private val logger = LoggerFactory.getLogger("CommandHandler")
    private val statusBoard = StatusBoard(jda)

    override fun updateStatus(event: SlashCommandInteractionEvent, isWillReturn: Boolean): CommandResult {
        val uuid = UserRepository.getUserIdByDiscordId(event.user.id).getOrElse {
            logger.warn(it.stackTraceToString())
            return CommandResult.Failure("Server error.")
        } ?: return CommandResult.Failure("Your account is not exists.")

        val status = event.getOption("status")?.asString?.let { RoomStatus.findValue(it) }
        status ?: return CommandResult.Failure("Invalid option. \"${event.getOption("status")?.asString}\"")

        val note = event.getOption("note")?.asString
        StatusRepository.add(
            Status(
                userId = uuid,
                status = status,
                note = note
            )
        )

        statusBoard.update()
        return CommandResult.Success("ok")
    }

    override fun willReturn(event: SlashCommandInteractionEvent): CommandResult {
        println("will return")
        return CommandResult.Success("ok")
    }

    override fun addMe(event: SlashCommandInteractionEvent): CommandResult {
        val userDiscordId = event.user.id
        UserRepository.existsByDiscordId(userDiscordId).getOrElse {
            logger.warn(it.stackTraceToString())
            return CommandResult.Failure("Server error.")
        }.runIf({ it }) {
            return CommandResult.Failure("Your account is already exists.")
        }
        val newUser = User(
            name = event.getOption("name")?.asString!!,
            grade = event.getOption("grade")?.asString!!,
            discordId = userDiscordId
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
        return CommandResult.Success("Your account was created.\nID: ${created.userId}")
    }

    override fun updateMe(event: SlashCommandInteractionEvent): CommandResult {
        val user = UserRepository.getByDiscordId(event.user.id).getOrElse {
            logger.warn(it.stackTraceToString())
            return CommandResult.Failure("Server error.")
        } ?: return CommandResult.Failure("Your account is not exists.")

        val newUser = user.copy(
            name = event.getOption("name")?.asString ?: user.name,
            grade = event.getOption("grade")?.asString ?: user.grade,
            isActive = event.getOption("enable")?.asBoolean ?: user.isActive
        )
        UserRepository.update(newUser).getOrElse {
            logger.warn(it.stackTraceToString())
            return CommandResult.Failure("Server error.")
        }.runIf({ !it }) {
            return CommandResult.Failure("Your account is not exists.")
        }
        return CommandResult.Success("Your account info was updated.")
    }

    override fun setup(event: SlashCommandInteractionEvent): CommandResult {
        return statusBoard.setup(event.channel.idLong)
    }

    private fun setupButtons() {

    }

}