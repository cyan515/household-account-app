package cyan0515.householdAccount.infrastructure

import cyan0515.householdAccount.model.category.Category
import cyan0515.householdAccount.model.category.ICategoryRepository
import java.util.UUID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object Categories : Table(), ICategoryRepository {
    val id = uuid("id").uniqueIndex()
    val name = varchar("name", 50)

    override fun create(category: Category) {
        transaction {
            insert {
                it[id] = UUID.fromString(category.id)
                it[name] = category.name
            }
        }
    }

    override fun readAll(): List<Category> {
        return transaction {
            selectAll().map { Category(it[name], it[Categories.id].toString()) }
        }
    }
}
