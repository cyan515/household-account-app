package cyan0515.householdAccount.infrastructure

import cyan0515.householdAccount.model.receipt.IReceiptRepository
import cyan0515.householdAccount.model.receipt.Receipt
import cyan0515.householdAccount.model.user.User
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object Receipts : IntIdTable(), IReceiptRepository {
    val userId = integer("user_id").references(Users.id)
    val dateTime = datetime("date_time")

    override fun create(user: User, receipt: Receipt): Int {
        return transaction {
            val userId = transaction {
                Users.select { Users.name eq user.userName }.map { it[Users.id] }.single().value
            }
            val id = Receipts.insertAndGetId {
                it[this.userId] = userId
                it[this.dateTime] = receipt.dateTime
            }.value
            receipt.details.forEach { detail ->
                ReceiptDetails.insert {
                    it[this.receiptId] = id
                    it[this.categoryId] = detail.categoryId
                    it[this.itemName] = detail.itemName
                    it[this.amount] = detail.amount
                }
            }
            return@transaction id
        }
    }

    override fun select(user: User): List<Receipt> {
        TODO("Not yet implemented")
    }
}
