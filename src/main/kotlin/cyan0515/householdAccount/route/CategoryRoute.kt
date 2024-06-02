package cyan0515.householdAccount.route

import cyan0515.householdAccount.infrastructure.Categories
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.categoryRoutes() {
    route("/categories") {

        authenticate {
            get {
                val categories = transaction {
                    Categories.selectAll().toList().map { it[Categories.name] }
                }
                call.respond(categories)
            }
        }
    }
}
