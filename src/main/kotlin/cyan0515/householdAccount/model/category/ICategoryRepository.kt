package cyan0515.householdAccount.model.category

interface ICategoryRepository {
    suspend fun create(category: Category)
    suspend fun readAll(): List<Category>
}
