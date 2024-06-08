package cyan0515.householdAccount.infrastructure

import cyan0515.householdAccount.model.user.IUserRepository
import cyan0515.householdAccount.model.user.User
import java.util.UUID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object Users : Table(), IUserRepository {
    val id = uuid("id").uniqueIndex()
    private val name = varchar("name", 50).uniqueIndex()
    private val password = varchar("password", 100)

    override fun create(user: User) {
        transaction {
            insert {
                it[id] = UUID.fromString(user.id)
                it[name] = user.name
                it[password] = user.password
            }
        }
    }

    override fun read(name: String): User? {
        return transaction {
            select { Users.name eq name }
                .singleOrNull()
                ?.let { User(it[Users.name], it[password]) }
        }
    }
}
