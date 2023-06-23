package com.n0n5ense.labindicator.bot

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.build.Commands

class LabIndicatorBot(
    discordBotToken: String,
    private val guildId: Long
) {
    private val jda = JDABuilder.createDefault(discordBotToken).build()
    private val commandUseCase = CommandUseCase(StatusBoard(jda))
    private val adminCommand = AdminCommand(commandUseCase)
    private val notEnoughOptionError = CommandResult.Failure("Not enough input options.")

    fun start() {
        jda.awaitReady()

        initLocalSlashCommand(guildId)
        jda.addEventListener(listener)

    }

    private val commands = ServerCommands.values().map {
        Commands.slash(it.commandName, it.description).apply {
            addOptions(it.options)
            addSubcommands(it.subCommands)
            it.descriptionJp?.let { d -> setDescriptionLocalization(DiscordLocale.JAPANESE, d) }
        }
    } + AdminCommand.commands

    private fun initLocalSlashCommand(guildId: Long) {
        val guild = jda.getGuildById(guildId) ?: return
        guild.updateCommands().addCommands(commands).queue()
    }

    private fun handleCommandEvent(event: SlashCommandInteractionEvent): CommandResult? {
        val userId = event.user.id
        val result = when(ServerCommands.values().find { event.name == it.commandName }) {
            ServerCommands.S,
            ServerCommands.STATUS -> {
                commandUseCase.updateStatus(
                    discordUserId = userId,
                    status = event.getOption("status")?.asString ?: return notEnoughOptionError,
                    note = event.getOption("note")?.asString
                )
            }
            ServerCommands.WILL_RETURN -> {
                commandUseCase.updateStatusToWillReturn(
                    discordUserId = userId,
                    hour = event.getOption("hour")?.asInt ?: return notEnoughOptionError,
                    minute = event.getOption("minute")?.asInt ?: return notEnoughOptionError,
                    note = event.getOption("note")?.asString
                )
            }
            ServerCommands.ADD_ME -> {
                commandUseCase.addUser(
                    discordUserId = userId,
                    name = event.getOption("name")?.asString ?: return notEnoughOptionError,
                    grade = event.getOption("grade")?.asString ?: return notEnoughOptionError,
                    display = true
                )
            }
            ServerCommands.UPDATE_ME -> {
                commandUseCase.updateUser(
                    discordUserId = userId,
                    name = event.getOption("name")?.asString,
                    grade = event.getOption("grade")?.asString,
                    display = true,
                    isActive = event.getOption("enable")?.asBoolean,
                    password = null
                )
            }
            null -> {
                if(event.name == "lbadmin") {
                    adminCommand.handler(event)
                } else {
                    null
                }
            }
        }
        return result
    }

    private fun handleButtonEvent(event: ButtonInteractionEvent): CommandResult {
        return commandUseCase.updateStatus(
            discordUserId = event.user.id,
            status = event.interaction.button.id ?: return notEnoughOptionError,
            note = null
        )
    }

    private val listener = object: ListenerAdapter() {
        override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
            when(val result = handleCommandEvent(event)) {
                is CommandResult.Failure -> event.reply(result.message).setEphemeral(true).queue()
                is CommandResult.Success -> event.reply(result.message).setEphemeral(true).queue()
                null -> event.reply("Command not found.").setEphemeral(true).queue()
            }
        }

        override fun onButtonInteraction(event: ButtonInteractionEvent) {
            when(val result = handleButtonEvent(event)) {
                is CommandResult.Failure -> event.reply(result.message).setEphemeral(true).queue()
                is CommandResult.Success -> event.deferEdit().queue()
            }
        }
    }

}
