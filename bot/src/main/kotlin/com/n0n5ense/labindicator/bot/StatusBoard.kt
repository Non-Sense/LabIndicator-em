package com.n0n5ense.labindicator.bot

import com.n0n5ense.labindicator.common.RoomStatus
import com.n0n5ense.labindicator.database.dto.StatusMessage
import com.n0n5ense.labindicator.database.dto.StatusToDisplay
import com.n0n5ense.labindicator.database.repository.StatusRepository
import net.dv8tion.jda.api.JDA
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class StatusBoardRow(
    val messageId: Long
)

class StatusBoard(
    private val jda: JDA
) {

    companion object {
        private val dateFormatter = DateTimeFormatter.ofPattern("YYYY/MM/dd HH:mm:ss [z]")
        private val shortDateFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")
    }

    private val logger = LoggerFactory.getLogger("StatusBoard")

    fun setup(channelId: Long) {
        val channel = jda.getTextChannelById(channelId) ?: kotlin.run {
            logger.warn("channel not found")
            return
        }
        val userStatuses = StatusRepository.getLatest().getOrElse {
            logger.warn(it.toString())
            return
        }

        val messages = userStatuses.mapIndexed() { index, status ->
            channel.sendMessage(makeTextMessage(ShowData.fromStatusToDisplay(status))).queue {
                StatusMessage(index, it.idLong)
            }
        }
    }
}

private data class ShowData(
    val name: String,
    val status: RoomStatus,
    val time: Instant,
    val hour: Int? = null,
    val minute: Int? = null
) {
    companion object {
        fun fromStatusToDisplay(status: StatusToDisplay): ShowData {
            return ShowData(
                name = status.user.name,
                status = status.status,
                time = status.time,
                hour = status.backHour,
                minute = status.backMinute
            )
        }
    }
}

private fun makeStatusString(showData: ShowData): Pair<String, String> {
    val lang = when(showData.status) {
        RoomStatus.InRoom -> "yaml"
        RoomStatus.Home -> "brainfuck"
        else -> ""
    }
    if(showData.status == RoomStatus.WillReturnAt) {
        val str = RoomStatus.getWillReturnDisplayString(showData.hour ?: -1, showData.minute ?: -1)
        return """```${str.first}```""" to """```${str.second}```"""
    }
    return """```$lang
            |${showData.status.japanese}
            |```""".trimMargin() to """```$lang
            |${showData.status.english}
            |```""".trimMargin()
}


private val shortDateFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")
private fun Instant.toShortDisplayString(): String {
    return this.atZone(ZoneId.of("Asia/Tokyo")).format(shortDateFormatter)
}

private fun makeTextMessage(showData: ShowData): String {
    val s = makeStatusString(showData)
    return "${getEmoji(showData)}  ${showData.name}  `${showData.time.toShortDisplayString()}`\n${s.first}"
}

private fun getEmoji(showData: ShowData): String {
    fun getReturnTimeEmoji(showData: ShowData): String {
        showData.hour ?: return "\uD83D\uDD70️"
        showData.minute ?: return "\uD83D\uDD70️"
        return when(showData.hour.mod(12) + if(showData.minute >= 30) 12 else 0) {
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