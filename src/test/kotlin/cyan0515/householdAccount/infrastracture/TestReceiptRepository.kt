package cyan0515.householdAccount.infrastracture

import cyan0515.householdAccount.model.receipt.IReceiptRepository
import cyan0515.householdAccount.model.receipt.Receipt
import cyan0515.householdAccount.model.receipt.ReceiptDetail
import cyan0515.householdAccount.model.user.User

object TestReceiptRepository : IReceiptRepository {
    val content: HashMap<Pair<String, String>, Receipt> = HashMap()
    val detailContent: HashMap<Int, ReceiptDetail> = HashMap()
    override fun create(user: User, receipt: Receipt) {
        content[receipt.id to user.id] = receipt
        receipt.details.forEach { detailContent.plus(it.hashCode() to it) }
    }

    override fun readByUser(user: User): List<Receipt> {

        return this.content.values.toList()
    }
}
