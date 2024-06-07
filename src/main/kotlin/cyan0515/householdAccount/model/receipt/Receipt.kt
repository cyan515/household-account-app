package cyan0515.householdAccount.model.receipt

import java.time.LocalDateTime
import java.util.UUID

data class Receipt(
    val dateTime: LocalDateTime,
    val details: List<ReceiptDetail>,
    val id: String = UUID.randomUUID().toString()
)
