package com.n0n5ense.labindicator.bot

import com.n0n5ense.labindicator.common.runIf
import com.n0n5ense.labindicator.database.dto.User
import com.n0n5ense.labindicator.database.repository.UserRepository
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

internal class CommandHandler(
    private val jda: JDA
): CommandProcessor {
    override fun updateStatus(event: SlashCommandInteractionEvent) {
        println("update status")
    }

    override fun willReturn(event: SlashCommandInteractionEvent) {
        println("will return")
    }

    override fun addMe(event: SlashCommandInteractionEvent) {
        val userDiscordId = event.user.id
        UserRepository.existsByDiscordId(userDiscordId).getOrElse {
            event.reply("Server error").setEphemeral(true).queue()
            return
        }.runIf({ it }) {
            event.reply("You are already added").setEphemeral(true).queue()
            return
        }
        val newUser = User(
            name = event.getOption("name")?.asString!!,
            grade = event.getOption("grade")?.asString!!,
            discordId = userDiscordId
        )

        UserRepository.add(
            newUser
        )

    }

}