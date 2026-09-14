package cyan0515.householdAccount.model.user

interface IUserRepository {
    suspend fun create(user: User)
    suspend fun read(name: String): User?
}
