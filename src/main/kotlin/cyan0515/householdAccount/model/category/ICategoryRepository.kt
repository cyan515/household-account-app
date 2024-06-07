package cyan0515.householdAccount.model.category

interface ICategoryRepository {
    fun create(category: Category)
    fun readAll(): List<Category>
}
