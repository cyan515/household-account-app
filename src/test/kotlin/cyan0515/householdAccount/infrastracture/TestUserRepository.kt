package cyan0515.householdAccount.infrastracture

import cyan0515.householdAccount.model.user.IUserRepository
import cyan0515.householdAccount.model.user.User

object TestUserRepository :IUserRepository{
    private val content: Map<Int,User> = HashMap()
    override fun create(user: User): Int {
        content.plus(user.hashCode() to user)
        return user.hashCode()
    }

    override fun read(name: String): User? {
        return content.values.firstOrNull { it.userName == name }
    }
}