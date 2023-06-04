package com.n0n5ense.labindicator.common

import kotlin.reflect.full.isSubclassOf

//sealed class RoomStatus(
//    val name: String,
//    val english: String,
//    val japanese: String,
//    val alias: String
//) {
//    object InRoom: RoomStatus("inRoom", "In room", "在室", "i")
//    object AroundHere: RoomStatus("aroundHere", "Around here", "この辺", "a")
//    object Lecture: RoomStatus("lecture", "Lecture/Seminar", "講義/輪講", "l")
//    object Library: RoomStatus("library", "Library", "図書館", "lib")
//    object Meal: RoomStatus("meal", "Meal", "食事", "m")
//    object RightBack: RoomStatus("rightBack", "Right back", "すぐ戻る", "r")
//    object WillReturnAt: RoomStatus("willReturnAt", "Will return at", "何時に戻る", "wr")
//    object Exercise: RoomStatus("exercise", "Exercise", "運動", "ex")
//    object Home: RoomStatus("home", "Home", "帰宅", "h")
//    object Unknown: RoomStatus("unknown", "Unknown", "不明", "unknown")
//
//    companion object {
//        private val objects by lazy {
//            RoomStatus::class.nestedClasses.filter {
//                it.isFinal && it.isSubclassOf(RoomStatus::class)
//            }.map { it.objectInstance as RoomStatus }
//        }
//
//        fun values(): List<RoomStatus> = objects.filter { it !is Unknown }
//        fun valueOf(name: String): RoomStatus = objects.find { it.name == name } ?: Unknown
//        fun getWillReturnDisplayString(hour: Int, minute: Int): Pair<String, String> {
//            if(minute == 0)
//                return "${hour}時に戻る" to "${WillReturnAt.english} $hour o'clock"
//            return "$hour:${"%02d".format(minute)}に戻る" to "${WillReturnAt.english} $hour:${"%02d".format(minute)}"
//        }
//    }
//}

enum class RoomStatus(
    val displayName: String,
    val english: String,
    val japanese: String,
    val alias: String
) {
    InRoom("inRoom", "In room", "在室", "i"),
    AroundHere("aroundHere", "Around here", "この辺", "a"),
    Lecture("lecture", "Lecture/Seminar", "講義/輪講", "l"),
    Library("library", "Library", "図書館", "lib"),
    Meal("meal", "Meal", "食事", "m"),
    RightBack("rightBack", "Right back", "すぐ戻る", "r"),
    WillReturnAt("willReturnAt", "Will return at", "何時に戻る", "wr"),
    Exercise("exercise", "Exercise", "運動", "ex"),
    Home("home", "Home", "帰宅", "h"),
    Unknown("unknown", "Unknown", "不明", "unknown");

    companion object {
        private val objects by lazy {
            RoomStatus::class.nestedClasses.filter {
                it.isFinal && it.isSubclassOf(RoomStatus::class)
            }.map { it.objectInstance as RoomStatus }
        }
        fun values(): List<RoomStatus> = objects.filter { it == Unknown }
        fun valueOf(name: String): RoomStatus = objects.find { it.name == name } ?: Unknown
        fun getWillReturnDisplayString(hour: Int, minute: Int): Pair<String, String> {
            if(minute == 0)
                return "${hour}時に戻る" to "${WillReturnAt.english} $hour o'clock"
            return "$hour:${"%02d".format(minute)}に戻る" to "${WillReturnAt.english} $hour:${"%02d".format(minute)}"
        }
    }
}