package cyan0515.householdAccount.infrastracture

import cyan0515.householdAccount.model.user.IUserRepository
import cyan0515.householdAccount.model.user.User
import cyan0515.householdAccount.model.user.UserAlreadyExistsException

object TestUserRepository : IUserRepository {
    val content: HashMap<String, User> = HashMap()
    override fun create(user: User) {
        if (read(user.name) != null) {
            throw UserAlreadyExistsException()
        }
        content[user.id] = user
    }

    override fun read(name: String): User? {
        return content.values.firstOrNull { it.name == name }
    }
}
