package cyan0515.householdAccount

import cyan0515.householdAccount.model.user.UserAlreadyExistsException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.exception
import io.ktor.server.response.respond
import java.util.concurrent.CancellationException

data class ErrorResponse(val code: String, val message: String)

fun Application.setupErrorHandling() {
    install(StatusPages) {
        exception<BadRequestException> { call, _ ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("invalid_request", "Request is invalid")
            )
        }
        exception<UserAlreadyExistsException> { call, _ ->
            call.respond(
                HttpStatusCode.Conflict,
                ErrorResponse("user_already_exists", "User already exists")
            )
        }
        exception<Throwable> { call, cause ->
            if (cause is CancellationException) throw cause
            call.application.environment.log.error("Unhandled request failure", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("internal_server_error", "Internal server error")
            )
        }
    }
}
