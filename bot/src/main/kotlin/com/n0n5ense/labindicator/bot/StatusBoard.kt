package com.n0n5ense.labindicator.bot

import com.n0n5ense.labindicator.common.ButtonChannelIdKeyName
import com.n0n5ense.labindicator.common.ButtonMessageIdKeyName
import com.n0n5ense.labindicator.common.ChannelIdKeyName
import com.n0n5ense.labindicator.common.RoomStatus
import com.n0n5ense.labindicator.database.dto.StatusMessage
import com.n0n5ense.labindicator.database.dto.StatusToDisplay
import com.n0n5ense.labindicator.database.repository.ConfigRepository
import com.n0n5ense.labindicator.database.repository.StatusMessageRepository
import com.n0n5ense.labindicator.database.repository.StatusRepository
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.slf4j.LoggerFactory
import java.awt.Color
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class StatusBoard(
    private val jda: JDA
) {

    private val logger = LoggerFactory.getLogger("StatusBoard")

    fun setup(channelId: Long): CommandResult {
        val channel = jda.getTextChannelById(channelId) ?: kotlin.run {
            return CommandResult.Failure("Channel not found.")
        }
        val userStatuses = StatusRepository.getLatest().getOrElse {
            logger.warn(it.toString())
            return CommandResult.Failure("Server error.")
        }

        deleteExistsMessages()

        ConfigRepository.set(ChannelIdKeyName, channelId.toString())
        userStatuses.forEachIndexed { index, status ->
            val showData = ShowData.fromStatusToDisplay(status)
            channel.sendMessageEmbeds(makeEmbeddedMessage(showData)).queue {
                val s = StatusMessage(
                    index = index,
                    messageId = it.idLong
                )
                StatusMessageRepository.add(s)
            }
        }

        addButton(channelId)

        return CommandResult.Success("ok")
    }

    private fun addButton(channelId: Long): CommandResult {
        val channel = jda.getTextChannelById(channelId) ?: kotlin.run {
            return CommandResult.Failure("Channel not found.")
        }

        val existsChannel = ConfigRepository.get(ButtonChannelIdKeyName).getOrNull()
        val existsMessage = ConfigRepository.get(ButtonMessageIdKeyName).getOrNull()
        if (existsChannel != null && existsMessage != null) {
            jda.getTextChannelById(existsChannel)?.apply {
                kotlin.runCatching { deleteMessageById(existsMessage) }
            }
        }

        channel.sendMessage("Update your status")
            .addActionRow(
                Button.success(RoomStatus.InRoom.name, RoomStatus.InRoom.japanese),
                Button.primary(RoomStatus.AroundHere.name, RoomStatus.AroundHere.japanese),
                Button.primary(RoomStatus.Lecture.name, RoomStatus.Lecture.japanese),
                Button.primary(RoomStatus.Library.name, RoomStatus.Library.japanese),
            )
            .addActionRow(
                Button.primary(RoomStatus.Meal.name, RoomStatus.Meal.japanese),
                Button.primary(RoomStatus.RightBack.name, RoomStatus.RightBack.japanese),
                Button.primary(RoomStatus.Exercise.name, RoomStatus.Exercise.japanese),
                Button.danger(RoomStatus.Home.name, RoomStatus.Home.japanese),
            )
            .queue {
                ConfigRepository.set(ButtonChannelIdKeyName, channelId.toString())
                ConfigRepository.set(ButtonMessageIdKeyName, it.id)
            }

        return CommandResult.Success("ok")
    }

    private fun deleteExistsMessages(): CommandResult {
        val channelId = ConfigRepository.get(ChannelIdKeyName).getOrElse {
            logger.warn(it.toString())
            return CommandResult.Failure("Server error.")
        } ?: return CommandResult.Failure("Channel not found.")

        val channel = jda.getTextChannelById(channelId) ?: return CommandResult.Failure("Channel not found.")
        val messageIds = StatusMessageRepository.getAll().getOrElse {
            logger.warn(it.toString())
            return CommandResult.Failure("Server error.")
        }.map {
            it.messageId.toString()
        }
        kotlin.runCatching {
            when (messageIds.size) {
                0 -> {}
                1 -> channel.deleteMessageById(messageIds.first()).queue()
                else -> channel.deleteMessagesByIds(messageIds).queue()
            }
        }
        return CommandResult.Success("ok")
    }

    fun update(): CommandResult {
        val messages = StatusMessageRepository.getAll().getOrElse {
            logger.warn(it.stackTraceToString())
            return CommandResult.Failure("Server error.")
        }
        val userStatuses = StatusRepository.getLatest().getOrElse {
            logger.warn(it.toString())
            return CommandResult.Failure("Server error.")
        }
        val channelId = ConfigRepository.get(ChannelIdKeyName).getOrNull()
            ?: return CommandResult.Failure("Your status is updated. But status board is not setup. run /lbadmin setup")

        if (messages.size != userStatuses.size) {
            logger.info("setup messages: messages size = ${messages.size}, users size = ${userStatuses.size}")
            return setup(channelId.toLong())
        }

        val channel = jda.getTextChannelById(channelId) ?: return CommandResult.Failure("Channel not found")

        messages.zip(userStatuses).forEach {
            channel.editMessageEmbedsById(
                it.first.messageId,
                makeEmbeddedMessage(ShowData.fromStatusToDisplay(it.second))
            ).queue()
        }
        return CommandResult.Success("ok")
    }
}

