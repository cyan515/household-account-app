package cyan0515.householdAccount.model.service

import cyan0515.householdAccount.model.category.Category
import cyan0515.householdAccount.model.category.ICategoryRepository
import cyan0515.householdAccount.model.receipt.InvalidReceiptException
import cyan0515.householdAccount.model.receipt.Receipt

class ReceiptService(
    private val categoryRepository: ICategoryRepository
) {
    fun validateForCreation(receipt: Receipt) {
        val hasInvalidDetail = receipt.details.any { detail ->
            detail.itemName.isBlank() ||
                detail.itemName.codePointCount(0, detail.itemName.length) > MAX_ITEM_NAME_LENGTH ||
                detail.amount <= 0
        }
        if (receipt.details.isEmpty() || hasInvalidDetail) throw InvalidReceiptException()

        val categoryIds = categoryRepository.readAll().mapTo(mutableSetOf()) { it.id }
        if (receipt.details.any { it.categoryId !in categoryIds }) throw InvalidReceiptException()
    }

    fun summarize(receipts: List<Receipt>): Map<Category, Int> {
        val categoryMap = categoryRepository.readAll().associateBy { it.id }

        return receipts
            .flatMap { it.details }
            .groupBy { it.categoryId }
            .mapKeys { entry ->
                checkNotNull(categoryMap[entry.key]) { "Receipt references an unknown category" }
            }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    private companion object {
        const val MAX_ITEM_NAME_LENGTH = 50
    }
}
