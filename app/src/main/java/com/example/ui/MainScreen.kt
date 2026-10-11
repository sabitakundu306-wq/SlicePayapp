package com.example.ui

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.CompareArrows
import com.example.data.model.AccountEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.AddEditAccountDialog
import com.example.ui.components.AddTransactionSheet
import com.example.ui.components.SetBudgetDialog
import com.example.ui.components.TransferDialog
import com.example.ui.components.YearComparisonDetailSheet
import com.example.ui.screens.AccountsScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.BudgetViewModel
import kotlinx.coroutines.launch

enum class AppNavigationTab(
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    OVERVIEW("Overview", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    TRANSACTIONS("Transactions", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong),
    BUDGETS("Budgets", Icons.Filled.PieChart, Icons.Outlined.PieChart),
    ACCOUNTS("Accounts", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentTab by remember { mutableStateOf(AppNavigationTab.OVERVIEW) }

    // Dialog & BottomSheet state
    var showAddTransactionSheet by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }

    var showSetBudgetDialog by remember { mutableStateOf(false) }
    var budgetToEdit by remember { mutableStateOf<BudgetEntity?>(null) }

    var showTransferDialog by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<AccountEntity?>(null) }
    var showYearComparisonSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Collect States
    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val yearOverYearState by viewModel.yearOverYearState.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val allBudgets by viewModel.allBudgets.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val selectedTimeRange by viewModel.selectedTimeRange.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterType by viewModel.filterType.collectAsStateWithLifecycle()
    val filterCategory by viewModel.filterCategory.collectAsStateWithLifecycle()
    val filterAccountId by viewModel.filterAccountId.collectAsStateWithLifecycle()

    // Handle Android system back button
    BackHandler(enabled = currentTab != AppNavigationTab.OVERVIEW) {
        currentTab = AppNavigationTab.OVERVIEW
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Wallet,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SlicePay",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Theme Toggle
                    IconButton(
                        onClick = { viewModel.toggleDarkTheme() },
                        modifier = Modifier.testTag("theme_toggle_top_btn")
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDark) "Switch to Light Theme" else "Switch to Dark Theme",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // YoY Comparison
                    IconButton(
                        onClick = { showYearComparisonSheet = true },
                        modifier = Modifier.testTag("yoy_top_app_bar_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = "Year-over-Year Comparison",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Export CSV
                    IconButton(
                        onClick = {
                            viewModel.exportTransactionsCsv(context)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Exporting CSV report...")
                            }
                        },
                        modifier = Modifier.testTag("export_csv_top_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export CSV",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("main_bottom_nav_bar")
            ) {
                AppNavigationTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    transactionToEdit = null
                    showAddTransactionSheet = true
                },
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                modifier = Modifier.testTag("main_add_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabContent"
            ) { tab ->
                when (tab) {
                    AppNavigationTab.OVERVIEW -> {
                        DashboardScreen(
                            state = dashboardState,
                            currencySymbol = currencySymbol,
                            selectedTimeRange = selectedTimeRange,
                            onTimeRangeSelected = { viewModel.setTimeRange(it) },
                            onAddExpense = {
                                transactionToEdit = null
                                showAddTransactionSheet = true
                            },
                            onAddIncome = {
                                transactionToEdit = null
                                showAddTransactionSheet = true
                            },
                            onQuickTransfer = {
                                showTransferDialog = true
                            },
                            onViewAllTransactions = {
                                currentTab = AppNavigationTab.TRANSACTIONS
                            },
                            onTransactionClick = { tx ->
                                transactionToEdit = tx
                                showAddTransactionSheet = true
                            },
                            onDeleteTransaction = { id ->
                                viewModel.deleteTransaction(id)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Transaction deleted")
                                }
                            },
                            yoyState = yearOverYearState,
                            onViewDetailedComparison = { showYearComparisonSheet = true }
                        )
                    }

                    AppNavigationTab.TRANSACTIONS -> {
                        TransactionsScreen(
                            transactions = allTransactions,
                            accounts = allAccounts,
                            currencySymbol = currencySymbol,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.searchQuery.value = it },
                            selectedTypeFilter = filterType,
                            onTypeFilterChange = { viewModel.filterType.value = it },
                            selectedCategoryFilter = filterCategory,
                            onCategoryFilterChange = { viewModel.filterCategory.value = it },
                            selectedAccountFilter = filterAccountId,
                            onAccountFilterChange = { viewModel.filterAccountId.value = it },
                            onTransactionClick = { tx ->
                                transactionToEdit = tx
                                showAddTransactionSheet = true
                            },
                            onDeleteTransaction = { id ->
                                viewModel.deleteTransaction(id)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Transaction deleted")
                                }
                            }
                        )
                    }

                    AppNavigationTab.BUDGETS -> {
                        BudgetsScreen(
                            state = dashboardState,
                            rawBudgets = allBudgets,
                            currencySymbol = currencySymbol,
                            onAddBudget = {
                                budgetToEdit = null
                                showSetBudgetDialog = true
                            },
                            onEditBudget = { b ->
                                budgetToEdit = b
                                showSetBudgetDialog = true
                            },
                            onDeleteBudget = { id ->
                                viewModel.deleteBudget(id)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Budget limit removed")
                                }
                            }
                        )
                    }

                    AppNavigationTab.ACCOUNTS -> {
                        AccountsScreen(
                            accounts = allAccounts,
                            accountBalances = dashboardState.accountBalances,
                            currencySymbol = currencySymbol,
                            onCurrencyChange = { viewModel.setCurrency(it) },
                            onOpenTransferDialog = { showTransferDialog = true },
                            onAddAccount = {
                                accountToEdit = null
                                showAddAccountDialog = true
                            },
                            onEditAccount = { acc ->
                                accountToEdit = acc
                                showAddAccountDialog = true
                            },
                            onDeleteAccount = { id ->
                                viewModel.deleteAccount(id)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Account deleted")
                                }
                            },
                            onClearAllData = {
                                viewModel.clearAllData()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("All financial data cleared")
                                }
                            },
                            isDarkTheme = isDark,
                            onToggleDarkTheme = { viewModel.toggleDarkTheme() }
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Add or Edit Transaction
    if (showAddTransactionSheet) {
        AddTransactionSheet(
            sheetState = sheetState,
            currencySymbol = currencySymbol,
            accounts = allAccounts,
            initialTransaction = transactionToEdit,
            onAddNewAccount = {
                showAddAccountDialog = true
            },
            onDismiss = {
                showAddTransactionSheet = false
                transactionToEdit = null
            },
            onSave = { title, amount, type, category, accountId, toAccountId, note, tags, isRecurring ->
                if (transactionToEdit == null) {
                    viewModel.addTransaction(
                        title = title,
                        amount = amount,
                        type = type,
                        category = category,
                        accountId = accountId,
                        toAccountId = toAccountId,
                        note = note,
                        tags = tags,
                        isRecurring = isRecurring
                    )
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Transaction added successfully")
                    }
                } else {
                    val updated = transactionToEdit!!.copy(
                        title = title,
                        amount = amount,
                        type = type,
                        category = category,
                        accountId = accountId,
                        toAccountId = toAccountId,
                        note = note,
                        tags = tags,
                        isRecurring = isRecurring
                    )
                    viewModel.updateTransaction(updated)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Transaction updated")
                    }
                }
                showAddTransactionSheet = false
                transactionToEdit = null
            }
        )
    }

    // Set Budget Dialog
    if (showSetBudgetDialog) {
        SetBudgetDialog(
            currencySymbol = currencySymbol,
            existingBudget = budgetToEdit,
            onDismiss = {
                showSetBudgetDialog = false
                budgetToEdit = null
            },
            onConfirm = { category, limit ->
                viewModel.saveBudget(category, limit)
                showSetBudgetDialog = false
                budgetToEdit = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Budget saved for $category")
                }
            }
        )
    }

    // Transfer Dialog
    if (showTransferDialog) {
        TransferDialog(
            currencySymbol = currencySymbol,
            accounts = allAccounts,
            onDismiss = { showTransferDialog = false },
            onConfirm = { fromId, toId, amount, note ->
                viewModel.addTransaction(
                    title = "Transfer to ${allAccounts.find { it.id == toId }?.name ?: toId}",
                    amount = amount,
                    type = "TRANSFER",
                    category = "Transfer",
                    accountId = fromId,
                    toAccountId = toId,
                    note = note
                )
                showTransferDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Transferred $currencySymbol$amount successfully")
                }
            }
        )
    }

    // Add / Edit Account Dialog
    if (showAddAccountDialog) {
        AddEditAccountDialog(
            currencySymbol = currencySymbol,
            existingAccount = accountToEdit,
            onDismiss = {
                showAddAccountDialog = false
                accountToEdit = null
            },
            onSave = { id, name, type, balance, colorHex, iconName ->
                viewModel.addAccount(id, name, type, balance, colorHex, iconName)
                showAddAccountDialog = false
                accountToEdit = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Account '$name' saved")
                }
            }
        )
    }

    // Year-over-Year Detail Sheet
    if (showYearComparisonSheet) {
        YearComparisonDetailSheet(
            yoyState = yearOverYearState,
            currencySymbol = currencySymbol,
            onYearChange = { viewModel.setComparisonYear(it) },
            onDismiss = { showYearComparisonSheet = false }
        )
    }
}
