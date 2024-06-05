package cyan0515.householdAccount.model.receipt

import cyan0515.householdAccount.model.user.User

interface IReceiptRepository {
    fun create(user:User, receipt: Receipt): Int
    fun select(user: User): List<Receipt>
}