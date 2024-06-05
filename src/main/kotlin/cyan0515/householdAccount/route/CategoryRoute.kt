package cyan0515.householdAccount.route

import cyan0515.householdAccount.infrastructure.Categories
import cyan0515.householdAccount.model.category.ICategoryRepository
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.categoryRoutes() {

    val repository by inject<ICategoryRepository>()

    route("/categories") {

        authenticate {
            get {
                val categories = repository.readAll()

                call.respond(categories)
            }
        }
    }
}
