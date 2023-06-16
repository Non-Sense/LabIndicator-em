package com.n0n5ense.labindicator.bot

import com.n0n5ense.labindicator.common.RoomStatus
import com.n0n5ense.labindicator.common.runIf
import com.n0n5ense.labindicator.database.ConflictException
import com.n0n5ense.labindicator.database.dto.Status
import com.n0n5ense.labindicator.database.dto.User
import com.n0n5ense.labindicator.database.repository.StatusRepository
import com.n0n5ense.labindicator.database.repository.UserRepository
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.slf4j.LoggerFactory

internal class CommandHandler(
    private val jda: JDA
): CommandProcessor {
    private val logger = LoggerFactory.getLogger("CommandHandler")
    private val statusBoard = StatusBoard(jda)

    override fun updateStatus(event: SlashCommandInteractionEvent, isWillReturn: Boolean) {
        val uuid = UserRepository.getUserIdByDiscordId(event.user.id).getOrElse {
            logger.warn(it.stackTraceToString())
            event.reply("Server error.").setEphemeral(true).queue()
            return
        }
        if(uuid == null) {
            event.reply("Your account is not exists.").setEphemeral(true).queue()
            return
        }
        val status = event.getOption("status")?.asString?.let { RoomStatus.findValue(it) }
        if(status == null) {
            event.reply("Invalid option. \"${event.getOption("status")?.asString}\"").setEphemeral(true).queue()
            return
        }
        val note = event.getOption("note")?.asString
        StatusRepository.add(Status(
            userId = uuid,
            status = status,
            note = note
        ))
        event.reply("ok").setEphemeral(true).queue()
        statusBoard.update(event)
    }

    override fun willReturn(event: SlashCommandInteractionEvent) {
        println("will return")
    }

    override fun addMe(event: SlashCommandInteractionEvent) {
        val userDiscordId = event.user.id
        UserRepository.existsByDiscordId(userDiscordId).getOrElse {
            logger.warn(it.stackTraceToString())
            event.reply("Server error.").setEphemeral(true).queue()
            return
        }.runIf({ it }) {
            event.reply("Your account is already exists.").setEphemeral(true).queue()
            return
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
            event.reply("Server error.").setEphemeral(true).queue()
            return
        }
        StatusRepository.add(Status(
            userId = created.userId,
            status = RoomStatus.Unknown
        ))
        event.reply("Your account was created.\nID: ${created.userId}").setEphemeral(true).queue()
    }

    override fun updateMe(event: SlashCommandInteractionEvent) {
        val user = UserRepository.getByDiscordId(event.user.id).getOrElse {
            logger.warn(it.stackTraceToString())
            event.reply("Server error.").setEphemeral(true).queue()
            return
        }
        if(user == null) {
            event.reply("Your account is not exists.").setEphemeral(true).queue()
            return
        }

        val newUser = user.copy(
            name = event.getOption("name")?.asString ?: user.name,
            grade = event.getOption("grade")?.asString ?: user.grade,
            isActive = event.getOption("enable")?.asBoolean ?: user.isActive
        )
        UserRepository.update(newUser).getOrElse {
            logger.warn(it.stackTraceToString())
            event.reply("Server error.").setEphemeral(true).queue()
            return
        }.runIf({!it}) {
            event.reply("Your account is not exists.").setEphemeral(true).queue()
            return
        }
        event.reply("Your account info was updated.").setEphemeral(true).queue()
    }

    override fun setup(event: SlashCommandInteractionEvent) {
        statusBoard.setup(event.channel.idLong, event)
        event.reply("ok").setEphemeral(true).queue()
    }

    private fun setupButtons() {

    }

}