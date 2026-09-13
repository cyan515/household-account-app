package cyan0515.householdAccount.route

import cyan0515.householdAccount.model.user.IUserRepository
import cyan0515.householdAccount.model.user.User
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import org.mindrot.jbcrypt.BCrypt

fun Route.userRoutes() {

    val repository by inject<IUserRepository>()

    route("/users") {

        post {
            val request = call.receive<Credentials>()
            val encryptedUser = User(
                name = request.name,
                password = BCrypt.hashpw(request.password, BCrypt.gensalt())
            )
            repository.create(encryptedUser)
            call.respond(HttpStatusCode.Created)
        }
    }
}
