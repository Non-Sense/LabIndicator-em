package com.n0n5ense.labindicator.bot

import com.n0n5ense.labindicator.common.RoomStatus
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

internal enum class ServerCommands(
    val commandName: String,
    val description: String,
    val descriptionJp: String? = null,
    val options: List<OptionData> = listOf(),
    val subCommands: List<SubcommandData> = listOf()
) {
    S(
        commandName = "s",
        description = StringAsset.updateCommandDescription,
        descriptionJp = StringAsset.updateCommandDescriptionJp,
        options = listOf(
            OptionData(
                OptionType.STRING,
                StringAsset.updateOptionName,
                StringAsset.updateOptionDescription,
                true
            )
                .setNameLocalization(DiscordLocale.JAPANESE, StringAsset.updateOptionNameJp)
                .setDescriptionLocalization(DiscordLocale.JAPANESE, StringAsset.updateOptionDescriptionJp)
                .addChoices(RoomStatus.values().filter { it != RoomStatus.WillReturnAt }
                    .map { Choice(it.alias, it.name) })
        )
    ),
    STATUS(
        commandName = "status",
        description = StringAsset.updateCommandDescription,
        descriptionJp = StringAsset.updateCommandDescriptionJp,
        options = listOf(
            OptionData(
                OptionType.STRING,
                StringAsset.updateOptionName,
                StringAsset.updateOptionDescription,
                true
            )
                .setNameLocalization(DiscordLocale.JAPANESE, StringAsset.updateOptionNameJp)
                .setDescriptionLocalization(DiscordLocale.JAPANESE, StringAsset.updateOptionDescriptionJp)
                .addChoices(RoomStatus.values().filter { it != RoomStatus.WillReturnAt }.map {
                    Choice(it.english, it.name).setNameLocalization(DiscordLocale.JAPANESE, it.japanese)
                })
        )
    ),
    WILL_RETURN(
        commandName = "will-return",
        description = StringAsset.willReturnCommandDescription,
        descriptionJp = StringAsset.willReturnCommandDescriptionJp,
        options = listOf(
            OptionData(
                OptionType.INTEGER,
                StringAsset.hour,
                StringAsset.descriptionHour,
                true
            )
                .setDescriptionLocalization(DiscordLocale.JAPANESE, StringAsset.descriptionHourJp)
                .addChoices((0L..23L).map { Choice(it.toString(), it) }),
            OptionData(
                OptionType.INTEGER,
                StringAsset.minute,
                StringAsset.descriptionMinute,
                true
            )
                .setDescriptionLocalization(DiscordLocale.JAPANESE, StringAsset.descriptionMinuteJp)
                .addChoices(Choice("0", 0), Choice("30", 30)),
        )
    ),
    ADD_ME(
        commandName = "add-me",
        description = "add you to LabIndicator user",
        descriptionJp = "インジケータのユーザとして登録します",
        options = listOf(
            OptionData(
                OptionType.STRING,
                "name",
                "your real name (to display name)",
                true
            )
                .setDescriptionLocalization(DiscordLocale.JAPANESE, "本名(表示される名前)")
        )
    )
    ;

    sealed class MemberCommand {
        companion object {
            private val genericError = Error(StringAsset.memberCommandGenericError)

            fun parseEvent(event: SlashCommandInteractionEvent): MemberCommand {
                return when(event.subcommandName) {
                    StringAsset.memberAddSubCommandName -> Add.parseEvent(event)
                    StringAsset.memberRemoveSubCommandName -> Remove.parseEvent(event)
                    StringAsset.memberUpdateSubCommandName -> Update.parseEvent(event)
                    else -> Error("Unknown command")
                }
            }
        }

        data class Error(
            val massage: String
        ): MemberCommand()

        data class Add(
            val user: User,
            val name: String,
            val order: Int
        ): MemberCommand() {
            companion object {
                fun parseEvent(event: SlashCommandInteractionEvent): MemberCommand {
                    val user = event.getOption(StringAsset.memberUserOptionName)?.asUser ?: return genericError
                    val name = event.getOption(StringAsset.memberNameOptionName)?.asString ?: return genericError
                    val order = event.getOption(StringAsset.memberOrderOptionName)?.asInt ?: return genericError
                    return Add(user, name, order)
                }
            }
        }

        data class Remove(
            val user: User
        ): MemberCommand() {
            companion object {
                fun parseEvent(event: SlashCommandInteractionEvent): MemberCommand {
                    val user = event.getOption(StringAsset.memberUserOptionName)?.asUser ?: return genericError
                    return Remove(user)
                }
            }
        }

        data class Update(
            val user: User,
            val name: String?,
            val order: Int?
        ): MemberCommand() {
            companion object {
                fun parseEvent(event: SlashCommandInteractionEvent): MemberCommand {
                    val user = event.getOption(StringAsset.memberUserOptionName)?.asUser ?: return genericError
                    val name = event.getOption(StringAsset.memberNameOptionName)?.asString
                    val order = event.getOption(StringAsset.memberOrderOptionName)?.asInt
                    return Update(user, name, order)
                }
            }
        }
    }
}