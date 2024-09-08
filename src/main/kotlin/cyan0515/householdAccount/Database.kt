package cyan0515.householdAccount

import com.typesafe.config.ConfigFactory
import cyan0515.householdAccount.infrastructure.Categories
import cyan0515.householdAccount.infrastructure.ReceiptDetails
import cyan0515.householdAccount.infrastructure.Receipts
import cyan0515.householdAccount.infrastructure.Users
import cyan0515.householdAccount.model.category.Category
import cyan0515.householdAccount.model.category.ICategoryRepository
import io.ktor.server.application.Application
import io.ktor.server.config.tryGetString
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject

fun Application.setupDatabase() {

    val categoryRepository by inject<ICategoryRepository>()

    val config = ConfigFactory.load()
    val dbUrl = config.tryGetString("db.url") ?: "jdbc:postgresql://localhost:5432/household_db"
    val dbUser = config.getString("db.user")
    val dbPassword = config.tryGetString("db.password") ?: "password"

    Database.connect(
        url = dbUrl,
        driver = "org.postgresql.Driver",
        user = dbUser,
        password = dbPassword
    )

    transaction {
        SchemaUtils.create(Users, Categories, Receipts, ReceiptDetails)
        if (!isInitialized()) {
            categoryRepository.create(Category("食費"))
            categoryRepository.create(Category("衣料品費"))
            categoryRepository.create(Category("住居費"))
            categoryRepository.create(Category("水道光熱費"))
            categoryRepository.create(Category("交通費"))
            categoryRepository.create(Category("医療費"))
            categoryRepository.create(Category("教育費"))
            categoryRepository.create(Category("娯楽費"))
            categoryRepository.create(Category("通信費"))
            categoryRepository.create(Category("保険料"))
            categoryRepository.create(Category("税金"))
            categoryRepository.create(Category("借入返済"))
        }
    }
}

private fun isInitialized(): Boolean {
    return Categories.selectAll().toList().isNotEmpty()
}
