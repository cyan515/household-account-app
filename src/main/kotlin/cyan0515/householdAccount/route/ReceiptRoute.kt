package cyan0515.householdAccount.route

import cyan0515.householdAccount.model.receipt.IReceiptRepository
import cyan0515.householdAccount.model.receipt.InvalidDateRangeException
import cyan0515.householdAccount.model.service.ReceiptService
import cyan0515.householdAccount.model.user.IUserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
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
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException
import org.koin.ktor.ext.inject

fun Route.receiptRoutes() {

    val repository by inject<IReceiptRepository>()
    val userRepository by inject<IUserRepository>()

    val receiptService by inject<ReceiptService>()

    route("/receipts") {

        authenticate {
            post {
                val user = call.principal<JWTPrincipal>()
                    ?.payload
                    ?.getClaim("userName")
                    ?.asString()
                    ?.let(userRepository::read)

                if (user == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid token")
                    return@post
                }

                val receipt = call.receive<CreateReceiptRequest>().toReceipt()
                receiptService.validateForCreation(receipt)
                repository.create(user, receipt)

                call.respond(HttpStatusCode.Created)
            }

            get("/summaries") {
                val user = call.principal<JWTPrincipal>()
                    ?.payload
                    ?.getClaim("userName")
                    ?.asString()
                    ?.let(userRepository::read)

                if (user == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid token")
                    return@get
                }

                val fromDate = call.request.queryParameters.parseLocalDate("from")
                val toDate = call.request.queryParameters.parseLocalDate("to")
                if (fromDate != null && toDate != null && fromDate > toDate) {
                    throw InvalidDateRangeException()
                }
                val from = fromDate?.atStartOfDay() ?: LocalDateTime.MIN
                val to = toDate?.atTime(LocalTime.MAX) ?: LocalDateTime.MAX

                val receipts = repository.readByUser(user).filter {
                    !it.dateTime.isBefore(from) && !it.dateTime.isAfter(to)
                }
                val summary = receiptService.summarize(receipts).mapKeys { it.key.name }

                call.respond(summary)
            }
        }
    }
}

private fun Parameters.parseLocalDate(name: String): LocalDate? {
    val value = this[name] ?: return null
    return try {
        LocalDate.parse(value)
    } catch (_: DateTimeParseException) {
        throw InvalidDateRangeException()
    }
}
