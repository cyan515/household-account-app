package cyan0515.householdAccount.route

import cyan0515.householdAccount.infrastructure.ReceiptDetails
import cyan0515.householdAccount.infrastructure.Receipts
import cyan0515.householdAccount.infrastructure.Users
import cyan0515.householdAccount.model.receipt.Receipt
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.receiptRoutes() {

    route("/receipt") {

        authenticate {
            post {
                val principal = call.principal<JWTPrincipal>()
                val name = principal?.payload?.getClaim("userName")?.asString()

                if (name == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid token")
                    return@post
                }
                val userId = transaction {
                    Users.select { Users.name eq name }.map { it[Users.id] }.single().value
                }

                val receipt = call.receive<Receipt>()
                transaction {
                    val id = Receipts.insertAndGetId {
                        it[this.userId] = userId
                        it[this.dateTime] = receipt.dateTime
                    }.value
                    receipt.details.forEach { detail ->
                        ReceiptDetails.insert {
                            it[this.receiptId] = id
                            it[this.categoryId] = detail.categoryId
                            it[this.itemName] = detail.itemName
                            it[this.amount] = detail.amount
                        }
                    }
                }

                call.respond(HttpStatusCode.Created)
            }
        }
    }
}
