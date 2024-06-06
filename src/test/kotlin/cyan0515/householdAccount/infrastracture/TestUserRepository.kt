package cyan0515.householdAccount.infrastracture

import cyan0515.householdAccount.model.user.IUserRepository
import cyan0515.householdAccount.model.user.User
import org.mindrot.jbcrypt.BCrypt

object TestUserRepository : IUserRepository {
    val content: HashMap<Int, User> = HashMap()
    override fun create(user: User): Int {
        val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())
        val encryptedUser = User(user.userName, hashedPassword)
        content[encryptedUser.hashCode()] = encryptedUser
        return encryptedUser.hashCode()
    }

    override fun read(name: String): User? {
        return content.values.firstOrNull { it.userName == name }
    }
}
