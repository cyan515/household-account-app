package cyan0515.householdAccount.infrastructure

import cyan0515.householdAccount.databaseTransaction
import cyan0515.householdAccount.model.user.IUserRepository
import cyan0515.householdAccount.model.user.User
import java.util.UUID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

object Users : Table(), IUserRepository {
    val id = uuid("id").uniqueIndex()
    private val name = varchar("name", 50).uniqueIndex()
    private val password = varchar("password", 100)

    override suspend fun create(user: User) {
        databaseTransaction {
            insert {
                it[id] = UUID.fromString(user.id)
                it[name] = user.name
                it[password] = user.password
            }
        }
    }

    override suspend fun read(name: String): User? {
        return databaseTransaction {
            select { Users.name eq name }
                .singleOrNull()
                ?.let { User(it[Users.id].toString(), it[Users.name], it[password]) }
        }
    }
}
