package cyan0515.householdAccount.infrastructure

import cyan0515.householdAccount.model.user.IUserRepository
import cyan0515.householdAccount.model.user.User
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object Users : IntIdTable(), IUserRepository {
    val name = varchar("name", 50).uniqueIndex()
    val password = varchar("password", 100)

    override fun create(user: User): Int {
        return transaction {
            insertAndGetId {
                it[name] = user.userName
                it[password] = user.password
            }
        }.value
    }

    override fun read(name: String): User? {
        return transaction {
            select { Users.name eq name }
                .singleOrNull()
                ?.let { User(it[Users.name], it[password]) }
        }
    }
}
