package cyan0515.householdAccount.route

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import cyan0515.householdAccount.infrastructure.Users
import cyan0515.householdAccount.model.user.User
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

fun Route.authRoutes(secret: String, issuer: String, audience: String) {

    post("/login") {
        val loginRequest = call.receive<User>()
        val user = validateUser(loginRequest.userName, loginRequest.password)
        if (user != null) {
            val token = generateToken(loginRequest.userName, secret, issuer, audience)
            call.respond(token)
        } else {
            call.respondText("Invalid credentials", status = HttpStatusCode.Unauthorized)
        }
    }
}

private fun validateUser(userName: String, password: String): User? {
    return transaction {
        Users
            .select { Users.userName eq userName }
            .singleOrNull { BCrypt.checkpw(password, it[Users.password]) }
            ?.let { User(userName, password) }
    }
}

private fun generateToken(userName: String, secret: String, issuer: String, audience: String): String {
    return JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("userName", userName)
        .sign(Algorithm.HMAC256(secret))
}