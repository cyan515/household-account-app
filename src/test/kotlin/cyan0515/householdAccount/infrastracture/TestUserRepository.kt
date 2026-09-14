package cyan0515.householdAccount.infrastracture

import cyan0515.householdAccount.model.user.IUserRepository
import cyan0515.householdAccount.model.user.User
import org.mindrot.jbcrypt.BCrypt

object TestUserRepository : IUserRepository {
    val content: HashMap<Int, User> = HashMap()
    override suspend fun create(user: User) {
        val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())
        val encryptedUser = User(id = user.id, name = user.name, password = hashedPassword)
        content[encryptedUser.hashCode()] = encryptedUser
    }

    override suspend fun read(name: String): User? {
        return content.values.firstOrNull { it.name == name }
    }
}
