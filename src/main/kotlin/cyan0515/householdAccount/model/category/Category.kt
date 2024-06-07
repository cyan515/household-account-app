package cyan0515.householdAccount.model.category

import java.util.UUID

data class Category(val name: String) {
    val id: String = UUID.randomUUID().toString()
}
