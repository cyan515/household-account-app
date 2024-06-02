package cyan0515.householdAccount

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.ConfigFactory
import cyan0515.householdAccount.infrastructure.Users
import cyan0515.householdAccount.route.authRoutes
import cyan0515.householdAccount.route.userRoutes
import io.ktor.server.application.*
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

fun Application.module() {
    install(ContentNegotiation) {
        jackson { }
    }

    val environment: ApplicationEnvironment = applicationEngineEnvironment {
        config = HoconApplicationConfig(ConfigFactory.parseFile(File("src/main/resources/application.conf")))
    }
    val jwtAudience = environment.config.property("jwt.audience").getString()
    val jwtRealm = environment.config.property("jwt.realm").getString()
    val jwtSecret = environment.config.property("jwt.secret").getString()
    val jwtIssuer = environment.config.property("jwt.domain").getString()

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

    configureDatabase()

    transaction {
        SchemaUtils.create(Users)
    }

    routing {
        get("/") {
            call.respondText("Hello Ktor!")
        }
        userRoutes()
        authRoutes(jwtSecret, jwtIssuer, jwtAudience)
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

