package cyan0515.householdAccount.model.user

import java.util.UUID

data class User(val id: String = UUID.randomUUID().toString(), val name: String, val password: String)
