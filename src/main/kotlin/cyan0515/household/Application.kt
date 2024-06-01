package cyan0515.household

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Application.module() {
    install(ContentNegotiation) {
        jackson { }
    }

    Database.connect(
        url = "jdbc:postgresql://db:5432/household_db",
        driver = "org.postgresql.Driver",
        user = "household_user",
        password = "password"
    )

    routing {
        get("/") {
            call.respondText("Hello Ktor!")
        }
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}
