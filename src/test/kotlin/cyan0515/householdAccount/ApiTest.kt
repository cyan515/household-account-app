package cyan0515.householdAccount

import cyan0515.householdAccount.infrastracture.TestCategoryRepository
import cyan0515.householdAccount.infrastracture.TestReceiptRepository
import cyan0515.householdAccount.infrastracture.TestUserRepository
import cyan0515.householdAccount.model.category.Category
import cyan0515.householdAccount.model.category.ICategoryRepository
import cyan0515.householdAccount.model.receipt.IReceiptRepository
import cyan0515.householdAccount.model.user.IUserRepository
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
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
        )
            .map(::Category)
            .map(TestCategoryRepository::create)
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
            setBody(""" {"name":"foo","password":"pass"} """)
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
        client.post("/users") {
            contentType(Json)
            setBody(""" {"name":"foo","password":"pass"} """)
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
        client.post("/users") {
            contentType(Json)
            setBody(""" {"name":"foo","password":"pass"} """)
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
        client.post("/users") {
            contentType(Json)
            setBody(""" {"name":"foo","password":"pass"} """)
        }
        val authRes = client.post("/login") {
            contentType(Json)
            setBody(""" {"name":"foo","password":"pass"} """)
        }
        val receipt = """
            {
              "dateTime": "2020-01-01T12:30:00",
              "details": [
                {
                  "categoryId": 1,
                  "itemName": "卵",
                  "amount": 150
                },
                {
                  "categoryId": 1,
                  "itemName": "牛乳",
                  "amount": 200
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

}
