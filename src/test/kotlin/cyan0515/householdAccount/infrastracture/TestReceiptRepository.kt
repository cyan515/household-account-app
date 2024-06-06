package cyan0515.householdAccount.infrastracture

import cyan0515.householdAccount.model.receipt.IReceiptRepository
import cyan0515.householdAccount.model.receipt.Receipt
import cyan0515.householdAccount.model.receipt.ReceiptDetail
import cyan0515.householdAccount.model.user.User

object TestReceiptRepository : IReceiptRepository {
    val content: HashMap<Pair<Int, Int>, Receipt> = HashMap()
    val detailContent: HashMap<Int, ReceiptDetail> = HashMap()
    override fun create(user: User, receipt: Receipt): Int {
        val userId = TestUserRepository.content
            .filterValues { e -> e == user }
            .firstNotNullOf { e -> e.key }
        content[receipt.hashCode() to userId] = receipt
        receipt.details.forEach { detailContent.plus(it.hashCode() to it) }
        return receipt.hashCode()
    }

    override fun select(user: User): List<Receipt> {
        return this.content.values.toList()
    }
}