package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BudgetDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.CategoryInfo
import com.example.data.model.CategoryRegistry
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TimeRangeFilter(val label: String) {
    THIS_MONTH("This Month"),
    LAST_7_DAYS("7 Days"),
    LAST_30_DAYS("30 Days"),
    THIS_YEAR("This Year"),
    ALL_TIME("All Time")
}

data class MonthlyComparison(
    val monthIndex: Int, // 0..11
    val monthShortName: String,
    val currentYearExpense: Double,
    val previousYearExpense: Double,
    val currentYearIncome: Double,
    val previousYearIncome: Double
) {
    val expenseDiff: Double get() = currentYearExpense - previousYearExpense
    val expenseChangePct: Double
        get() = if (previousYearExpense > 0) ((currentYearExpense - previousYearExpense) / previousYearExpense) * 100 else 0.0

    val incomeDiff: Double get() = currentYearIncome - previousYearIncome
    val incomeChangePct: Double
        get() = if (previousYearIncome > 0) ((currentYearIncome - previousYearIncome) / previousYearIncome) * 100 else 0.0
}

data class CategoryYearComparison(
    val category: String,
    val categoryInfo: CategoryInfo,
    val currentYearAmount: Double,
    val previousYearAmount: Double,
    val diff: Double,
    val percentChange: Double
)

data class YearOverYearState(
    val selectedYear: Int = 2026,
    val previousYear: Int = 2025,
    val currentYearIncome: Double = 0.0,
    val previousYearIncome: Double = 0.0,
    val incomeChangePct: Double = 0.0,
    val currentYearExpense: Double = 0.0,
    val previousYearExpense: Double = 0.0,
    val expenseChangePct: Double = 0.0,
    val currentYearNet: Double = 0.0,
    val previousYearNet: Double = 0.0,
    val netChangePct: Double = 0.0,
    val currentSavingsRate: Int = 0,
    val previousSavingsRate: Int = 0,
    val monthlyComparisons: List<MonthlyComparison> = emptyList(),
    val categoryComparisons: List<CategoryYearComparison> = emptyList(),
    val availableYears: List<Int> = listOf(2026, 2025),
    val insightText: String = ""
)

data class CategorySpend(
    val category: String,
    val totalAmount: Double,
    val percentage: Float,
    val categoryInfo: CategoryInfo
)

data class DayTrend(
    val dayLabel: String,
    val dateMillis: Long,
    val expense: Double,
    val income: Double
)

data class BudgetProgress(
    val category: String,
    val limit: Double,
    val spent: Double,
    val remaining: Double,
    val progressFraction: Float,
    val isOverBudget: Boolean,
    val categoryInfo: CategoryInfo
)

