package cyan0515.householdAccount.model.user

interface IUserRepository {
    fun create(user: User)
    fun read(name: String): User?
}
