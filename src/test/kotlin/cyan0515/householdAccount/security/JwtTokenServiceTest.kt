package cyan0515.householdAccount.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.MissingClaimException
import com.auth0.jwt.exceptions.TokenExpiredException
import io.ktor.server.config.MapApplicationConfig
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class JwtTokenServiceTest {

    @Test
    fun `generates token with required claims and expiration`() {
        val issuedAt = Instant.parse("2024-01-01T00:00:00Z")
        val settings = validSettings()
        val service = JwtTokenService(
            settings,
            Clock.fixed(issuedAt, ZoneOffset.UTC)
        )

        val signedToken = service.generate("alice")
        val token = JWT.decode(signedToken)

        Algorithm.HMAC256(settings.secret).verify(token)
        assertEquals(settings.issuer, token.issuer)
        assertEquals(listOf(settings.audience), token.audience)
        assertNull(token.subject)
        assertEquals("alice", token.getClaim("userName").asString())
        assertEquals(issuedAt, token.issuedAtAsInstant)
        assertEquals(issuedAt.plus(settings.tokenTtl), token.expiresAtAsInstant)
    }

    @Test
    fun `verifier rejects expired token`() {
        val service = JwtTokenService(
            validSettings(),
            Clock.fixed(Instant.parse("2000-01-01T00:00:00Z"), ZoneOffset.UTC)
        )
        val token = service.generate("alice")

        assertFailsWith<TokenExpiredException> {
            service.verifier().verify(token)
        }
    }

    @Test
    fun `verifier requires identity and lifetime claims`() {
        val settings = validSettings()
        val service = JwtTokenService(settings)
        val now = Instant.now().minusSeconds(1)
        val baseToken = {
            JWT.create()
                .withAudience(settings.audience)
                .withIssuer(settings.issuer)
        }
        val tokensMissingRequiredClaim = listOf(
            baseToken()
                .withIssuedAt(now)
                .withExpiresAt(now.plus(settings.tokenTtl))
                .sign(Algorithm.HMAC256(settings.secret)),
            baseToken()
                .withClaim(USER_NAME_CLAIM, "alice")
                .withExpiresAt(now.plus(settings.tokenTtl))
                .sign(Algorithm.HMAC256(settings.secret)),
            baseToken()
                .withClaim(USER_NAME_CLAIM, "alice")
                .withIssuedAt(now)
                .sign(Algorithm.HMAC256(settings.secret))
        )

        tokensMissingRequiredClaim.forEach { token ->
            assertFailsWith<MissingClaimException> {
                service.verifier().verify(token)
            }
        }
    }

    @Test
    fun `requires secure secret configuration`() {
        assertFailsWith<IllegalStateException> {
            configWithoutSecret().readJwtSettings()
        }
        assertFailsWith<IllegalArgumentException> {
            configWithoutSecret()
                .apply { put("jwt.secret", "too-short") }
                .readJwtSettings()
        }
        configWithoutSecret()
            .apply { put("jwt.secret", "12345678901234567890123456789012") }
            .readJwtSettings()
    }

    @Test
    fun `requires positive integer token ttl`() {
        listOf("0", "-1", "86401", "not-a-number").forEach { ttl ->
            assertFailsWith<IllegalArgumentException>("ttl=$ttl") {
                configWithoutSecret()
                    .apply {
                        put("jwt.secret", TEST_SECRET)
                        put("jwt.ttlSeconds", ttl)
                    }
                    .readJwtSettings()
            }
        }
    }

    private fun validSettings() = JwtSettings(
        secret = TEST_SECRET,
        issuer = "https://example.com",
        audience = "household-account-api",
        realm = "household-account",
        tokenTtl = Duration.ofHours(1)
    )

    private fun configWithoutSecret() = MapApplicationConfig(
        "jwt.domain" to "https://example.com",
        "jwt.audience" to "household-account-api",
        "jwt.realm" to "household-account",
        "jwt.ttlSeconds" to "3600"
    )

    private companion object {
        const val TEST_SECRET = "test-secret-with-at-least-32-characters"
    }
}
