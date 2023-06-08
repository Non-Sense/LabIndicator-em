package com.n0n5ense.labindicator.bot

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

internal class UserCommandHandler(
    private val jda: JDA
) {

    fun addUser(event: SlashCommandInteractionEvent) {
        val userDiscordId = event.user.id


    }

}