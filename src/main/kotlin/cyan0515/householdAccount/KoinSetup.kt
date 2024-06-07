package cyan0515.householdAccount

import cyan0515.householdAccount.infrastructure.Categories
import cyan0515.householdAccount.infrastructure.Receipts
import cyan0515.householdAccount.infrastructure.Users
import cyan0515.householdAccount.model.category.ICategoryRepository
import cyan0515.householdAccount.model.receipt.IReceiptRepository
import cyan0515.householdAccount.model.service.ReceiptService
import cyan0515.householdAccount.model.user.IUserRepository
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.ktor.plugin.Koin

fun Application.setupKoin() {
    val mods = org.koin.dsl.module {
        single<IUserRepository> { Users }
        single<ICategoryRepository> { Categories }
        single<IReceiptRepository> { Receipts }

        single { ReceiptService(get()) }
    }
    install(Koin) {
        modules(mods)
    }
}
