package cyan0515.householdAccount

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.typesafe.config.ConfigFactory
import cyan0515.householdAccount.route.authRoutes
import cyan0515.householdAccount.route.categoryRoutes
import cyan0515.householdAccount.route.receiptRoutes
import cyan0515.householdAccount.route.userRoutes
import cyan0515.householdAccount.security.JwtSettings
import cyan0515.householdAccount.security.JwtTokenService
import cyan0515.householdAccount.security.USER_NAME_CLAIM
import cyan0515.householdAccount.security.readJwtSettings
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.time.Clock

fun Application.module() = configureApplication(
    test = false,
    jwtSettings = environment.config.readJwtSettings(),
    jwtClock = Clock.systemUTC()
)

internal fun Application.configureForTest(
    jwtSettingsOverride: JwtSettings? = null,
    jwtClock: Clock = Clock.systemUTC()
) = configureApplication(
    test = true,
    jwtSettings = jwtSettingsOverride ?: environment.config.readJwtSettings(),
    jwtClock = jwtClock
)

private fun Application.configureApplication(
    test: Boolean,
    jwtSettings: JwtSettings,
    jwtClock: Clock
) {
    val jwtTokenService = JwtTokenService(jwtSettings, jwtClock)

    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }

    setupErrorHandling()

    if (!test) setupKoin()

    install(Authentication) {
        jwt {
            realm = jwtSettings.realm
            verifier(jwtTokenService.verifier())
            validate { credential ->
                val userName = credential.payload.getClaim(USER_NAME_CLAIM).asString()
                if (userName.isNullOrBlank()) null else JWTPrincipal(credential.payload)
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
        authRoutes(jwtTokenService)
    }
}

fun main() {
    val applicationConfig = HoconApplicationConfig(ConfigFactory.load())
    val serverEnvironment = applicationEngineEnvironment {
        config = applicationConfig
        connector {
            port = applicationConfig.property("ktor.deployment.port").getString().toInt()
        }
        module { module() }
    }
    embeddedServer(Netty, serverEnvironment).start(wait = true)
}
