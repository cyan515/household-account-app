package cyan0515.householdAccount.route

import cyan0515.householdAccount.model.user.User
import cyan0515.householdAccount.infrastructure.ExposedUserRepository
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.server.routing.post
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import io.ktor.http.HttpStatusCode

fun Route.userRoutes() {
    route("/users") {

        post {
            try {
                val user = call.receive<User>()
                transaction {
                    ExposedUserRepository.insert {
                        it[userName] = user.userName
                        it[password] = user.password
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
