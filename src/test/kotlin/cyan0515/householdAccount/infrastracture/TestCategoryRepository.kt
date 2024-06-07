package cyan0515.householdAccount.infrastracture

import cyan0515.householdAccount.model.category.Category
import cyan0515.householdAccount.model.category.ICategoryRepository

object TestCategoryRepository : ICategoryRepository {
    val content: HashMap<String, Category> = HashMap()

    override fun create(category: Category) {
        this.content[category.id] = category
    }

    override fun readAll(): List<Category> {
        return this.content.values.toList()
    }
}
