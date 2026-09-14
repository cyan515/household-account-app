package cyan0515.householdAccount.infrastructure

import cyan0515.householdAccount.model.receipt.IReceiptRepository
import cyan0515.householdAccount.model.receipt.Receipt
import cyan0515.householdAccount.model.receipt.ReceiptDetail
import cyan0515.householdAccount.model.user.User
import java.util.UUID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.select
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

    override fun readByUser(user: User): List<Receipt> {
        val targetUserId = UUID.fromString(user.id)

        return transaction {
            (Receipts leftJoin ReceiptDetails)
                .select { userId eq targetUserId }
                .orderBy(
                    dateTime to SortOrder.ASC,
                    Receipts.id to SortOrder.ASC,
                    ReceiptDetails.id to SortOrder.ASC
                )
                .toList()
                .groupBy { it[Receipts.id] }
                .map { (receiptId, rows) ->
                    Receipt(
                        id = receiptId.toString(),
                        dateTime = rows.first()[dateTime],
                        details = rows.mapNotNull { row ->
                            row.getOrNull(ReceiptDetails.id)?.let {
                                ReceiptDetail(
                                    itemName = row[ReceiptDetails.itemName],
                                    amount = row[ReceiptDetails.amount],
                                    categoryId = row[ReceiptDetails.categoryId].toString()
                                )
                            }
                        }
                    )
                }
        }
    }
}
