package com.n0n5ense.labindicator.bot

import net.dv8tion.jda.api.JDABuilder

class LabIndicatorBot(
    discordBotToken: String
) {
    private val jda = JDABuilder.createDefault(discordBotToken).build()

    fun start() {
        jda.awaitReady()

    }

}