package cyan0515.householdAccount.route

import cyan0515.householdAccount.infrastructure.Users
import cyan0515.householdAccount.model.user.User
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

fun Route.userRoutes() {
    route("/users") {

        post {
            try {
                val user = call.receive<User>()
                transaction {
                    Users.insert {
                        it[name] = user.userName
                        it[password] = BCrypt.hashpw(user.password, BCrypt.gensalt());
                    }
                }
            } catch (e: Exception) {
                println(e)
                call.respond(HttpStatusCode.InternalServerError)
            }
            call.respond(HttpStatusCode.Created)
        }
    }
}
