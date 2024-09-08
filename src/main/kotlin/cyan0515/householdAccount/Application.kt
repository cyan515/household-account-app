package cyan0515.householdAccount

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.typesafe.config.ConfigFactory
import cyan0515.householdAccount.route.authRoutes
import cyan0515.householdAccount.route.categoryRoutes
import cyan0515.householdAccount.route.receiptRoutes
import cyan0515.householdAccount.route.userRoutes
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.config.tryGetString
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.module(test: Boolean = false) {
    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }

    if (!test) setupKoin()

    val config = ConfigFactory.load()
    val jwtAudience = config.getString("jwt.audience")
    val jwtRealm = config.getString("jwt.realm")
    val jwtSecret = config.tryGetString("jwt.secret") ?: "secret"
    val jwtIssuer = config.getString("jwt.domain")

    install(Authentication) {
        jwt {
            realm = jwtRealm

            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
        }
    }

    if (!test) setupDatabase()

    routing {
        get("/") {
            call.respondText("Hello Ktor!")
        }
        userRoutes()
        categoryRoutes()
        receiptRoutes()
        authRoutes(jwtSecret, jwtIssuer, jwtAudience)
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}
