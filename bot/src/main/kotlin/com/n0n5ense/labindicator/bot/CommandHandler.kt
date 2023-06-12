package com.n0n5ense.labindicator.bot

import com.n0n5ense.labindicator.common.runIf
import com.n0n5ense.labindicator.database.ConflictException
import com.n0n5ense.labindicator.database.dto.User
import com.n0n5ense.labindicator.database.repository.UserRepository
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.slf4j.LoggerFactory

internal class CommandHandler(
    private val jda: JDA
): CommandProcessor {
    private val logger = LoggerFactory.getLogger("CommandHandler")

    override fun updateStatus(event: SlashCommandInteractionEvent) {
        println("update status")
    }

    override fun willReturn(event: SlashCommandInteractionEvent) {
        println("will return")
    }

    override fun addMe(event: SlashCommandInteractionEvent) {
        val userDiscordId = event.user.id
        UserRepository.existsByDiscordId(userDiscordId).getOrElse {
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
        event.reply("Your account was created.\nID: ${created.userId}").setEphemeral(true).queue()
    }

    override fun updateMe(event: SlashCommandInteractionEvent) {
        val user = UserRepository.getByDiscordId(event.user.id).getOrElse {
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
            event.reply("Server error.").setEphemeral(true).queue()
            return
        }.runIf({!it}) {
            event.reply("Your account is not exists.").setEphemeral(true).queue()
            return
        }
        event.reply("Your account info was updated.").setEphemeral(true).queue()
    }

    override fun setup(event: SlashCommandInteractionEvent) {
        event.channel.idLong
    }

}