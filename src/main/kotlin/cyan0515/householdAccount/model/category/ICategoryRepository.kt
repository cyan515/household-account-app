package cyan0515.householdAccount.model.category

interface ICategoryRepository {
    fun readAll(): List<Category>
}
