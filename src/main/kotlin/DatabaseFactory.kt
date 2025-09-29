package com.example

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.DriverManager

object DatabaseFactory {
    private const val DB_NAME = "ktor_db"
    private const val DB_USER = "postgres"
    private const val DB_PASSWORD = "123"

    fun init() {
        createDatabaseIfNotExists(DB_NAME, DB_USER, DB_PASSWORD)
        connectToDatabase(DB_NAME, DB_USER, DB_PASSWORD)
        createTables()
    }

    private fun createDatabaseIfNotExists(dbName: String, user: String, password: String) {
        val url = "jdbc:postgresql://localhost:5432/postgres"
        DriverManager.getConnection(url, user, password).use { connection ->
            connection.createStatement().use { statement ->
                val result = statement.executeQuery("SELECT 1 FROM pg_database WHERE datname = '$dbName'")
                if (!result.next()) {
                    statement.executeUpdate("CREATE DATABASE $dbName")
                    println("Database $dbName created")
                }
            }
        }
    }

    private fun connectToDatabase(dbName: String, user: String, password: String) {
        Database.connect(
            url = "jdbc:postgresql://localhost:5432/$dbName",
            driver = "org.postgresql.Driver",
            user = user,
            password = password
        )
    }

    private fun createTables() {
        transaction {
            SchemaUtils.create(Users) // Твои таблицы
        }
    }
}

private fun createTables() {
    transaction {
        SchemaUtils.create(Users) // просто импортируем объект Users
    }
}


