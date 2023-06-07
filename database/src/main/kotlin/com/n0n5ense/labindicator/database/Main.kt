package com.n0n5ense.labindicator.database

import com.n0n5ense.labindicator.common.Permissions
import com.n0n5ense.labindicator.common.RoomStatus
import com.n0n5ense.labindicator.database.entity.Permission
import com.n0n5ense.labindicator.database.entity.Status
import com.n0n5ense.labindicator.database.entity.User
import com.n0n5ense.labindicator.database.repository.StatusRepository
import com.n0n5ense.labindicator.database.repository.UserRepository


fun main() {
    connectToDatabase("H:/dbs/lb2.db")
//    val user = User("test1","test 1", "M2")
//    println(UserRepository.exists("test1"))

//    StatusRepository.getAll().also {  it.fold({ println(it)},{it.printStackTrace()}) }
//    val u1 = UserRepository.add(User("test1","test 1", "M2")).getOrThrow()
//    val u2 = UserRepository.add(User("test2","test 2", "M1")).getOrThrow()
//    val u3 = UserRepository.add(User("test3","test 3", "B4")).getOrThrow()
//
//    StatusRepository.add(Status(u1, RoomStatus.Home))
//    StatusRepository.add(Status(u2, RoomStatus.Home))
//    StatusRepository.add(Status(u3, RoomStatus.Home))
//    StatusRepository.add(Status(u1, RoomStatus.Lecture))
//    StatusRepository.add(Status(u3, RoomStatus.Meal))
//    StatusRepository.add(Status(u1, RoomStatus.AroundHere))

    StatusRepository.getLatest().also { it.fold({ it.forEach { println(it) }},{it.printStackTrace()}) }
}