package cyan0515.householdAccount.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.time.Clock

class JwtTokenService(
    private val settings: JwtSettings,
    private val clock: Clock = Clock.systemUTC()
) {
    private val algorithm = Algorithm.HMAC256(settings.secret)

    fun generate(userName: String): String {
        require(userName.isNotBlank()) { "userName must not be blank" }
        val issuedAt = clock.instant()
        return JWT.create()
            .withAudience(settings.audience)
            .withIssuer(settings.issuer)
            .withClaim(USER_NAME_CLAIM, userName)
            .withIssuedAt(issuedAt)
            .withExpiresAt(issuedAt.plus(settings.tokenTtl))
            .sign(algorithm)
    }

    fun verifier(): JWTVerifier = JWT
        .require(algorithm)
        .withAudience(settings.audience)
        .withIssuer(settings.issuer)
        .withClaimPresence(USER_NAME_CLAIM)
        .withClaimPresence(ISSUED_AT_CLAIM)
        .withClaimPresence(EXPIRES_AT_CLAIM)
        .build()
}

const val USER_NAME_CLAIM = "userName"
private const val ISSUED_AT_CLAIM = "iat"
private const val EXPIRES_AT_CLAIM = "exp"
