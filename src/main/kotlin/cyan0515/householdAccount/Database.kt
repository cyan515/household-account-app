package cyan0515.householdAccount

import cyan0515.householdAccount.infrastructure.Users
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
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
        SchemaUtils.create(Users)
    }
}
