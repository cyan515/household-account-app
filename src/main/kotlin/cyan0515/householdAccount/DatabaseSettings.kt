package cyan0515.householdAccount

import io.ktor.server.config.ApplicationConfig

class DatabaseSettings(
    val url: String,
    val user: String,
    val password: String,
    val maximumPoolSize: Int,
    val minimumIdle: Int,
    val connectionTimeoutMillis: Long,
    val baselineOnMigrate: Boolean
)

fun ApplicationConfig.readDatabaseSettings(): DatabaseSettings {
    val maximumPoolSize = integer("db.maximumPoolSize", DEFAULT_MAXIMUM_POOL_SIZE)
    require(maximumPoolSize in 1..MAXIMUM_POOL_SIZE) {
        "db.maximumPoolSize must be between 1 and $MAXIMUM_POOL_SIZE"
    }

    val minimumIdle = integer("db.minimumIdle", DEFAULT_MINIMUM_IDLE)
    require(minimumIdle in 0..maximumPoolSize) {
        "db.minimumIdle must be between 0 and db.maximumPoolSize"
    }

    val connectionTimeoutMillis = long("db.connectionTimeoutMillis", DEFAULT_CONNECTION_TIMEOUT_MILLIS)
    require(connectionTimeoutMillis in MINIMUM_CONNECTION_TIMEOUT_MILLIS..MAXIMUM_CONNECTION_TIMEOUT_MILLIS) {
        "db.connectionTimeoutMillis must be between $MINIMUM_CONNECTION_TIMEOUT_MILLIS and " +
            "$MAXIMUM_CONNECTION_TIMEOUT_MILLIS"
    }

    val url = requiredNonBlank("db.url")
    require(url.startsWith(POSTGRESQL_JDBC_PREFIX)) {
        "db.url must be a PostgreSQL JDBC URL"
    }

    return DatabaseSettings(
        url = url,
        user = requiredNonBlank("db.user"),
        password = requiredNonBlank("db.password"),
        maximumPoolSize = maximumPoolSize,
        minimumIdle = minimumIdle,
        connectionTimeoutMillis = connectionTimeoutMillis,
        baselineOnMigrate = boolean("db.baselineOnMigrate", false)
    )
}

private fun ApplicationConfig.requiredNonBlank(path: String): String =
    propertyOrNull(path)
        ?.getString()
        ?.takeIf(String::isNotBlank)
        ?: throw IllegalStateException("$path must be configured")

private fun ApplicationConfig.integer(path: String, default: Int): Int {
    val value = propertyOrNull(path)?.getString() ?: return default
    return value.toIntOrNull() ?: throw IllegalArgumentException("$path must be an integer")
}

private fun ApplicationConfig.long(path: String, default: Long): Long {
    val value = propertyOrNull(path)?.getString() ?: return default
    return value.toLongOrNull() ?: throw IllegalArgumentException("$path must be an integer")
}

private fun ApplicationConfig.boolean(path: String, default: Boolean): Boolean {
    val value = propertyOrNull(path)?.getString() ?: return default
    return value.toBooleanStrictOrNull() ?: throw IllegalArgumentException("$path must be true or false")
}

private const val POSTGRESQL_JDBC_PREFIX = "jdbc:postgresql:"
private const val DEFAULT_MAXIMUM_POOL_SIZE = 10
private const val MAXIMUM_POOL_SIZE = 100
private const val DEFAULT_MINIMUM_IDLE = 1
private const val DEFAULT_CONNECTION_TIMEOUT_MILLIS = 30_000L
private const val MINIMUM_CONNECTION_TIMEOUT_MILLIS = 250L
private const val MAXIMUM_CONNECTION_TIMEOUT_MILLIS = 300_000L
