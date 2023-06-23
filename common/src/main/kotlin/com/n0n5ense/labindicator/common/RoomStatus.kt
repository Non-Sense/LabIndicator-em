package com.n0n5ense.labindicator.common

enum class RoomStatus(
    val english: String,
    val japanese: String,
    val alias: String
) {
    InRoom("In room", "在室", "in"),
    AroundHere("Around here", "この辺", "ar"),
    Lecture("Lecture/Seminar", "講義/輪講", "lc"),
    Library("Library", "図書館", "lb"),
    Meal("Meal", "食事", "me"),
    RightBack("Right back", "すぐ戻る", "rb"),
    WillReturnAt("Will return at", "何時に戻る", "wr"),
    Exercise("Exercise", "運動", "ex"),
    Home("Home", "帰宅", "hm"),
    Unknown("Unknown", "不明", "unknown");

    companion object {
        val validStatuses = values().filter { it != WillReturnAt && it != Unknown }

        fun findValue(str: String): RoomStatus? {
            return kotlin.runCatching { valueOf(str) }.getOrNull()
        }
    }
}