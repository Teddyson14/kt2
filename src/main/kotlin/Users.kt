package com.example

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

object Users : IntIdTable() {
    val username = varchar("username", 50)
    val passwordHash = varchar("password_hash", 64) // <-- исправил имя
}
