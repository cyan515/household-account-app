package cyan0515.householdAccount.infrastructure

import cyan0515.householdAccount.model.user.IUserRepository
import cyan0515.householdAccount.model.user.User
import cyan0515.householdAccount.model.user.UserAlreadyExistsException
import java.util.UUID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object Users : Table(), IUserRepository {
    val id = uuid("id").uniqueIndex()
    private val name = varchar("name", 50).uniqueIndex()
    private val password = varchar("password", 100)

    override fun create(user: User) {
        val insertedCount = transaction {
            insertIgnore {
                it[id] = UUID.fromString(user.id)
                it[name] = user.name
                it[password] = user.password
            }
        }.insertedCount

        if (insertedCount == 0) {
            throw UserAlreadyExistsException()
        }
    }

    override fun read(name: String): User? {
        return transaction {
            select { Users.name eq name }
                .singleOrNull()
                ?.let { User(it[Users.id].toString(), it[Users.name], it[password]) }
        }
    }
}
