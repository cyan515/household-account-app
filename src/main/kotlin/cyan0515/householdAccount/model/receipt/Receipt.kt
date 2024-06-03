package cyan0515.householdAccount.model.receipt

import java.time.LocalDateTime

data class Receipt(val dateTime: LocalDateTime, val details: List<ReceiptDetail>)
