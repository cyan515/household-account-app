package cyan0515.householdAccount.route

import cyan0515.householdAccount.model.receipt.Receipt
import cyan0515.householdAccount.model.receipt.ReceiptDetail
import java.time.LocalDateTime

data class CreateReceiptRequest(
    val dateTime: LocalDateTime,
    val details: List<ReceiptDetail>
) {
    fun toReceipt() = Receipt(dateTime = dateTime, details = details)
}
