package com.n0n5ense.labindicator.database

import com.n0n5ense.labindicator.common.Permissions
import com.n0n5ense.labindicator.common.RoomStatus
import com.n0n5ense.labindicator.database.dto.Status
import com.n0n5ense.labindicator.database.dto.User
import com.n0n5ense.labindicator.database.repository.StatusRepository
import com.n0n5ense.labindicator.database.repository.UserRepository


fun main() {
    connectToDatabase("C:/db/lb2.db")
//    val user = User("test1","test 1", "M2")
//    println(UserRepository.exists("test1"))

//    StatusRepository.getAll().also {  it.fold({ println(it)},{it.printStackTrace()}) }
    val u1 = UserRepository.add(User(name = "test 1", grade = "M2")).getOrThrow()
    val u2 = UserRepository.add(User(name = "test 2", grade = "M1")).getOrThrow()
    val u3 = UserRepository.add(User(name = "test 3", grade = "B4")).getOrThrow()

    StatusRepository.add(Status(u1.userId, RoomStatus.Home))
    StatusRepository.add(Status(u2.userId, RoomStatus.Home))
    StatusRepository.add(Status(u3.userId, RoomStatus.Home))
    StatusRepository.add(Status(u1.userId, RoomStatus.Lecture))
    StatusRepository.add(Status(u3.userId, RoomStatus.Meal))
    StatusRepository.add(Status(u1.userId, RoomStatus.AroundHere))

    val u4 = User(
        "test 4", "M2", password = "\$2a\$10\$eDRcJNICUkvLPPpPkPmA5O8ELhyh/a37F11QhNsc/5YnPulSaKjgS",
        permissions = listOf(Permissions.Admin, Permissions.EditOtherUser)
    )
    UserRepository.add(u4)

    val u5 = User("test 5", "B3", discordId = "t1")
    val u6 = User("test 6", "B3", discordId = "t1")
    UserRepository.add(u5).onFailure { println(it) }.onSuccess { println(it) }
    UserRepository.add(u6).onFailure { println(it) }.onSuccess { println(it) }

//    StatusRepository.add(Status(User.fromUserId("sdlfkj"), RoomStatus.Meal)).onFailure {
//        it.printStackTrace()
//    }


    //StatusRepository.getLatest().also { it.fold({ it.forEach { println(it) }},{it.printStackTrace()}) }
}