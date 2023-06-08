package com.n0n5ense.labindicator.database

import com.n0n5ense.labindicator.common.Permissions
import com.n0n5ense.labindicator.common.RoomStatus
import com.n0n5ense.labindicator.database.dto.Status
import com.n0n5ense.labindicator.database.dto.User
import com.n0n5ense.labindicator.database.repository.StatusRepository
import com.n0n5ense.labindicator.database.repository.UserRepository


fun main() {
    connectToDatabase("H:/dbs/lb2.db")
//    val user = User("test1","test 1", "M2")
//    println(UserRepository.exists("test1"))

//    StatusRepository.getAll().also {  it.fold({ println(it)},{it.printStackTrace()}) }
    val u1 = UserRepository.add(User("test1","test 1", "M2")).getOrThrow()
    val u2 = UserRepository.add(User("test2","test 2", "M1")).getOrThrow()
    val u3 = UserRepository.add(User("test3","test 3", "B4")).getOrThrow()

    StatusRepository.add(Status("test1", RoomStatus.Home))
    StatusRepository.add(Status("test2", RoomStatus.Home))
    StatusRepository.add(Status("test3", RoomStatus.Home))
    StatusRepository.add(Status("test1", RoomStatus.Lecture))
    StatusRepository.add(Status("test3", RoomStatus.Meal))
    StatusRepository.add(Status("test1", RoomStatus.AroundHere))

    val u4 = User(
        "test4", "test 4", "M2", null, "\$2a\$10\$eDRcJNICUkvLPPpPkPmA5O8ELhyh/a37F11QhNsc/5YnPulSaKjgS",
        permissions = listOf(Permissions.Admin, Permissions.EditOtherUser)
    )
    UserRepository.add(u4)

    val u5 = User("test5", "test 5", "B3", discordId = "t1")
    val u6 = User("test6", "test 6", "B3", discordId = "t1")
    UserRepository.add(u5).onFailure { println(it) }
    UserRepository.add(u6).onFailure { println(it) }

//    StatusRepository.add(Status(User.fromUserId("sdlfkj"), RoomStatus.Meal)).onFailure {
//        it.printStackTrace()
//    }


    //StatusRepository.getLatest().also { it.fold({ it.forEach { println(it) }},{it.printStackTrace()}) }
}