package com.example.data.repository

import com.example.data.local.AccountDao
import com.example.data.local.BudgetDao
import com.example.data.local.CategoryDao
import com.example.data.local.TransactionDao
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.CategoryRegistry
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class BudgetRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()
    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun addTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun saveBudget(budget: BudgetEntity): Long {
        return budgetDao.insertBudget(budget)
    }

    suspend fun deleteBudget(id: Long) {
        budgetDao.deleteBudgetById(id)
    }

    suspend fun saveAccount(account: AccountEntity) {
        accountDao.insertAccount(account)
    }

    suspend fun deleteAccount(id: String) {
        accountDao.deleteAccountById(id)
    }

    suspend fun addCategory(category: CategoryEntity): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun deleteCategory(id: Long) {
        categoryDao.deleteCategoryById(id)
    }

    suspend fun clearAllAccounts() {
        accountDao.clearAllAccounts()
    }

    suspend fun clearAllData() {
        transactionDao.clearAllTransactions()
        budgetDao.clearAllBudgets()
        accountDao.clearAllAccounts()
    }

    suspend fun initializeDefaultDataIfEmpty() {
        val catCount = categoryDao.getCategoryCount()
        if (catCount == 0) {
            val allDefault = CategoryRegistry.defaultExpenseCategories + CategoryRegistry.defaultIncomeCategories
            categoryDao.insertCategories(allDefault)
        }

        // Clean any previously seeded sample accounts so only user creates accounts
        accountDao.deleteAccountById("checking")
        accountDao.deleteAccountById("savings")
        accountDao.deleteAccountById("cash")
        accountDao.deleteAccountById("credit_card")

        // Clean any previously seeded sample transactions to ensure zero start
        transactionDao.clearAllTransactions()
        budgetDao.clearAllBudgets()
    }

    suspend fun seedSampleTransactions() {
        val now = System.currentTimeMillis()
        val oneDayMillis = 86_400_000L

        val sampleTransactions = listOf(
            TransactionEntity(
                title = "Tech Corp Payroll",
                amount = 4200.00,
                type = TransactionType.INCOME.name,
                category = "Salary",
                accountId = "checking",
                timestamp = now - (oneDayMillis * 9),
                note = "Bi-weekly direct deposit",
                tags = "payroll, work"
            ),
            TransactionEntity(
                title = "Whole Foods Market",
                amount = 142.50,
                type = TransactionType.EXPENSE.name,
                category = "Groceries",
                accountId = "checking",
                timestamp = now - (oneDayMillis * 1),
                note = "Organic produce, pantry supplies",
                tags = "groceries, home"
            ),
            TransactionEntity(
                title = "Downtown Bistro Dinner",
                amount = 68.20,
                type = TransactionType.EXPENSE.name,
                category = "Dining Out",
                accountId = "credit_card",
                timestamp = now - (oneDayMillis * 2),
                note = "Dinner with team",
                tags = "food, social"
            ),
            TransactionEntity(
                title = "Electric & Energy Bill",
                amount = 115.00,
                type = TransactionType.EXPENSE.name,
                category = "Bills & Utilities",
                accountId = "checking",
                timestamp = now - (oneDayMillis * 4),
                note = "Monthly electricity statement",
                tags = "bills, home",
                isRecurring = true,
                recurringFrequency = "MONTHLY"
            ),
            TransactionEntity(
                title = "Metro Transit Pass",
                amount = 65.00,
                type = TransactionType.EXPENSE.name,
                category = "Transport",
                accountId = "checking",
                timestamp = now - (oneDayMillis * 5),
                note = "Monthly commuter transit recharge",
                tags = "commute"
            ),
            TransactionEntity(
                title = "Freelance UI Design",
                amount = 750.00,
                type = TransactionType.INCOME.name,
                category = "Freelance",
                accountId = "checking",
                timestamp = now - (oneDayMillis * 6),
                note = "Mobile app landing page gig",
                tags = "freelance, design"
            ),
            TransactionEntity(
                title = "Trader Joe's",
                amount = 78.40,
                type = TransactionType.EXPENSE.name,
                category = "Groceries",
                accountId = "checking",
                timestamp = now - (oneDayMillis * 7),
                note = "Snacks and breakfast items",
                tags = "groceries"
            ),
            TransactionEntity(
                title = "Gym Membership",
                amount = 45.00,
                type = TransactionType.EXPENSE.name,
                category = "Health & Medical",
                accountId = "credit_card",
                timestamp = now - (oneDayMillis * 8),
                note = "Equinox monthly subscription",
                tags = "fitness, health",
                isRecurring = true,
                recurringFrequency = "MONTHLY"
            ),
            TransactionEntity(
                title = "Amazon Online Order",
                amount = 89.90,
                type = TransactionType.EXPENSE.name,
                category = "Shopping",
                accountId = "credit_card",
                timestamp = now - (oneDayMillis * 3),
                note = "Home office desk accessories",
                tags = "shopping, tech"
            ),
            TransactionEntity(
                title = "Coffee & Bakery",
                amount = 14.50,
                type = TransactionType.EXPENSE.name,
                category = "Dining Out",
                accountId = "cash",
                timestamp = now - (1000 * 3600 * 3), // 3 hours ago
                note = "Morning espresso & croissant",
                tags = "coffee"
            ),
            TransactionEntity(
                title = "Movie Night IMAX",
                amount = 36.00,
                type = TransactionType.EXPENSE.name,
                category = "Entertainment",
                accountId = "checking",
                timestamp = now - (oneDayMillis * 10),
                note = "Weekend cinema tickets",
                tags = "movies"
            ),
            TransactionEntity(
                title = "Transfer to Emergency Fund",
                amount = 400.00,
                type = TransactionType.TRANSFER.name,
                category = "Savings",
                accountId = "checking",
                toAccountId = "savings",
                timestamp = now - (oneDayMillis * 9),
                note = "Automated monthly wealth transfer"
            )
        )
        transactionDao.insertTransactions(sampleTransactions)
    }
}
