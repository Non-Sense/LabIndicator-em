package com.n0n5ense.labindicator.bot

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.build.Commands

class LabIndicatorBot(
    discordBotToken: String,
    private val guildId: Long
) {
    private val jda = JDABuilder.createDefault(discordBotToken).build()
    private val commandHandler = CommandHandler(jda)

    fun start() {
        jda.awaitReady()

        initLocalSlashCommand(guildId)
        jda.addEventListener(SlashCommandListener(commandHandler))

    }

    private val commands = ServerCommands.values().map {
        Commands.slash(it.commandName, it.description).apply {
            addOptions(it.options)
            addSubcommands(it.subCommands)
            it.descriptionJp?.let { d -> setDescriptionLocalization(DiscordLocale.JAPANESE, d) }
        }
    }

    private fun initLocalSlashCommand(guildId: Long) {
        val guild = jda.getGuildById(guildId) ?: return
        guild.updateCommands().addCommands(commands).queue()
    }

}

internal interface CommandProcessor {
    fun updateStatus(event: SlashCommandInteractionEvent)
    fun willReturn(event: SlashCommandInteractionEvent)
    fun addMe(event: SlashCommandInteractionEvent)
    fun updateMe(event: SlashCommandInteractionEvent)
    fun setup(event: SlashCommandInteractionEvent)
}

private class SlashCommandListener(
    private val commandProcessor: CommandProcessor
) : ListenerAdapter() {
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val command = ServerCommands.values().find { event.name == it.commandName } ?: return
        when (command) {
            ServerCommands.S,
            ServerCommands.STATUS -> {
                commandProcessor.updateStatus(event)
            }
            ServerCommands.WILL_RETURN -> {
                commandProcessor.willReturn(event)
            }
            ServerCommands.ADD_ME -> {
                commandProcessor.addMe(event)
            }
            ServerCommands.UPDATE_ME -> {
                commandProcessor.updateMe(event)
            }
            ServerCommands.SETUP -> {
                commandProcessor.setup(event)
            }
        }
    }
}