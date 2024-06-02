package cyan0515.householdAccount.route

import cyan0515.householdAccount.infrastructure.Categories
import cyan0515.householdAccount.infrastructure.Users
import cyan0515.householdAccount.model.user.User
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt


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