private data class ShowData(
    val grade: String,
    val name: String,
    val status: RoomStatus,
    val time: Instant,
    val note: String?,
    val hour: Int? = null,
    val minute: Int? = null
) {
    companion object {
        fun fromStatusToDisplay(status: StatusToDisplay): ShowData {
            return ShowData(
                grade = status.user.grade,
                name = status.user.name,
                status = status.status,
                time = status.time,
                note = status.note,
                hour = status.backHour,
                minute = status.backMinute
            )
        }
    }
}

private fun makeEmbeddedMessage(status: ShowData): MessageEmbed {
    val emoji = getEmoji(status)
    val color = when (status.status) {
        RoomStatus.InRoom -> Color(0x66bb6a)
        RoomStatus.AroundHere,
        RoomStatus.Lecture,
        RoomStatus.Library,
        RoomStatus.Meal,
        RoomStatus.WillReturnAt,
        RoomStatus.Exercise -> Color(0xf57c00)
        RoomStatus.RightBack -> Color(0xffb74d)
        RoomStatus.Home -> Color(0xe57373)
        RoomStatus.Unknown -> Color(0x101010)
    }
    return EmbedBuilder().apply {
        addField("`${status.grade}`  ${status.name}", "", false)
        addField("$emoji\t${status.status.japanese}", "", false)
        status.note?.let {
            addField(it, "", false)
        }
        setColor(color)
        setFooter(status.time.toShortDisplayString())
    }.build()
}

private fun makeStatusString(showData: ShowData): String {
    val lang = when (showData.status) {
        RoomStatus.InRoom -> "yaml"
        RoomStatus.Home -> "brainfuck"
        else -> ""
    }
    if (showData.status == RoomStatus.WillReturnAt) {
        val str = RoomStatus.getWillReturnDisplayString(showData.hour ?: -1, showData.minute ?: -1)
        return """```$str```"""
    }
    return """```$lang
            |${showData.status.japanese}
            |```""".trimMargin()
}


private val shortDateFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")
private fun Instant.toShortDisplayString(): String {
    return this.atZone(ZoneId.of("Asia/Tokyo")).format(shortDateFormatter)
}

private fun getEmoji(showData: ShowData): String {
    fun getReturnTimeEmoji(showData: ShowData): String {
        showData.hour ?: return "\uD83D\uDD70️"
        showData.minute ?: return "\uD83D\uDD70️"
        return when (showData.hour.mod(12) + if (showData.minute >= 30) 12 else 0) {
            0 -> "\uD83D\uDD5B"
            1 -> "\uD83D\uDD50"
            2 -> "\uD83D\uDD51"
            3 -> "\uD83D\uDD52"
            4 -> "\uD83D\uDD53"
            5 -> "\uD83D\uDD54"
            6 -> "\uD83D\uDD55"
            7 -> "\uD83D\uDD56"
            8 -> "\uD83D\uDD57"
            9 -> "\uD83D\uDD58"
            10 -> "\uD83D\uDD59"
            11 -> "\uD83D\uDD5A"
            12 -> "\uD83D\uDD67"
            13 -> "\uD83D\uDD5C"
            14 -> "\uD83D\uDD5D"
            15 -> "\uD83D\uDD5E"
            16 -> "\uD83D\uDD5F"
            17 -> "\uD83D\uDD60"
            18 -> "\uD83D\uDD61"
            19 -> "\uD83D\uDD62"
            20 -> "\uD83D\uDD63"
            21 -> "\uD83D\uDD64"
            22 -> "\uD83D\uDD65"
            23 -> "\uD83D\uDD66"
            else -> "\uD83D\uDD70"
        }
    }
    return when (showData.status) {
        RoomStatus.InRoom -> "\uD83D\uDCBB"
        RoomStatus.AroundHere -> "\uD83D\uDEB6"
        RoomStatus.Lecture -> "\uD83D\uDC68\u200D\uD83C\uDFEB"
        RoomStatus.Library -> "\uD83D\uDCDA"
        RoomStatus.Meal -> "\uD83C\uDF7D️"
        RoomStatus.RightBack -> "\uD83D\uDCA8"
        RoomStatus.Exercise -> "\uD83D\uDCAA"
        RoomStatus.Home -> "\uD83C\uDFE0"
        RoomStatus.WillReturnAt -> getReturnTimeEmoji(showData)
        RoomStatus.Unknown -> "\uD83D\uDC77"
    }
}