package cyan0515.householdAccount

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val dbUrl = environment.config.property("db.url").getString()
    val dbUser = environment.config.property("db.user").getString()
    val dbPassword = environment.config.property("db.password").getString()

    Database.connect(
        url = dbUrl,
        driver = "org.postgresql.Driver",
        user = dbUser,
        password = dbPassword
    )

    transaction {
        TODO("initialize DB")
//        SchemaUtils.create()
    }
}
