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
import cyan0515.householdAccount.security.JwtSettings
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.Application
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.mindrot.jbcrypt.BCrypt

class ApiTest {

    private fun Application.testModule(clock: Clock = Clock.systemUTC()) = configureForTest(
        jwtSettingsOverride = TEST_JWT_SETTINGS,
        jwtClock = clock
    )

    private fun testModules(userRepository: IUserRepository = TestUserRepository) = module {
        single<IUserRepository> { userRepository }
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
        val testUser = User(name = "foo", password = BCrypt.hashpw("pass", BCrypt.gensalt()))
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
            testModule()
        }
        install(Koin) {
            modules(testModules())
        }
        val res = client.post("/users") {
            contentType(Json)
            setBody(""" {"name":"bar","password":"pass"} """)
        }
        assertEquals(HttpStatusCode.Created, res.status)

        val storedUser = TestUserRepository.read("bar")!!
        assertNotEquals("pass", storedUser.password)
        assertTrue(BCrypt.checkpw("pass", storedUser.password))

        val loginRes = client.post("/login") {
            contentType(Json)
            setBody(""" {"name":"bar","password":"pass"} """)
        }
        assertEquals(HttpStatusCode.OK, loginRes.status)
    }

    @Test
    fun `post duplicate user`() = testApplication {
        application {
            testModule()
        }
        install(Koin) {
            modules(testModules())
        }
        val res = client.post("/users") {
            contentType(Json)
            setBody(""" {"name":"foo","password":"pass"} """)
        }

        assertEquals(HttpStatusCode.Conflict, res.status)
        assertEquals(
            """{"code":"user_already_exists","message":"User already exists"}""",
            res.bodyAsText()
        )
    }

    @Test
    fun `post user with malformed body`() = testApplication {
        application {
            testModule()
        }
        install(Koin) {
            modules(testModules())
        }
        val res = client.post("/users") {
            contentType(Json)
            setBody(""" {"name":"bar" """)
        }

        assertEquals(HttpStatusCode.BadRequest, res.status)
        assertEquals(
            """{"code":"invalid_request","message":"Request is invalid"}""",
            res.bodyAsText()
        )
        assertNull(TestUserRepository.read("bar"))
    }

    @Test
    fun `post user with missing password`() = testApplication {
        application {
            testModule()
        }
        install(Koin) {
            modules(testModules())
        }
        val res = client.post("/users") {
            contentType(Json)
            setBody(""" {"name":"bar"} """)
        }

        assertEquals(HttpStatusCode.BadRequest, res.status)
        assertEquals(
            """{"code":"invalid_request","message":"Request is invalid"}""",
            res.bodyAsText()
        )
        assertNull(TestUserRepository.read("bar"))
    }

    @Test
    fun `login with malformed body`() = testApplication {
        application {
            testModule()
        }
        install(Koin) {
            modules(testModules())
        }
        val res = client.post("/login") {
            contentType(Json)
            setBody(""" {"name":"foo" """)
        }

        assertEquals(HttpStatusCode.BadRequest, res.status)
        assertEquals(
            """{"code":"invalid_request","message":"Request is invalid"}""",
            res.bodyAsText()
        )
    }

    @Test
    fun `post user returns internal server error when repository fails`() = testApplication {
        val failingRepository = object : IUserRepository {
            override fun create(user: User) {
                throw IllegalStateException("internal details must not be exposed")
            }

            override fun read(name: String): User? = null
        }
        application {
            testModule()
        }
        install(Koin) {
            modules(testModules(failingRepository))
        }
        val res = client.post("/users") {
            contentType(Json)
            setBody(""" {"name":"bar","password":"pass"} """)
        }

        assertEquals(HttpStatusCode.InternalServerError, res.status)
        assertEquals(
            """{"code":"internal_server_error","message":"Internal server error"}""",
            res.bodyAsText()
        )
    }

    @Test
    fun `login with valid credentials`() = testApplication {
        application {
            testModule()
        }
        install(Koin) {
            modules(testModules())
        }
        val res = client.post("/login") {
            contentType(Json)
            setBody(""" {"name":"foo","password":"pass"} """)
        }
        assertEquals(HttpStatusCode.OK, res.status)
    }

    @Test
    fun `application reads jwt settings from environment configuration`() = testApplication {
        environment {
            config = MapApplicationConfig(
                "jwt.secret" to TEST_JWT_SETTINGS.secret,
                "jwt.domain" to TEST_JWT_SETTINGS.issuer,
                "jwt.audience" to TEST_JWT_SETTINGS.audience,
                "jwt.realm" to TEST_JWT_SETTINGS.realm,
                "jwt.ttlSeconds" to TEST_JWT_SETTINGS.tokenTtl.seconds.toString()
            )
        }
        application {
            configureForTest()
        }
        install(Koin) {
            modules(testModules())
        }
        val res = client.post("/login") {
            contentType(Json)
            setBody(""" {"name":"foo","password":"pass"} """)
        }

        assertEquals(HttpStatusCode.OK, res.status)
    }

    @Test
    fun `expired login token is rejected`() = testApplication {
        application {
            testModule(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))
        }
        install(Koin) {
            modules(testModules())
        }
        val authRes = client.post("/login") {
            contentType(Json)
            setBody(""" {"name":"foo","password":"pass"} """)
        }
        val res = client.get("/categories") {
            header("Authorization", "Bearer ${authRes.bodyAsText()}")
        }

        assertEquals(HttpStatusCode.OK, authRes.status)
        assertEquals(HttpStatusCode.Unauthorized, res.status)
    }

    @Test
    fun `login with incorrect password`() = testApplication {
        application {
            testModule()
        }
        install(Koin) {
            modules(testModules())
        }
        val res = client.post("/login") {
            contentType(Json)
            setBody(""" {"name":"foo","password":"incorrect"} """)
        }
        assertEquals(HttpStatusCode.Unauthorized, res.status)
        assertEquals("Invalid credentials", res.bodyAsText())
    }

    @Test
    fun `login with unknown user`() = testApplication {
        application {
            testModule()
        }
        install(Koin) {
            modules(testModules())
        }
        val res = client.post("/login") {
            contentType(Json)
            setBody(""" {"name":"unknown","password":"pass"} """)
        }
        assertEquals(HttpStatusCode.Unauthorized, res.status)
        assertEquals("Invalid credentials", res.bodyAsText())
    }

    @Test
    fun `get category`() = testApplication {
        application {
            testModule()
        }
        install(Koin) {
            modules(testModules())
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
            testModule()
        }
        install(Koin) {
            modules(testModules())
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
        assertEquals(4, TestReceiptRepository.content.size)
    }

    @Test
    fun `post invalid receipt`() = testApplication {
        application {
            testModule()
        }
        install(Koin) {
            modules(testModules())
        }
        val authRes = client.post("/login") {
            contentType(Json)
            setBody(""" {"name":"foo","password":"pass"} """)
        }
        val res = client.post("/receipts") {
            contentType(Json)
            header("Authorization", "Bearer ${authRes.bodyAsText()}")
            setBody(
                """
                {
                  "dateTime": "2020-01-01T12:30:00",
                  "details": []
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.BadRequest, res.status)
        assertEquals(
            """{"code":"invalid_receipt","message":"Receipt is invalid"}""",
            res.bodyAsText()
        )
        assertEquals(3, TestReceiptRepository.content.size)
    }

    @Test
    fun `post receipt with invalid date time`() = testApplication {
        val foodCostId = TestCategoryRepository.content
            .values
            .first { it.name == "食費" }
            .id
        application {
            testModule()
        }
        install(Koin) {
            modules(testModules())
        }
        val authRes = client.post("/login") {
            contentType(Json)
            setBody(""" {"name":"foo","password":"pass"} """)
        }
        val res = client.post("/receipts") {
            contentType(Json)
            header("Authorization", "Bearer ${authRes.bodyAsText()}")
            setBody(
                """
                {
                  "dateTime": "not-a-date",
                  "details": [
                    {
                      "categoryId": "$foodCostId",
                      "itemName": "鶏肉",
                      "amount": 500
                    }
                  ]
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.BadRequest, res.status)
        assertEquals(
            """{"code":"invalid_request","message":"Request is invalid"}""",
            res.bodyAsText()
        )
        assertEquals(3, TestReceiptRepository.content.size)
    }

    @Test
    fun `get receipt summary with invalid date`() = testApplication {
        application {
            testModule()
        }
        install(Koin) {
            modules(testModules())
        }
        val authRes = client.post("/login") {
            contentType(Json)
            setBody(""" {"name":"foo","password":"pass"} """)
        }
        val res = client.get("/receipts/summaries") {
            header("Authorization", "Bearer ${authRes.bodyAsText()}")
            parameter("from", "not-a-date")
        }

        assertEquals(HttpStatusCode.BadRequest, res.status)
        assertEquals(
            """{"code":"invalid_date_range","message":"Date range is invalid"}""",
            res.bodyAsText()
        )
    }

    @Test
    fun `get receipt summary with reversed date range`() = testApplication {
        application {
            testModule()
        }
        install(Koin) {
            modules(testModules())
        }
        val authRes = client.post("/login") {
            contentType(Json)
            setBody(""" {"name":"foo","password":"pass"} """)
        }
        val res = client.get("/receipts/summaries") {
            header("Authorization", "Bearer ${authRes.bodyAsText()}")
            parameter("from", "2030-01-01")
            parameter("to", "2020-01-01")
        }

        assertEquals(HttpStatusCode.BadRequest, res.status)
        assertEquals(
            """{"code":"invalid_date_range","message":"Date range is invalid"}""",
            res.bodyAsText()
        )
    }

    @Test
    fun `get receipt summary includes the whole to date`() = testApplication {
        application {
            testModule()
        }
        install(Koin) {
            modules(testModules())
        }
        val authRes = client.post("/login") {
            contentType(Json)
            setBody(""" {"name":"foo","password":"pass"} """)
        }
        val res = client.get("/receipts/summaries") {
            header("Authorization", "Bearer ${authRes.bodyAsText()}")
            parameter("from", "2020-01-01")
            parameter("to", "2020-01-01")
        }

        assertEquals(HttpStatusCode.OK, res.status)
        assertEquals("""{"食費":350}""", res.bodyAsText())
    }

    @Test
    fun `get receipt summary`() = testApplication {
        application {
            testModule()
        }
        install(Koin) {
            modules(testModules())
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
            testModule()
        }
        install(Koin) {
            modules(testModules())
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
            testModule()
        }
        install(Koin) {
            modules(testModules())
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
            testModule()
        }
        install(Koin) {
            modules(testModules())
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

    private companion object {
        val TEST_JWT_SETTINGS = JwtSettings(
            secret = "test-secret-with-at-least-32-characters",
            issuer = "cyan0515.com",
            audience = "myAudience",
            realm = "myRealm",
            tokenTtl = Duration.ofHours(1)
        )
    }

}
