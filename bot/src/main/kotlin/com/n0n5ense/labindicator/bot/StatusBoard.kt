package com.n0n5ense.labindicator.bot

import com.n0n5ense.labindicator.common.*
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

internal class StatusBoard(
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
        userStatuses
            .sortedBy { kotlin.runCatching { Grade.valueOf(it.user.grade) }.getOrNull()?.ordinal ?: Int.MAX_VALUE }
            .forEachIndexed { index, status ->
                val showData = ShowData.fromStatusToDisplay(status)
                channel.sendMessageEmbeds(makeEmbeddedMessage(showData)).queue {
                    val s = StatusMessage(
                        index = index,
                        messageId = it.idLong
                    )
                    StatusMessageRepository.add(s)
                }
            }

        setupButton(channelId)

        return CommandResult.Success("ok")
    }

    private fun setupButton(channelId: Long): CommandResult {
        val channel = jda.getTextChannelById(channelId) ?: kotlin.run {
            return CommandResult.Failure("Channel not found.")
        }

        val existsChannel = ConfigRepository.get(ButtonChannelIdKeyName).getOrNull()
        val existsMessage = ConfigRepository.get(ButtonMessageIdKeyName).getOrNull()
        if(existsChannel != null && existsMessage != null) {
            jda.getTextChannelById(existsChannel)?.apply {
                kotlin.runCatching { deleteMessageById(existsMessage).queue() }.onFailure {
                    logger.warn(it.stackTraceToString())
                }
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
            when(messageIds.size) {
                0 -> {}
                1 -> channel.deleteMessageById(messageIds.first()).queue()
                else -> channel.deleteMessagesByIds(messageIds).queue()
            }
        }
        StatusMessageRepository.deleteAll()
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

        if(messages.size != userStatuses.size) {
            logger.info("setup messages: messages size = ${messages.size}, users size = ${userStatuses.size}")
            return setup(channelId.toLong())
        }

        val channel = jda.getTextChannelById(channelId) ?: return CommandResult.Failure("Channel not found")
        val sortedUserStatuses = userStatuses
            .sortedBy { kotlin.runCatching { Grade.valueOf(it.user.grade) }.getOrNull()?.ordinal ?: Int.MAX_VALUE }
        messages.zip(sortedUserStatuses).forEach {
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
    val color = when(status.status) {
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
    val statusString = if(status.status == RoomStatus.WillReturnAt) {
        "${status.hour}時${status.minute}に戻る"
    } else {
        status.status.japanese
    }
    return EmbedBuilder().apply {
        addField("`${status.grade}`  ${status.name}", "", false)
        addField("$emoji\t$statusString", "", false)
        status.note?.let {
            addField(it, "", false)
        }
        setColor(color)
        setFooter("updated at ${status.time.toShortDisplayString()}")
    }.build()
}

private val shortDateFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")
private fun Instant.toShortDisplayString(): String {
    return this.atZone(ZoneId.of("Asia/Tokyo")).format(shortDateFormatter)
}

private fun getEmoji(showData: ShowData): String {
    fun getReturnTimeEmoji(showData: ShowData): String {
        showData.hour ?: return "\uD83D\uDD70️"
        showData.minute ?: return "\uD83D\uDD70️"
        val hourIndex = (showData.hour - 1).mod(12)
        var char = '\uDD50' + hourIndex
        if(showData.minute >= 30)
            char += 12
        return "\uD83D$char"
    }
    return when(showData.status) {
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