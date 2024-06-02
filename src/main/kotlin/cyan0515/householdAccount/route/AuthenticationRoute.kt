package cyan0515.householdAccount.route

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import cyan0515.householdAccount.infrastructure.Users
import cyan0515.householdAccount.model.user.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.authRoutes(secret: String, issuer: String,audience: String) {

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

fun validateUser(userName: String, password: String): User? {
    return transaction {
        Users
            .select { Users.userName eq userName }
            .singleOrNull { it[Users.password] == password }
            ?.let { User(userName, password) }
    }
}

fun generateToken(userName: String, secret: String, issuer: String, audience: String): String {
    return JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim("userName", userName)
        .sign(Algorithm.HMAC256(secret))
}
