package cyan0515.householdAccount.infrastructure

import cyan0515.householdAccount.DatabaseManager
import cyan0515.householdAccount.DatabaseSettings
import cyan0515.householdAccount.setupDatabase
import cyan0515.householdAccount.model.receipt.Receipt
import cyan0515.householdAccount.model.receipt.ReceiptDetail
import cyan0515.householdAccount.model.user.User
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import java.sql.DriverManager
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.flywaydb.core.api.FlywayException
import org.flywaydb.core.api.output.MigrateResult
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReceiptsIntegrationTest {

    private lateinit var databaseManager: DatabaseManager
    private lateinit var initialMigration: MigrateResult
    private lateinit var repeatedMigration: MigrateResult

    companion object {
        @Container
        @JvmField
        val postgres = PostgreSQLContainer("postgres:14")

        private val EXPECTED_CATEGORY_NAMES = setOf(
            "食費",
            "衣料品費",
            "住居費",
            "水道光熱費",
            "交通費",
            "医療費",
            "教育費",
            "娯楽費",
            "通信費",
            "保険料",
            "税金",
            "借入返済"
        )

        private val LEGACY_SCHEMA_STATEMENTS = listOf(
            """
            CREATE TABLE users (
                id UUID NOT NULL UNIQUE,
                name VARCHAR(50) NOT NULL UNIQUE,
                password VARCHAR(100) NOT NULL
            )
            """.trimIndent(),
            """
            CREATE TABLE categories (
                id UUID NOT NULL UNIQUE,
                name VARCHAR(50) NOT NULL
            )
            """.trimIndent(),
            """
            CREATE TABLE receipts (
                id UUID NOT NULL UNIQUE,
                user_id UUID NOT NULL REFERENCES users (id),
                date_time TIMESTAMP WITHOUT TIME ZONE NOT NULL
            )
            """.trimIndent(),
            """
            CREATE TABLE receipt_details (
                id SERIAL PRIMARY KEY,
                receipt_id UUID NOT NULL REFERENCES receipts (id),
                category_id UUID NOT NULL REFERENCES categories (id),
                item_name VARCHAR(50) NOT NULL,
                amount INTEGER NOT NULL
            )
            """.trimIndent(),
            """
            INSERT INTO users (id, name, password)
            VALUES ('10000000-0000-0000-0000-000000000001', 'legacy-user', 'legacy-password')
            """.trimIndent(),
            """
            INSERT INTO categories (id, name)
            VALUES ('20000000-0000-0000-0000-000000000001', 'legacy-category')
            """.trimIndent(),
            """
            INSERT INTO receipts (id, user_id, date_time)
            VALUES (
                '30000000-0000-0000-0000-000000000001',
                '10000000-0000-0000-0000-000000000001',
                '2024-01-01 12:30:00'
            )
            """.trimIndent(),
            """
            INSERT INTO receipt_details (receipt_id, category_id, item_name, amount)
            VALUES (
                '30000000-0000-0000-0000-000000000001',
                '20000000-0000-0000-0000-000000000001',
                'legacy-item',
                100
            )
            """.trimIndent()
        )
    }

    @BeforeAll
    fun connectToDatabase() {
        databaseManager = DatabaseManager(
            DatabaseSettings(
                url = postgres.jdbcUrl,
                user = postgres.username,
                password = postgres.password,
                maximumPoolSize = 4,
                minimumIdle = 1,
                connectionTimeoutMillis = 5_000,
                baselineOnMigrate = false
            )
        )
        initialMigration = databaseManager.initialize()
        repeatedMigration = databaseManager.migrate()
    }

    @BeforeEach
    fun clearData() {
        transaction {
            ReceiptDetails.deleteAll()
            Receipts.deleteAll()
            Users.deleteAll()
        }
    }

    @AfterAll
    fun closeDatabase() {
        databaseManager.close()
        assertTrue(databaseManager.dataSource.isClosed)
    }

    @Test
    fun `migrates empty database once and seeds categories`() {
        assertTrue(initialMigration.migrationsExecuted > 0)
        assertEquals(0, repeatedMigration.migrationsExecuted)
        assertEquals(EXPECTED_CATEGORY_NAMES, Categories.readAll().map { it.name }.toSet())
        assertEquals(4, databaseManager.dataSource.maximumPoolSize)
        assertEquals(1, databaseManager.dataSource.minimumIdle)
        assertTrue(databaseManager.dataSource.connection.use { it.isValid(1) })
    }

    @Test
    fun `requires explicit baseline opt in and preserves legacy data`() {
        databaseManager.dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("CREATE SCHEMA legacy_schema")
            }
        }
        val legacyUrl = postgres.jdbcUrl.withCurrentSchema("legacy_schema")
        DriverManager.getConnection(legacyUrl, postgres.username, postgres.password).use { connection ->
            connection.createStatement().use { statement ->
                LEGACY_SCHEMA_STATEMENTS.forEach(statement::execute)
            }
        }

        val rejectedManager = DatabaseManager(databaseSettings(legacyUrl, baselineOnMigrate = false))
        assertFailsWith<FlywayException> {
            rejectedManager.initialize()
        }
        assertTrue(rejectedManager.dataSource.isClosed)

        val baselineManager = DatabaseManager(databaseSettings(legacyUrl, baselineOnMigrate = true))
        try {
            val result = baselineManager.initialize()

            assertEquals(0, result.migrationsExecuted)
            baselineManager.dataSource.connection.use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeQuery(
                        """
                        SELECT users.id AS user_id,
                               categories.id AS category_id,
                               receipts.id AS receipt_id,
                               receipt_details.item_name,
                               receipt_details.amount
                        FROM receipt_details
                        JOIN receipts ON receipts.id = receipt_details.receipt_id
                        JOIN users ON users.id = receipts.user_id
                        JOIN categories ON categories.id = receipt_details.category_id
                        """.trimIndent()
                    ).use { rows ->
                        assertTrue(rows.next())
                        assertEquals("10000000-0000-0000-0000-000000000001", rows.getString("user_id"))
                        assertEquals("20000000-0000-0000-0000-000000000001", rows.getString("category_id"))
                        assertEquals("30000000-0000-0000-0000-000000000001", rows.getString("receipt_id"))
                        assertEquals("legacy-item", rows.getString("item_name"))
                        assertEquals(100, rows.getInt("amount"))
                        assertFalse(rows.next())
                    }
                    statement.executeQuery(
                        "SELECT type, version FROM flyway_schema_history WHERE success = true"
                    ).use { rows ->
                        assertTrue(rows.next())
                        assertEquals("BASELINE", rows.getString("type"))
                        assertEquals("2", rows.getString("version"))
                    }
                }
            }
        } finally {
            baselineManager.close()
            Database.connect(databaseManager.dataSource)
        }
    }

    @Test
    fun `application database setup reads environment configuration`() {
        databaseManager.dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("CREATE SCHEMA application_config_schema")
            }
        }
        val applicationUrl = postgres.jdbcUrl.withCurrentSchema("application_config_schema")

        try {
            testApplication {
                environment {
                    config = MapApplicationConfig(
                        "db.url" to applicationUrl,
                        "db.user" to postgres.username,
                        "db.password" to postgres.password,
                        "db.maximumPoolSize" to "2",
                        "db.minimumIdle" to "1",
                        "db.connectionTimeoutMillis" to "5000",
                        "db.baselineOnMigrate" to "false"
                    )
                }
                application {
                    setupDatabase()
                }
                startApplication()
            }
        } finally {
            Database.connect(databaseManager.dataSource)
        }

        DriverManager.getConnection(applicationUrl, postgres.username, postgres.password).use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery(
                    "SELECT COUNT(*) FROM flyway_schema_history WHERE success = true"
                ).use { rows ->
                    assertTrue(rows.next())
                    assertTrue(rows.getInt(1) >= 2)
                }
            }
        }
    }

    @Test
    fun `reads receipts and details only for requested user`() {
        val userA = User(name = "alice", password = "password")
        val userB = User(name = "bob", password = "password")
        val categories = Categories.readAll().associateBy { it.name }
        val food = categories.getValue("食費")
        val transport = categories.getValue("交通費")
        Users.create(userA)
        Users.create(userB)

        val firstReceipt = Receipt(
            dateTime = LocalDateTime.parse("2024-01-01T12:30:00"),
            details = listOf(
                ReceiptDetail("卵", 100, food.id),
                ReceiptDetail("電車", 200, transport.id)
            )
        )
        val secondReceipt = Receipt(
            dateTime = LocalDateTime.parse("2024-02-01T12:30:00"),
            details = listOf(ReceiptDetail("牛乳", 150, food.id))
        )
        val otherUsersReceipt = Receipt(
            dateTime = LocalDateTime.parse("2024-03-01T12:30:00"),
            details = listOf(ReceiptDetail("混入してはいけない支出", 999, food.id))
        )
        Receipts.create(userA, secondReceipt)
        Receipts.create(userA, firstReceipt)
        Receipts.create(userB, otherUsersReceipt)

        assertEquals(listOf(firstReceipt, secondReceipt), Receipts.readByUser(userA))
        assertEquals(listOf(otherUsersReceipt), Receipts.readByUser(userB))
    }

    @Test
    fun `reads receipt without details`() {
        val user = User(name = "alice", password = "password")
        val receipt = Receipt(
            dateTime = LocalDateTime.parse("2024-01-01T12:30:00"),
            details = emptyList()
        )
        Users.create(user)
        Receipts.create(user, receipt)

        assertEquals(listOf(receipt), Receipts.readByUser(user))
    }

    @Test
    fun `returns empty list when user has no receipts`() {
        val user = User(name = "alice", password = "password")
        Users.create(user)

        assertTrue(Receipts.readByUser(user).isEmpty())
    }

    private fun databaseSettings(url: String, baselineOnMigrate: Boolean) = DatabaseSettings(
        url = url,
        user = postgres.username,
        password = postgres.password,
        maximumPoolSize = 2,
        minimumIdle = 1,
        connectionTimeoutMillis = 5_000,
        baselineOnMigrate = baselineOnMigrate
    )

    private fun String.withCurrentSchema(schema: String): String =
        "$this${if (contains('?')) '&' else '?'}currentSchema=$schema"
}
