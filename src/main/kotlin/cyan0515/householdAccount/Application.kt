package cyan0515.householdAccount

import cyan0515.householdAccount.infrastructure.Users
import cyan0515.householdAccount.route.userRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.module() {
    install(ContentNegotiation) {
        jackson { }
    }

    configureDatabase()

    transaction {
        SchemaUtils.create(Users)
    }

    routing {
        get("/") {
            call.respondText("Hello Ktor!")
        }
        userRoutes()
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}
