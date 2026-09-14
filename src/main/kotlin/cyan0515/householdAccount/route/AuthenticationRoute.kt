package cyan0515.householdAccount.route

import cyan0515.householdAccount.model.user.IUserRepository
import cyan0515.householdAccount.security.JwtTokenService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.ktor.ext.inject
import org.mindrot.jbcrypt.BCrypt

fun Route.authRoutes(jwtTokenService: JwtTokenService) {

    val userRepository by inject<IUserRepository>()

    post("/login") {
        val loginRequest = call.receive<Credentials>()
        val passwordMatches = userRepository
            .read(loginRequest.name)
            ?.password
            ?.let { BCrypt.checkpw(loginRequest.password, it) }
        if (passwordMatches == true) {
            val token = jwtTokenService.generate(loginRequest.name)
            call.respond(token)
        } else {
            call.respondText("Invalid credentials", status = HttpStatusCode.Unauthorized)
        }
    }
}
