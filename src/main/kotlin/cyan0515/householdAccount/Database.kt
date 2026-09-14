package cyan0515.householdAccount

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationVersion
import org.flywaydb.core.api.output.MigrateResult
import org.jetbrains.exposed.sql.Database

fun Application.setupDatabase() {
    val settings = environment.config.readDatabaseSettings()
    val databaseManager = DatabaseManager(settings)
    databaseManager.initialize()
    environment.monitor.subscribe(ApplicationStopped) {
        databaseManager.close()
    }
}

internal class DatabaseManager(private val settings: DatabaseSettings) : AutoCloseable {
    val dataSource = HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = settings.url
            username = settings.user
            password = settings.password
            driverClassName = POSTGRESQL_DRIVER
            maximumPoolSize = settings.maximumPoolSize
            minimumIdle = settings.minimumIdle
            connectionTimeout = settings.connectionTimeoutMillis
            poolName = POOL_NAME
        }
    )

    private val flyway = Flyway.configure()
        .dataSource(dataSource)
        .baselineOnMigrate(settings.baselineOnMigrate)
        .baselineVersion(MigrationVersion.fromVersion(LEGACY_SCHEMA_VERSION))
        .load()

    fun initialize(): MigrateResult = try {
        migrate().also { Database.connect(dataSource) }
    } catch (exception: Exception) {
        close()
        throw exception
    }

    fun migrate(): MigrateResult = flyway.migrate()

    override fun close() {
        dataSource.close()
    }
}

private const val POSTGRESQL_DRIVER = "org.postgresql.Driver"
private const val POOL_NAME = "household-account-db"
private const val LEGACY_SCHEMA_VERSION = "2"
