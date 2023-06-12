package com.n0n5ense.labindicator.bot

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
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.slf4j.LoggerFactory
import java.awt.Color
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class StatusBoardRow(
    val messageId: Long
)

class StatusBoard(
    private val jda: JDA
) {

    private val logger = LoggerFactory.getLogger("StatusBoard")

    fun setup(channelId: Long, event: SlashCommandInteractionEvent) {
        val channel = jda.getTextChannelById(channelId) ?: kotlin.run {
            event.reply("Channel not found").setEphemeral(true).queue()
            return
        }
        val userStatuses = StatusRepository.getLatest().getOrElse {
            logger.warn(it.toString())
            event.reply("Server error.").setEphemeral(true).queue()
            return
        }

        deleteExistsMessages()

        ConfigRepository.set(ChannelIdKeyName, channelId.toString())
        userStatuses.forEachIndexed { index, status ->
            val showData = ShowData.fromStatusToDisplay(status)
//            channel.sendMessage(makeTextMessage(showData)).queue {
//                StatusMessage(index, it.idLong)
//            }
            channel.sendMessageEmbeds(makeEmbeddedMessage(showData)).queue {
                val s = StatusMessage(
                    index = index,
                    messageId = it.idLong
                )
                StatusMessageRepository.add(s)
            }
        }
    }

    private fun deleteExistsMessages() {
        val channelId = ConfigRepository.get(ChannelIdKeyName).getOrElse {
            logger.warn(it.toString())
            return
        } ?: return

        val channel = jda.getTextChannelById(channelId) ?: return
        val messageIds = StatusMessageRepository.getAll().getOrElse {
            logger.warn(it.toString())
            return
        }.map {
            it.messageId.toString()
        }
        channel.deleteMessagesByIds(messageIds).queue()
    }

    fun update(event: SlashCommandInteractionEvent) {
        val messages = StatusMessageRepository.getAll().getOrElse {
            logger.warn(it.stackTraceToString())
            event.reply("Server error.").setEphemeral(true).queue()
            return
        }
        val userStatuses = StatusRepository.getLatest().getOrElse {
            logger.warn(it.toString())
            event.reply("Server error.").setEphemeral(true).queue()
            return
        }
        val channelId = ConfigRepository.get(ChannelIdKeyName).getOrNull()
        if (channelId == null) {
            if(!event.isAcknowledged)
                event.reply("Status board is not setup. run /setup").setEphemeral(true).queue()
            return
        }
        if (messages.size != userStatuses.size) {
            logger.info("setup messages: messages size = ${messages.size}, users size = ${userStatuses.size}")
            setup(channelId.toLong(), event)
            return
        }

        val channel = jda.getTextChannelById(channelId)
        if (channel == null) {
            event.reply("Channel not found").setEphemeral(true).queue()
            return
        }
        messages.zip(userStatuses).forEach {
            channel.editMessageEmbedsById(
                it.first.messageId,
                makeEmbeddedMessage(ShowData.fromStatusToDisplay(it.second))
            ).queue()
        }
    }
}

private data class ShowData(
    val grade: String,
    val name: String,
    val status: RoomStatus,
    val time: Instant,
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
//        addField(status.grade, "", true)
        addField("`${status.grade}`  ${status.name}", "", false)
        addField("$emoji\t${status.status.japanese}", "", false)

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

private fun makeTextMessage(showData: ShowData): String {
    val time = showData.time.toShortDisplayString()
    val s = makeStatusString(showData)
    return "${getEmoji(showData)}  `${showData.grade}` ${showData.name} \t`$time`\n$s\n"
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
            else -> "\uD83D\uDD70️"
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