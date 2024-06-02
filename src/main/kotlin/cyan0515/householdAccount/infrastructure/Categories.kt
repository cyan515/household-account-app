package cyan0515.householdAccount.infrastructure

import org.jetbrains.exposed.dao.id.IntIdTable

object Categories: IntIdTable() {
    val name = varchar("name", 50).uniqueIndex()
}