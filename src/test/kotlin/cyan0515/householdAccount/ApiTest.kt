package cyan0515.householdAccount

import cyan0515.householdAccount.infrastracture.TestUserRepository
import cyan0515.householdAccount.model.user.IUserRepository
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

class ApiTest {
    @Test
    fun `post user`() = testApplication {
        application {
            module(test = true)
        }
        install(Koin) {
            modules(module {
                single<IUserRepository> { TestUserRepository }

            })
        }
        val res = client.post("/users") {
            contentType(Json)
            setBody(""" {"userName":"foo","password":"pass"} """)
        }
        assertEquals(HttpStatusCode.Created, res.status)
    }
}
