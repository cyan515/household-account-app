package cyan0515.householdAccount.route

import cyan0515.householdAccount.model.receipt.IReceiptRepository
import cyan0515.householdAccount.model.receipt.Receipt
import cyan0515.householdAccount.model.user.IUserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.receiptRoutes() {

    val repository by inject<IReceiptRepository>()
    val userRepository by inject<IUserRepository>()

    route("/receipts") {

        authenticate {
            post {
                val principal = call.principal<JWTPrincipal>()
                val user = principal
                    ?.payload
                    ?.getClaim("userName")
                    ?.asString()
                    ?.let(userRepository::read)

                if (user == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid token")
                    return@post
                }

                val receipt = call.receive<Receipt>()
                repository.create(user, receipt)

                call.respond(HttpStatusCode.Created)
            }
        }
    }
}
