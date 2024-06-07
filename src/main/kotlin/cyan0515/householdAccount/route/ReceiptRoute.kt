package cyan0515.householdAccount.route

import cyan0515.householdAccount.model.receipt.IReceiptRepository
import cyan0515.householdAccount.model.receipt.Receipt
import cyan0515.householdAccount.model.service.ReceiptService
import cyan0515.householdAccount.model.user.IUserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.time.LocalDate
import org.koin.ktor.ext.inject

fun Route.receiptRoutes() {

    val repository by inject<IReceiptRepository>()
    val userRepository by inject<IUserRepository>()

    val receiptService by inject<ReceiptService>()

    route("/receipts") {

        authenticate {
            post {
                val user =
                    call.principal<JWTPrincipal>()?.payload?.getClaim("userName")?.asString()?.let(userRepository::read)

                if (user == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid token")
                    return@post
                }

                val receipt = call.receive<Receipt>()
                repository.create(user, receipt)

                call.respond(HttpStatusCode.Created)
            }

            get("/summaries") {
                val from = (call.parameters["from"]?.let(LocalDate::parse) ?: LocalDate.of(1900, 1, 1)).atStartOfDay()
                val to = (call.parameters["to"]?.let(LocalDate::parse) ?: LocalDate.of(3000, 1, 1)).atStartOfDay()

                val user =
                    call.principal<JWTPrincipal>()?.payload?.getClaim("userName")?.asString()?.let(userRepository::read)

                if (user == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid token")
                    return@get
                }

                val receipts = repository.readByUser(user).filter { from <= it.dateTime && it.dateTime <= to }
                val summary = receiptService.summarize(receipts).mapKeys { it.key.name }

                call.respond(summary)
            }
        }
    }
}
