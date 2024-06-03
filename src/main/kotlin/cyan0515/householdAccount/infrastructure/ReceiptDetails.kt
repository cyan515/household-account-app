package cyan0515.householdAccount.infrastructure

import org.jetbrains.exposed.dao.id.IntIdTable

object ReceiptDetails : IntIdTable() {
    val receiptId = integer("receiptId").references(Receipts.id)
    val categoryId = integer("categoryId").references(Categories.id)
    val itemName = varchar("itemName", 50)
    val amount = integer("amount")
}