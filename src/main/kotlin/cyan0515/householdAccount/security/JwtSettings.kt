package cyan0515.householdAccount.security

import io.ktor.server.config.ApplicationConfig
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Duration

data class JwtSettings(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val tokenTtl: Duration
)

fun ApplicationConfig.readJwtSettings(): JwtSettings {
    val secret = requiredNonBlank("jwt.secret")
    require(secret.toByteArray(UTF_8).size >= MINIMUM_SECRET_BYTES) {
        "jwt.secret must contain at least $MINIMUM_SECRET_BYTES UTF-8 bytes"
    }

    val ttlSeconds = property("jwt.ttlSeconds").getString().toLongOrNull()
        ?: throw IllegalArgumentException("jwt.ttlSeconds must be an integer")
    require(ttlSeconds in 1..MAXIMUM_TTL_SECONDS) {
        "jwt.ttlSeconds must be between 1 and $MAXIMUM_TTL_SECONDS"
    }

    return JwtSettings(
        secret = secret,
        issuer = requiredNonBlank("jwt.domain"),
        audience = requiredNonBlank("jwt.audience"),
        realm = requiredNonBlank("jwt.realm"),
        tokenTtl = Duration.ofSeconds(ttlSeconds)
    )
}

private fun ApplicationConfig.requiredNonBlank(path: String): String =
    propertyOrNull(path)
        ?.getString()
        ?.takeIf(String::isNotBlank)
        ?: throw IllegalStateException("$path must be configured")

private const val MINIMUM_SECRET_BYTES = 32
private const val MAXIMUM_TTL_SECONDS = 86_400L
