package cyan0515.householdAccount.infrastructure

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object Receipts : IntIdTable() {
    val userId = integer("userId").references(Users.id)
    val dateTime = datetime("dateTime")
}