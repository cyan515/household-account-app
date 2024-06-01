package cyan0515.householdAccount.infrastructure

import org.jetbrains.exposed.dao.id.IntIdTable

object ExposedUserRepository : IntIdTable() {
    val userName = varchar("userName", 50).uniqueIndex()
    val password = varchar("password", 64)
}