data class DashboardUiState(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netSavings: Double = 0.0,
    val savingsRatePercentage: Int = 0,
    val totalMonthlyBudget: Double = 0.0,
    val totalMonthlyBudgetSpent: Double = 0.0,
    val monthlyBudgetFraction: Float = 0f,
    val daysRemainingInMonth: Int = 1,
    val safeDailySpendPace: Double = 0.0,
    val categorySpends: List<CategorySpend> = emptyList(),
    val weeklyTrends: List<DayTrend> = emptyList(),
    val budgetProgressList: List<BudgetProgress> = emptyList(),
    val accountBalances: Map<String, Double> = emptyMap(),
    val recentTransactions: List<TransactionEntity> = emptyList()
)

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BudgetRepository

    init {
        val db = BudgetDatabase.getDatabase(application)
        repository = BudgetRepository(
            transactionDao = db.transactionDao(),
            budgetDao = db.budgetDao(),
            accountDao = db.accountDao(),
            categoryDao = db.categoryDao()
        )
        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
        }
    }

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBudgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAccounts: StateFlow<List<AccountEntity>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedTimeRange = MutableStateFlow(TimeRangeFilter.THIS_MONTH)
    val currencySymbol = MutableStateFlow("₹")
    val isDarkTheme = MutableStateFlow(false)

    // Search and filters for Transaction List screen
    val searchQuery = MutableStateFlow("")
    val filterType = MutableStateFlow("ALL")
    val filterCategory = MutableStateFlow("ALL")
    val filterAccountId = MutableStateFlow("ALL")

    val dashboardState: StateFlow<DashboardUiState> = combine(
        allTransactions,
        allBudgets,
        allAccounts,
        allCategories,
        selectedTimeRange
    ) { txs, budgets, accounts, categories, timeRange ->
        computeDashboardState(txs, budgets, accounts, categories, timeRange)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    val selectedComparisonYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))

    val yearOverYearState: StateFlow<YearOverYearState> = combine(
        allTransactions,
        allCategories,
        selectedComparisonYear
    ) { txs, categories, selectedYear ->
        computeYearOverYearState(txs, categories, selectedYear)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), YearOverYearState())

    private fun computeDashboardState(
        allTxs: List<TransactionEntity>,
        budgets: List<BudgetEntity>,
        accounts: List<AccountEntity>,
        categories: List<CategoryEntity>,
        timeRange: TimeRangeFilter
    ): DashboardUiState {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        val filteredTxs = filterByTimeRange(allTxs, timeRange, now)

        var totalIncome = 0.0
        var totalExpense = 0.0

        filteredTxs.forEach { tx ->
            when (tx.type) {
                TransactionType.INCOME.name -> totalIncome += tx.amount
                TransactionType.EXPENSE.name -> totalExpense += tx.amount
            }
        }

        val netSavings = totalIncome - totalExpense
        val savingsRate = if (totalIncome > 0) {
            (((totalIncome - totalExpense) / totalIncome) * 100).toInt().coerceIn(-100, 100)
        } else 0

        // Compute Account Balances across ALL history
        val balanceMap = mutableMapOf<String, Double>()
        accounts.forEach { acc ->
            balanceMap[acc.id] = acc.initialBalance
        }

        allTxs.forEach { tx ->
            when (tx.type) {
                TransactionType.INCOME.name -> {
                    val current = balanceMap[tx.accountId] ?: 0.0
                    balanceMap[tx.accountId] = current + tx.amount
                }
                TransactionType.EXPENSE.name -> {
                    val current = balanceMap[tx.accountId] ?: 0.0
                    balanceMap[tx.accountId] = current - tx.amount
                }
                TransactionType.TRANSFER.name -> {
                    val source = balanceMap[tx.accountId] ?: 0.0
                    balanceMap[tx.accountId] = source - tx.amount
                    if (!tx.toAccountId.isNullOrBlank()) {
                        val dest = balanceMap[tx.toAccountId] ?: 0.0
                        balanceMap[tx.toAccountId] = dest + tx.amount
                    }
                }
            }
        }

        val totalNetWorth = balanceMap.values.sum()

        // Category Spends breakdown
        val expenseTxs = filteredTxs.filter { it.type == TransactionType.EXPENSE.name }
        val categoryGrouped = expenseTxs.groupBy { it.category }
        val categorySpends = categoryGrouped.map { (cat, list) ->
            val sum = list.sumOf { it.amount }
            val pct = if (totalExpense > 0) (sum / totalExpense).toFloat() else 0f
            CategorySpend(
                category = cat,
                totalAmount = sum,
                percentage = pct,
                categoryInfo = CategoryRegistry.getCategory(cat, categories)
            )
        }.sortedByDescending { it.totalAmount }

        // Monthly Budget calculations (Current Month)
        val monthStart = getStartOfMonthMillis()
        val monthEnd = getEndOfMonthMillis()
        val currentMonthTxs = allTxs.filter {
            it.timestamp in monthStart..monthEnd && it.type == TransactionType.EXPENSE.name
        }

        val monthSpentByCategory: Map<String, Double> = currentMonthTxs.groupBy { it.category }
            .mapValues { (_, list: List<TransactionEntity>) -> list.sumOf { tx: TransactionEntity -> tx.amount } }

        val totalBudgetSum = budgets.sumOf { it.monthlyLimit }
        val totalBudgetSpent = currentMonthTxs.sumOf { it.amount }
        val budgetFraction = if (totalBudgetSum > 0) {
            (totalBudgetSpent / totalBudgetSum).toFloat().coerceIn(0f, 1.5f)
        } else 0f

        calendar.timeInMillis = now
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val daysLeft = (daysInMonth - currentDay).coerceAtLeast(1)
        val remainingBudget = (totalBudgetSum - totalBudgetSpent).coerceAtLeast(0.0)
        val safeDailySpendPace = if (daysLeft > 0) remainingBudget / daysLeft else 0.0

        val budgetProgressList = budgets.map { b ->
            val spent = monthSpentByCategory[b.category] ?: 0.0
            val frac = if (b.monthlyLimit > 0) (spent / b.monthlyLimit).toFloat() else 0f
            BudgetProgress(
                category = b.category,
                limit = b.monthlyLimit,
                spent = spent,
                remaining = b.monthlyLimit - spent,
                progressFraction = frac.coerceIn(0f, 1f),
                isOverBudget = spent > b.monthlyLimit,
                categoryInfo = CategoryRegistry.getCategory(b.category, categories)
            )
        }.sortedByDescending { it.progressFraction }

        // 7-day spending & income trend
        val weeklyTrends = compute7DayTrends(allTxs, now)

        return DashboardUiState(
            totalBalance = totalNetWorth,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netSavings = netSavings,
            savingsRatePercentage = savingsRate,
            totalMonthlyBudget = totalBudgetSum,
            totalMonthlyBudgetSpent = totalBudgetSpent,
            monthlyBudgetFraction = budgetFraction,
            daysRemainingInMonth = daysLeft,
            safeDailySpendPace = safeDailySpendPace,
            categorySpends = categorySpends,
            weeklyTrends = weeklyTrends,
            budgetProgressList = budgetProgressList,
            accountBalances = balanceMap,
            recentTransactions = filteredTxs.take(5)
        )
    }

    private fun compute7DayTrends(allTxs: List<TransactionEntity>, nowMillis: Long): List<DayTrend> {
        val list = mutableListOf<DayTrend>()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val cal = Calendar.getInstance()

        for (i in 6 downTo 0) {
            cal.timeInMillis = nowMillis - (i * 86_400_000L)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val dayStart = cal.timeInMillis

            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val dayEnd = cal.timeInMillis

            val dayTxs = allTxs.filter { it.timestamp in dayStart..dayEnd }
            val expense = dayTxs.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
            val income = dayTxs.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }

            val label = if (i == 0) "Today" else dayFormat.format(Date(dayStart))
            list.add(DayTrend(label, dayStart, expense, income))
        }
        return list
    }

    private fun filterByTimeRange(
        txs: List<TransactionEntity>,
        timeRange: TimeRangeFilter,
        now: Long
    ): List<TransactionEntity> {
        return when (timeRange) {
            TimeRangeFilter.ALL_TIME -> txs
            TimeRangeFilter.THIS_MONTH -> {
                val start = getStartOfMonthMillis()
                val end = getEndOfMonthMillis()
                txs.filter { it.timestamp in start..end }
            }
            TimeRangeFilter.LAST_7_DAYS -> {
                val start = now - (7 * 86_400_000L)
                txs.filter { it.timestamp >= start }
            }
            TimeRangeFilter.LAST_30_DAYS -> {
                val start = now - (30 * 86_400_000L)
                txs.filter { it.timestamp >= start }
            }
            TimeRangeFilter.THIS_YEAR -> {
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                txs.filter { it.timestamp >= start }
            }
        }
    }

    private fun computeYearOverYearState(
        allTxs: List<TransactionEntity>,
        categories: List<CategoryEntity>,
        selectedYear: Int
    ): YearOverYearState {
        val previousYear = selectedYear - 1
        val cal = Calendar.getInstance()

        // Extract available years from transactions plus selected & previous year
        val availableYearsSet = mutableSetOf(selectedYear, previousYear)
        allTxs.forEach { tx ->
            cal.timeInMillis = tx.timestamp
            availableYearsSet.add(cal.get(Calendar.YEAR))
        }
        val availableYears = availableYearsSet.sortedDescending()

        // Filter transactions by year
        val currentYearTxs = allTxs.filter {
            cal.timeInMillis = it.timestamp
            cal.get(Calendar.YEAR) == selectedYear
        }

        val previousYearTxs = allTxs.filter {
            cal.timeInMillis = it.timestamp
            cal.get(Calendar.YEAR) == previousYear
        }

        val currentIncome = currentYearTxs.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
        val currentExpense = currentYearTxs.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
        val currentNet = currentIncome - currentExpense
        val currentSavingsRate = if (currentIncome > 0) (((currentIncome - currentExpense) / currentIncome) * 100).toInt().coerceIn(-100, 100) else 0

        val previousIncome = previousYearTxs.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
        val previousExpense = previousYearTxs.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
        val previousNet = previousIncome - previousExpense
        val previousSavingsRate = if (previousIncome > 0) (((previousIncome - previousExpense) / previousIncome) * 100).toInt().coerceIn(-100, 100) else 0

        val incomeChangePct = if (previousIncome > 0) {
            ((currentIncome - previousIncome) / previousIncome) * 100
        } else if (currentIncome > 0) 100.0 else 0.0

        val expenseChangePct = if (previousExpense > 0) {
            ((currentExpense - previousExpense) / previousExpense) * 100
        } else if (currentExpense > 0) 100.0 else 0.0

        val netChangePct = if (Math.abs(previousNet) > 0) {
            ((currentNet - previousNet) / Math.abs(previousNet)) * 100
        } else 0.0

        // 12 Months comparison
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val monthlyList = (0..11).map { monthIdx ->
            val curExp = currentYearTxs.filter {
                cal.timeInMillis = it.timestamp
                cal.get(Calendar.MONTH) == monthIdx && it.type == TransactionType.EXPENSE.name
            }.sumOf { it.amount }

            val prevExp = previousYearTxs.filter {
                cal.timeInMillis = it.timestamp
                cal.get(Calendar.MONTH) == monthIdx && it.type == TransactionType.EXPENSE.name
            }.sumOf { it.amount }

            val curInc = currentYearTxs.filter {
                cal.timeInMillis = it.timestamp
                cal.get(Calendar.MONTH) == monthIdx && it.type == TransactionType.INCOME.name
            }.sumOf { it.amount }

            val prevInc = previousYearTxs.filter {
                cal.timeInMillis = it.timestamp
                cal.get(Calendar.MONTH) == monthIdx && it.type == TransactionType.INCOME.name
            }.sumOf { it.amount }

            MonthlyComparison(
                monthIndex = monthIdx,
                monthShortName = monthNames[monthIdx],
                currentYearExpense = curExp,
                previousYearExpense = prevExp,
                currentYearIncome = curInc,
                previousYearIncome = prevInc
            )
        }

        // Category comparisons (expenses)
        val allExpenseCategories = (currentYearTxs.filter { it.type == TransactionType.EXPENSE.name }.map { it.category } +
                previousYearTxs.filter { it.type == TransactionType.EXPENSE.name }.map { it.category }).distinct()

        val categoryList = allExpenseCategories.map { cat ->
            val cur = currentYearTxs.filter { it.type == TransactionType.EXPENSE.name && it.category == cat }.sumOf { it.amount }
            val prev = previousYearTxs.filter { it.type == TransactionType.EXPENSE.name && it.category == cat }.sumOf { it.amount }
            val diff = cur - prev
            val pct = if (prev > 0) ((cur - prev) / prev) * 100 else if (cur > 0) 100.0 else 0.0
            CategoryYearComparison(
                category = cat,
                categoryInfo = CategoryRegistry.getCategory(cat, categories),
                currentYearAmount = cur,
                previousYearAmount = prev,
                diff = diff,
                percentChange = pct
            )
        }.sortedByDescending { Math.abs(it.diff) }

        val insight = when {
            previousExpense > 0 && expenseChangePct < 0 ->
                "Great discipline! Annual expenses decreased by ${String.format(Locale.US, "%.1f", Math.abs(expenseChangePct))}% compared to $previousYear."
            previousExpense > 0 && expenseChangePct > 0 ->
                "Annual expenses increased by ${String.format(Locale.US, "%.1f", expenseChangePct)}% compared to $previousYear."
            currentExpense > 0 && previousExpense == 0.0 ->
                "Annual tracking active for $selectedYear. Compare with $previousYear as you log more history."
            else ->
                "Compare financial trends, monthly momentum, and category budgets across $selectedYear and $previousYear."
        }

        return YearOverYearState(
            selectedYear = selectedYear,
            previousYear = previousYear,
            currentYearIncome = currentIncome,
            previousYearIncome = previousIncome,
            incomeChangePct = incomeChangePct,
            currentYearExpense = currentExpense,
            previousYearExpense = previousExpense,
            expenseChangePct = expenseChangePct,
            currentYearNet = currentNet,
            previousYearNet = previousNet,
            netChangePct = netChangePct,
            currentSavingsRate = currentSavingsRate,
            previousSavingsRate = previousSavingsRate,
            monthlyComparisons = monthlyList,
            categoryComparisons = categoryList,
            availableYears = availableYears,
            insightText = insight
        )
    }

    fun setComparisonYear(year: Int) {
        selectedComparisonYear.value = year
    }

    private fun getStartOfMonthMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getEndOfMonthMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    // Mutations
    fun addTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        accountId: String,
        toAccountId: String? = null,
        timestamp: Long = System.currentTimeMillis(),
        note: String = "",
        tags: String = "",
        isRecurring: Boolean = false,
        recurringFrequency: String? = null
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                title = title.trim(),
                amount = amount,
                type = type,
                category = category,
                accountId = accountId,
                toAccountId = toAccountId,
                timestamp = timestamp,
                note = note.trim(),
                tags = tags.trim(),
                isRecurring = isRecurring,
                recurringFrequency = recurringFrequency
            )
            repository.addTransaction(entity)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun saveBudget(category: String, limit: Double) {
        viewModelScope.launch {
            val entity = BudgetEntity(
                category = category,
                monthlyLimit = limit
            )
            repository.saveBudget(entity)
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            repository.deleteBudget(id)
        }
    }

    fun setCurrency(symbol: String) {
        currencySymbol.value = symbol
    }

    fun addAccount(
        id: String,
        name: String,
        type: String,
        initialBalance: Double,
        colorHex: String,
        iconName: String
    ) {
        viewModelScope.launch {
            val account = AccountEntity(
                id = id,
                name = name,
                type = type,
                initialBalance = initialBalance,
                colorHex = colorHex,
                iconName = iconName
            )
            repository.saveAccount(account)
        }
    }

    fun deleteAccount(id: String) {
        viewModelScope.launch {
            repository.deleteAccount(id)
        }
    }

    fun addCustomCategory(name: String, iconKey: String, colorHex: String, isExpense: Boolean) {
        viewModelScope.launch {
            val entity = CategoryEntity(
                name = name.trim(),
                iconKey = iconKey,
                colorHex = colorHex,
                isExpense = isExpense,
                isCustom = true
            )
            repository.addCategory(entity)
        }
    }

    fun deleteCustomCategory(id: Long) {
        viewModelScope.launch {
            repository.deleteCategory(id)
        }
    }

    fun setTimeRange(range: TimeRangeFilter) {
        selectedTimeRange.value = range
    }

    fun toggleDarkTheme() {
        isDarkTheme.value = !isDarkTheme.value
    }

    fun setDarkTheme(enabled: Boolean) {
        isDarkTheme.value = enabled
    }

    fun resetToSampleData() {
        viewModelScope.launch {
            repository.clearAllData()
            repository.initializeDefaultDataIfEmpty()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    fun exportTransactionsCsv(context: Context) {
        viewModelScope.launch {
            try {
                val txs = allTransactions.value
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val sb = StringBuilder()
                sb.append("ID,Date,Title,Type,Category,Amount,Account,ToAccount,Note,Tags\n")
                txs.forEach { tx ->
                    sb.append("${tx.id},")
                    sb.append("\"${dateFormat.format(Date(tx.timestamp))}\",")
                    sb.append("\"${tx.title.replace("\"", "\"\"")}\",")
                    sb.append("${tx.type},")
                    sb.append("\"${tx.category}\",")
                    sb.append("${tx.amount},")
                    sb.append("${tx.accountId},")
                    sb.append("${tx.toAccountId ?: ""},")
                    sb.append("\"${tx.note.replace("\"", "\"\"")}\",")
                    sb.append("\"${tx.tags.replace("\"", "\"\"")}\"\n")
                }

                val cacheDir = context.cacheDir
                val file = File(cacheDir, "SlicePay_Export_${System.currentTimeMillis()}.csv")
                FileWriter(file).use { it.write(sb.toString()) }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "SlicePay Transactions Export")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(sendIntent, "Share SlicePay CSV")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
