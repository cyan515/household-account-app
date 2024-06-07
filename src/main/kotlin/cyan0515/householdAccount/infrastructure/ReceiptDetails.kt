package cyan0515.householdAccount.infrastructure

import org.jetbrains.exposed.dao.id.IntIdTable

object ReceiptDetails : IntIdTable("receipt_details") {
    val receiptId = uuid("receipt_id").references(Receipts.id)
    val categoryId = uuid("category_id").references(Categories.id)
    val itemName = varchar("item_name", 50)
    val amount = integer("amount")
}
