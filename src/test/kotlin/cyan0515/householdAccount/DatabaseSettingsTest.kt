package cyan0515.householdAccount

import io.ktor.server.config.MapApplicationConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DatabaseSettingsTest {

    @Test
    fun `reads valid database settings`() {
        val settings = validConfig().apply {
            put("db.maximumPoolSize", "20")
            put("db.minimumIdle", "2")
            put("db.connectionTimeoutMillis", "5000")
            put("db.baselineOnMigrate", "true")
        }.readDatabaseSettings()

        assertEquals("jdbc:postgresql://localhost:5432/household", settings.url)
        assertEquals("household_user", settings.user)
        assertEquals("password", settings.password)
        assertEquals(20, settings.maximumPoolSize)
        assertEquals(2, settings.minimumIdle)
        assertEquals(5_000, settings.connectionTimeoutMillis)
        assertTrue(settings.baselineOnMigrate)
    }

    @Test
    fun `uses safe pool defaults`() {
        val settings = validConfig().readDatabaseSettings()

        assertEquals(10, settings.maximumPoolSize)
        assertEquals(1, settings.minimumIdle)
        assertEquals(30_000, settings.connectionTimeoutMillis)
        assertFalse(settings.baselineOnMigrate)
    }

    @Test
    fun `requires database connection settings`() {
        listOf("db.url", "db.user", "db.password").forEach { path ->
            assertFailsWith<IllegalStateException>("missing $path") {
                validConfig(excludedPath = path).readDatabaseSettings()
            }
            assertFailsWith<IllegalStateException>("blank $path") {
                validConfig().apply { put(path, " ") }.readDatabaseSettings()
            }
        }
    }

    @Test
    fun `rejects invalid database and pool settings`() {
        val invalidSettings = listOf(
            "db.url" to "jdbc:mysql://localhost/household",
            "db.maximumPoolSize" to "0",
            "db.maximumPoolSize" to "101",
            "db.maximumPoolSize" to "not-a-number",
            "db.minimumIdle" to "-1",
            "db.minimumIdle" to "11",
            "db.connectionTimeoutMillis" to "249",
            "db.connectionTimeoutMillis" to "300001",
            "db.connectionTimeoutMillis" to "not-a-number",
            "db.baselineOnMigrate" to "yes"
        )

        invalidSettings.forEach { (path, value) ->
            assertFailsWith<IllegalArgumentException>("$path=$value") {
                validConfig().apply { put(path, value) }.readDatabaseSettings()
            }
        }
    }

    private fun validConfig(excludedPath: String? = null): MapApplicationConfig {
        val values = mapOf(
            "db.url" to "jdbc:postgresql://localhost:5432/household",
            "db.user" to "household_user",
            "db.password" to "password"
        ).filterKeys { it != excludedPath }
        return MapApplicationConfig(*values.map { it.key to it.value }.toTypedArray())
    }
}
