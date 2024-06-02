package cyan0515.householdAccount

import cyan0515.householdAccount.infrastructure.Categories
import cyan0515.householdAccount.infrastructure.Users
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.setupDatabase() {
    val dbUrl = "jdbc:postgresql://localhost:5432/household_db"
    val dbUser = "household_user"
    val dbPassword = "password"

    Database.connect(
        url = dbUrl,
        driver = "org.postgresql.Driver",
        user = dbUser,
        password = dbPassword
    )

    transaction {
        SchemaUtils.create(Users, Categories)
        if (!isInitialized()) {
            Categories.insert { it[name] = "食費" }
            Categories.insert { it[name] = "衣料品費" }
            Categories.insert { it[name] = "住居費" }
            Categories.insert { it[name] = "水道光熱費" }
            Categories.insert { it[name] = "交通費" }
            Categories.insert { it[name] = "医療費" }
            Categories.insert { it[name] = "教育費" }
            Categories.insert { it[name] = "娯楽費" }
            Categories.insert { it[name] = "通信費" }
            Categories.insert { it[name] = "保険料" }
            Categories.insert { it[name] = "税金" }
            Categories.insert { it[name] = "借入返済" }
        }
    }
}

private fun isInitialized(): Boolean {
    return Categories.selectAll().toList().isNotEmpty()
}
