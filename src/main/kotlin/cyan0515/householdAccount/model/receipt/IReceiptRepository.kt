package cyan0515.householdAccount.model.receipt

import cyan0515.householdAccount.model.user.User

interface IReceiptRepository {
    suspend fun create(user: User, receipt: Receipt)
    suspend fun readByUser(user: User): List<Receipt>
}
