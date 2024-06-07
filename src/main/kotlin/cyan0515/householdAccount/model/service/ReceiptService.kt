package cyan0515.householdAccount.model.service

import cyan0515.householdAccount.model.category.Category
import cyan0515.householdAccount.model.category.ICategoryRepository
import cyan0515.householdAccount.model.receipt.Receipt

class ReceiptService(
    private val categoryRepository: ICategoryRepository
) {
    fun summarize(receipts: List<Receipt>): Map<Category, Int> {
        val categoryMap = categoryRepository.readAll().associateBy { it.id }

        return receipts
            .flatMap { it.details }
            .groupBy { it.categoryId }
            .mapKeys { categoryMap[it.key]!! }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }
}
