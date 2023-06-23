package com.n0n5ense.labindicator.bot

import com.n0n5ense.labindicator.common.Grade
import com.n0n5ense.labindicator.common.Permissions
import com.n0n5ense.labindicator.common.RoomStatus
import com.n0n5ense.labindicator.common.runIf
import com.n0n5ense.labindicator.database.repository.UserRepository
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import org.slf4j.LoggerFactory

internal class AdminCommand(
    private val commandUseCase: CommandUseCase
) {
    private val logger = LoggerFactory.getLogger("AdminCommand")
    private val notEnoughOptionError = CommandResult.Failure("Not enough input options.")

    companion object {
        val commands = Commands.slash("lbadmin", "admin commands").apply {
            addSubcommands(
                SubcommandData("setup", "setup a status board"),
                SubcommandData("status", "update other user status").addOptions(
                    OptionData(OptionType.USER, "user", "user", true),
                    OptionData(OptionType.STRING, "status", "status", true).addChoices(
                        RoomStatus.validStatuses.map {
                            Command.Choice(it.english, it.name)
                        }
                    ),
                    OptionData(OptionType.STRING, "note", "note", false)
                ),
            )
            addSubcommandGroups(
                SubcommandGroupData("privilege", "manage privilege").addSubcommands(
                    SubcommandData("add", "add privilege").addOptions(
                        OptionData(OptionType.USER, "user", "user", true)
                    ),
                    SubcommandData("remove", "remove privilege").addOptions(
                        OptionData(OptionType.USER, "user", "user", true)
                    )
                ),
                SubcommandGroupData("user", "manage user").addSubcommands(
                    SubcommandData("add", "add user").addOptions(
                        OptionData(OptionType.USER, "user", "user", true),
                        OptionData(OptionType.STRING, "name", "name", true),
                        OptionData(OptionType.STRING, "grade", "grade", true)
                            .addChoices(Grade.values().map { Command.Choice(it.name, it.name) }),
                        OptionData(OptionType.BOOLEAN, "display", "display", false)
                    ),
                    SubcommandData("update", "update user info").addOptions(
                        OptionData(OptionType.USER, "user", "user", true),
                        OptionData(OptionType.STRING, "name", "name", false),
                        OptionData(OptionType.STRING, "grade", "grade", false)
                            .addChoices(Grade.values().map { Command.Choice(it.name, it.name) }),
                        OptionData(OptionType.BOOLEAN, "enable", "enable", false),
                        OptionData(OptionType.BOOLEAN, "display", "display", false),
                        OptionData(OptionType.STRING, "password", "password", false)
                    )
                )
            )
        }
    }

    fun handler(event: SlashCommandInteractionEvent): CommandResult {
        val hasPermission = UserRepository.hasPermissionByDiscordId(event.user.id, Permissions.Admin).getOrNull()
        if(hasPermission != true) {
            return CommandResult.Failure("You don't have permission.")
        }

        return when(event.subcommandGroup) {
            null -> when(event.subcommandName) {
                "setup" -> setupCommand(event)
                "status" -> statusCommand(event)
                else -> null
            }
            "privilege" -> privilegeCommands(event)
            "user" -> userCommands(event)
            else -> null
        } ?: CommandResult.Failure("Command not found.")
    }

    private fun setupCommand(event: SlashCommandInteractionEvent): CommandResult {
        return commandUseCase.setup(event.channel.idLong)
    }

    private fun statusCommand(event: SlashCommandInteractionEvent): CommandResult {
        val targetUser = event.getOption("user")?.asUser ?: return CommandResult.Failure("Option [user] is required.")
        return commandUseCase.updateStatus(
            discordUserId = targetUser.id,
            status = event.getOption("status")?.asString ?: return notEnoughOptionError,
            note = event.getOption("note")?.asString
        )
    }

    private fun privilegeCommands(event: SlashCommandInteractionEvent): CommandResult {
        val targetUser = event.getOption("user")?.asUser ?: return CommandResult.Failure("Option [user] is required.")
        val result = when(event.subcommandName) {
            "add" -> UserRepository.addPermissionByDiscordId(targetUser.id, Permissions.Admin)
            "remove" -> UserRepository.removePermissionByDiscordId(targetUser.id, Permissions.Admin)
            else -> return CommandResult.Failure("Unknown command.")
        }
        result.getOrElse {
            logger.warn(it.stackTraceToString())
            return CommandResult.Failure("Server error.")
        }.runIf({ !it }) {
            return CommandResult.Failure("User is not found.")
        }
        return CommandResult.Success("ok")
    }

    private fun userCommands(event: SlashCommandInteractionEvent): CommandResult {
        val targetUser = event.getOption("user")?.asUser ?: return CommandResult.Failure("Option [user] is required.")
        return when(event.subcommandName) {
            "add" -> commandUseCase.addUser(
                discordUserId = targetUser.id,
                name = event.getOption("name")?.asString ?: return notEnoughOptionError,
                grade = event.getOption("grade")?.asString ?: return notEnoughOptionError,
                display = event.getOption("display")?.asBoolean ?: true
            )
            "update" -> commandUseCase.updateUser(
                discordUserId = targetUser.id,
                name = event.getOption("name")?.asString,
                grade = event.getOption("grade")?.asString,
                display = event.getOption("display")?.asBoolean,
                isActive = event.getOption("enable")?.asBoolean,
                password = event.getOption("password")?.asString
            )
            else -> CommandResult.Failure("Unknown command.")
        }
    }

}