package com.n0n5ense.labindicator.bot

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

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
    } + Commands.slash("lbadmin", "admin commands").apply {
        addSubcommands(SubcommandData("setup", "setup a status board"))
        addSubcommandGroups()
    }

    private fun initLocalSlashCommand(guildId: Long) {
        val guild = jda.getGuildById(guildId) ?: return
        guild.updateCommands().addCommands(commands).queue()
    }

}

internal interface CommandProcessor {
    fun updateStatus(event: SlashCommandInteractionEvent, isWillReturn: Boolean): CommandResult
    fun willReturn(event: SlashCommandInteractionEvent): CommandResult
    fun addMe(event: SlashCommandInteractionEvent): CommandResult
    fun updateMe(event: SlashCommandInteractionEvent): CommandResult
    fun setup(event: SlashCommandInteractionEvent): CommandResult
}

private class SlashCommandListener(
    private val commandProcessor: CommandProcessor
) : ListenerAdapter() {
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val command = ServerCommands.values().find { event.name == it.commandName } ?: return
        val result = when (command) {
            ServerCommands.S,
            ServerCommands.STATUS -> {
                commandProcessor.updateStatus(event, isWillReturn = false)
            }
            ServerCommands.WILL_RETURN -> {
                commandProcessor.updateStatus(event, isWillReturn = true)
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

        when(result) {
            is CommandResult.Failure -> event.reply(result.message).setEphemeral(true).queue()
            is CommandResult.Success -> event.reply(result.message).setEphemeral(true).queue()
        }
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        println(event)
        println(event.interaction.button.id)
        event.reply("ok").setEphemeral(true).queue()
    }
}