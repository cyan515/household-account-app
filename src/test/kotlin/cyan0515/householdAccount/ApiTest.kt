package cyan0515.householdAccount

import cyan0515.householdAccount.infrastracture.TestCategoryRepository
import cyan0515.householdAccount.infrastracture.TestReceiptRepository
import cyan0515.householdAccount.infrastracture.TestUserRepository
import cyan0515.householdAccount.model.category.Category
import cyan0515.householdAccount.model.category.ICategoryRepository
import cyan0515.householdAccount.model.receipt.IReceiptRepository
import cyan0515.householdAccount.model.receipt.Receipt
import cyan0515.householdAccount.model.receipt.ReceiptDetail
import cyan0515.householdAccount.model.service.ReceiptService
import cyan0515.householdAccount.model.user.IUserRepository
import cyan0515.householdAccount.model.user.User
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

class ApiTest {

    private val testModules = module {
        single<IUserRepository> { TestUserRepository }
        single<ICategoryRepository> { TestCategoryRepository }
        single<IReceiptRepository> { TestReceiptRepository }

        single { ReceiptService(get()) }
    }

    @BeforeEach
    fun setUp() {
        arrayOf(
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
        ).map(::Category).map(TestCategoryRepository::create)
        val testUser = User("foo", "pass")
        val foodCostId = TestCategoryRepository.content.filterValues { it.name == "食費" }.firstNotNullOf { it.key }
        val testReceipt1 = Receipt(
            LocalDateTime.parse("2000-01-01T12:30:00"),
            listOf(
                ReceiptDetail("卵", 100, foodCostId),
                ReceiptDetail("牛乳", 120, foodCostId)
            )
        )
        val testReceipt2 = Receipt(
            LocalDateTime.parse("2020-01-01T12:30:00"),
            listOf(
                ReceiptDetail("卵", 150, foodCostId),
                ReceiptDetail("牛乳", 200, foodCostId)
            )
        )
        val testReceipt3 = Receipt(
            LocalDateTime.parse("2040-01-01T12:30:00"),
            listOf(
                ReceiptDetail("卵", 5, foodCostId),
                ReceiptDetail("牛乳", 15, foodCostId)
            )
        )
        TestUserRepository.create(testUser)
        TestReceiptRepository.create(testUser, testReceipt1)
        TestReceiptRepository.create(testUser, testReceipt2)
        TestReceiptRepository.create(testUser, testReceipt3)
    }

    @AfterEach
    fun tearDown() {
        TestUserRepository.content.clear()
        TestCategoryRepository.content.clear()
        TestReceiptRepository.content.clear()
        TestReceiptRepository.detailContent.clear()
    }

    @Test
    fun `post user`() = testApplication {
        application {
            module(test = true)
        }
        install(Koin) {
            modules(testModules)
        }
        val res = client.post("/users") {
            contentType(Json)
            setBody(""" {"name":"bar","password":"pass"} """)
        }
        assertEquals(HttpStatusCode.Created, res.status)
    }

    @Test
    fun login() = testApplication {
        application {
            module(test = true)
        }
        install(Koin) {
            modules(testModules)
        }
        val res = client.post("/login") {
            contentType(Json)
            setBody(""" {"name":"foo","password":"pass"} """)
        }
        assertEquals(HttpStatusCode.OK, res.status)
    }

    @Test
    fun `get category`() = testApplication {
        application {
            module(test = true)
        }
        install(Koin) {
            modules(testModules)
        }
        val authRes = client.post("/login") {
            contentType(Json)
            setBody(""" {"name":"foo","password":"pass"} """)
        }
        val res = client.get("/categories") {
            contentType(Json)
            header("Authorization", "Bearer ${authRes.bodyAsText()}")
        }
        assertEquals(HttpStatusCode.OK, res.status)
    }

    @Test
    fun `post receipt`() = testApplication {
        application {
            module(test = true)
        }
        install(Koin) {
            modules(testModules)
        }
        val authRes = client.post("/login") {
            contentType(Json)
            setBody(""" {"name":"foo","password":"pass"} """)
        }
        val foodCostId = TestCategoryRepository.content.filterValues { it.name == "食費" }.firstNotNullOf { it.key }
        val receipt = """
            {
              "dateTime": "2020-01-01T12:30:00",
              "details": [
                {
                  "categoryId": "$foodCostId",
                  "itemName": "鶏肉",
                  "amount": 500
                }
              ]
            }
        """.trimIndent()
        val res = client.post("/receipts") {
            contentType(Json)
            header("Authorization", "Bearer ${authRes.bodyAsText()}")
            setBody(receipt)
        }
        assertEquals(HttpStatusCode.Created, res.status)
    }

    @Test
    fun `get receipt summary`() = testApplication {
        application {
            module(test = true)
        }
        install(Koin) {
            modules(testModules)
        }
        val authRes = client.post("/login") {
            contentType(Json)
            setBody(""" {"name":"foo","password":"pass"} """)
        }
        val res = client.get("/receipts/summaries") {
            contentType(Json)
            header("Authorization", "Bearer ${authRes.bodyAsText()}")
        }
        assertEquals("""{"食費":590}""", res.bodyAsText())
        assertEquals(HttpStatusCode.OK, res.status)
    }

    @Test
    fun `get receipt summary with from`() = testApplication {
        application {
            module(test = true)
        }
        install(Koin) {
            modules(testModules)
        }
        val authRes = client.post("/login") {
            contentType(Json)
            setBody(""" {"name":"foo","password":"pass"} """)
        }
        val res = client.get("/receipts/summaries") {
            contentType(Json)
            header("Authorization", "Bearer ${authRes.bodyAsText()}")
            parameter("from", "2010-01-01")
        }
        assertEquals("""{"食費":370}""", res.bodyAsText())
        assertEquals(HttpStatusCode.OK, res.status)
    }

    @Test
    fun `get receipt summary with to`() = testApplication {
        application {
            module(test = true)
        }
        install(Koin) {
            modules(testModules)
        }
        val authRes = client.post("/login") {
            contentType(Json)
            setBody(""" {"name":"foo","password":"pass"} """)
        }
        val res = client.get("/receipts/summaries") {
            contentType(Json)
            header("Authorization", "Bearer ${authRes.bodyAsText()}")
            parameter("to", "2030-01-01")
        }
        assertEquals("""{"食費":570}""", res.bodyAsText())
        assertEquals(HttpStatusCode.OK, res.status)
    }

    @Test
    fun `get receipt summary with from and to`() = testApplication {
        application {
            module(test = true)
        }
        install(Koin) {
            modules(testModules)
        }
        val authRes = client.post("/login") {
            contentType(Json)
            setBody(""" {"name":"foo","password":"pass"} """)
        }
        val res = client.get("/receipts/summaries") {
            contentType(Json)
            header("Authorization", "Bearer ${authRes.bodyAsText()}")
            parameter("from", "2010-01-01")
            parameter("to", "2030-01-01")
        }
        assertEquals("""{"食費":350}""", res.bodyAsText())
        assertEquals(HttpStatusCode.OK, res.status)
    }

}
