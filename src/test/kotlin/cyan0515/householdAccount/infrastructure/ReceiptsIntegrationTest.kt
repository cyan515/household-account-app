package cyan0515.householdAccount.infrastructure

import cyan0515.householdAccount.model.category.Category
import cyan0515.householdAccount.model.receipt.Receipt
import cyan0515.householdAccount.model.receipt.ReceiptDetail
import cyan0515.householdAccount.model.user.User
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReceiptsIntegrationTest {

    companion object {
        @Container
        @JvmField
        val postgres = PostgreSQLContainer("postgres:14")
    }

    @BeforeAll
    fun connectToDatabase() {
        Database.connect(
            url = postgres.jdbcUrl,
            driver = postgres.driverClassName,
            user = postgres.username,
            password = postgres.password
        )
    }

    @BeforeEach
    fun resetSchema() {
        transaction {
            SchemaUtils.drop(ReceiptDetails, Receipts, Categories, Users)
            SchemaUtils.create(Users, Categories, Receipts, ReceiptDetails)
        }
    }

    @Test
    fun `reads receipts and details only for requested user`() {
        val userA = User(name = "alice", password = "password")
        val userB = User(name = "bob", password = "password")
        val food = Category("食費")
        val transport = Category("交通費")
        Users.create(userA)
        Users.create(userB)
        Categories.create(food)
        Categories.create(transport)

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
}
