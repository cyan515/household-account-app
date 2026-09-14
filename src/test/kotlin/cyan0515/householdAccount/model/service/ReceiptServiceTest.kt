package cyan0515.householdAccount.model.service

import cyan0515.householdAccount.model.category.Category
import cyan0515.householdAccount.model.category.ICategoryRepository
import cyan0515.householdAccount.model.receipt.InvalidReceiptException
import cyan0515.householdAccount.model.receipt.Receipt
import cyan0515.householdAccount.model.receipt.ReceiptDetail
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ReceiptServiceTest {

    private val category = Category("食費")
    private val service = ReceiptService(
        object : ICategoryRepository {
            override fun create(category: Category) = Unit
            override fun readAll(): List<Category> = listOf(category)
        }
    )

    @Test
    fun `accepts valid receipt`() {
        service.validateForCreation(
            receiptWith(ReceiptDetail("😀".repeat(50), 100, category.id))
        )
    }

    @Test
    fun `rejects invalid receipt details`() {
        val invalidReceipts = listOf(
            "empty details" to Receipt(LocalDateTime.parse("2024-01-01T12:30:00"), emptyList()),
            "blank item name" to receiptWith(ReceiptDetail(" ", 100, category.id)),
            "zero amount" to receiptWith(ReceiptDetail("卵", 0, category.id)),
            "negative amount" to Receipt(
                LocalDateTime.parse("2024-01-01T12:30:00"),
                listOf(
                    ReceiptDetail("牛乳", 100, category.id),
                    ReceiptDetail("卵", -1, category.id)
                )
            ),
            "unknown category" to receiptWith(
                ReceiptDetail("卵", 100, "00000000-0000-0000-0000-000000000000")
            ),
            "long item name" to receiptWith(ReceiptDetail("😀".repeat(51), 100, category.id))
        )

        invalidReceipts.forEach { (caseName, receipt) ->
            assertFailsWith<InvalidReceiptException>(caseName) {
                service.validateForCreation(receipt)
            }
        }
    }

    private fun receiptWith(detail: ReceiptDetail) = Receipt(
        dateTime = LocalDateTime.parse("2024-01-01T12:30:00"),
        details = listOf(detail)
    )
}
