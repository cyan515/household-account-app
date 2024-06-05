package cyan0515.householdAccount.infrastructure

import cyan0515.householdAccount.model.category.Category
import cyan0515.householdAccount.model.category.ICategoryRepository
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object Categories : IntIdTable(), ICategoryRepository {
    val name = varchar("name", 50).uniqueIndex()

    override fun readAll(): List<Category> {
        return transaction {
            selectAll().map { Category(it[name]) }
        }
    }
}
