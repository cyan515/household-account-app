package cyan0515.householdAccount.infrastructure

import cyan0515.householdAccount.model.receipt.IReceiptRepository
import cyan0515.householdAccount.model.receipt.Receipt
import cyan0515.householdAccount.model.user.User
import java.util.UUID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction

object Receipts : Table(), IReceiptRepository {
    val id = uuid("id").uniqueIndex()
    private val userId = uuid("user_id").references(Users.id)
    private val dateTime = datetime("date_time")

    override fun create(user: User, receipt: Receipt) {
        transaction {
            insert {
                it[this.id] = UUID.fromString(receipt.id)
                it[this.userId] = UUID.fromString(user.id)
                it[this.dateTime] = receipt.dateTime
            }
            receipt.details.forEach { detail ->
                ReceiptDetails.insert {
                    it[this.receiptId] = UUID.fromString(receipt.id)
                    it[this.categoryId] = UUID.fromString(detail.categoryId)
                    it[this.itemName] = detail.itemName
                    it[this.amount] = detail.amount
                }
            }
        }
    }

    override fun select(user: User): List<Receipt> {
        TODO("Not yet implemented")
    }
}
