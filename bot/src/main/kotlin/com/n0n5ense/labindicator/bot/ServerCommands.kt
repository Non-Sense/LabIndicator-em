package com.n0n5ense.labindicator.bot

import com.n0n5ense.labindicator.common.Grade
import com.n0n5ense.labindicator.common.RoomStatus
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
        description = "Update your status",
        descriptionJp = "入室状態を更新します",
        options = listOf(
            OptionData(
                OptionType.STRING,
                "status",
                "Your status",
                true
            )
                .setNameLocalization(DiscordLocale.JAPANESE, "状態")
                .setDescriptionLocalization(DiscordLocale.JAPANESE, "在室状態")
                .addChoices(
                    RoomStatus.values()
                        .filter { it != RoomStatus.WillReturnAt && it != RoomStatus.Unknown }
                        .map { Choice(it.alias, it.name) }),
            OptionData(
                OptionType.STRING,
                "note",
                "Note",
                false
            )
                .setNameLocalization(DiscordLocale.JAPANESE, "メモ")
                .setDescriptionLocalization(DiscordLocale.JAPANESE, "メモ書き")
        )
    ),
    STATUS(
        commandName = "status",
        description = "Update your status",
        descriptionJp = "入室状態を更新します",
        options = listOf(
            OptionData(
                OptionType.STRING,
                "status",
                "Your status",
                true
            )
                .setNameLocalization(DiscordLocale.JAPANESE, "状態")
                .setDescriptionLocalization(DiscordLocale.JAPANESE, "在室状態")
                .addChoices(
                    RoomStatus.values()
                        .filter { it != RoomStatus.WillReturnAt && it != RoomStatus.Unknown }
                        .map {
                            Choice(it.english, it.name).setNameLocalization(DiscordLocale.JAPANESE, it.japanese)
                        }
                ),
            OptionData(
                OptionType.STRING,
                "note",
                "Note",
                false
            )
                .setNameLocalization(DiscordLocale.JAPANESE, "メモ")
                .setDescriptionLocalization(DiscordLocale.JAPANESE, "メモ書き")
        )
    ),
    WILL_RETURN(
        commandName = "will-return",
        description = """Set status to "Will return at"""",
        descriptionJp = "「何時に戻る」状態に設定します",
        options = listOf(
            OptionData(
                OptionType.INTEGER,
                "hour",
                "Hour(0~23)",
                true
            )
                .setDescriptionLocalization(DiscordLocale.JAPANESE, "時(0~23)")
                .addChoices((0L..23L).map { Choice(it.toString(), it) }),
            OptionData(
                OptionType.INTEGER,
                "minute",
                "Minute(0 or 30)",
                true
            )
                .setDescriptionLocalization(DiscordLocale.JAPANESE, "分(0か30)")
                .addChoices(Choice("0", 0), Choice("30", 30)),
            OptionData(
                OptionType.STRING,
                "note",
                "Note",
                false
            )
                .setNameLocalization(DiscordLocale.JAPANESE, "メモ")
                .setDescriptionLocalization(DiscordLocale.JAPANESE, "メモ書き")
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
                .setDescriptionLocalization(DiscordLocale.JAPANESE, "本名(表示される名前)"),
            OptionData(
                OptionType.STRING,
                "grade",
                "your grade",
                true,
            )
                .setDescriptionLocalization(DiscordLocale.JAPANESE, "学年")
                .addChoices(Grade.values().map { Choice(it.name, it.name) })
        )
    ),
    UPDATE_ME(
        commandName = "update-me",
        description = "update your account info",
        descriptionJp = "ユーザ情報を更新します",
        options = listOf(
            OptionData(
                OptionType.STRING,
                "name",
                "your real name (to display name)",
                false
            )
                .setDescriptionLocalization(DiscordLocale.JAPANESE, "本名(表示される名前)"),
            OptionData(
                OptionType.STRING,
                "grade",
                "your grade",
                false,
            )
                .setDescriptionLocalization(DiscordLocale.JAPANESE, "学年")
                .addChoices(Grade.values().map { Choice(it.name, it.name) }),
            OptionData(
                OptionType.BOOLEAN,
                "enable",
                "enable/disable your account",
                false
            )
                .setDescriptionLocalization(DiscordLocale.JAPANESE, "アカウントの有効/無効化")
        )
    ),
    SETUP(
        commandName = "setup",
        description = "test"
    )
    ;

}