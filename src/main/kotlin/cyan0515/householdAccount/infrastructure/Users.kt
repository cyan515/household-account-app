package cyan0515.householdAccount.infrastructure

import org.jetbrains.exposed.dao.id.IntIdTable

object Users : IntIdTable() {
    val name = varchar("name", 50).uniqueIndex()
    val password = varchar("password", 100)
}
