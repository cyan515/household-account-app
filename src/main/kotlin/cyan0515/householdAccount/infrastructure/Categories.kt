package cyan0515.householdAccount.infrastructure

import cyan0515.householdAccount.databaseTransaction
import cyan0515.householdAccount.model.category.Category
import cyan0515.householdAccount.model.category.ICategoryRepository
import java.util.UUID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

object Categories : Table(), ICategoryRepository {
    val id = uuid("id").uniqueIndex()
    val name = varchar("name", 50)

    override suspend fun create(category: Category) {
        databaseTransaction {
            insert {
                it[id] = UUID.fromString(category.id)
                it[name] = category.name
            }
        }
    }

    override suspend fun readAll(): List<Category> {
        return databaseTransaction {
            selectAll().map { Category(it[name], it[Categories.id].toString()) }
        }
    }
}
