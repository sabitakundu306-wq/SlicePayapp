package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryRegistry
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.components.TransactionItem
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TransferBlue
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    transactions: List<TransactionEntity>,
    accounts: List<AccountEntity>,
    currencySymbol: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedTypeFilter: String,
    onTypeFilterChange: (String) -> Unit,
    selectedCategoryFilter: String,
    onCategoryFilterChange: (String) -> Unit,
    selectedAccountFilter: String,
    onAccountFilterChange: (String) -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter transactions
    val filteredList = transactions.filter { tx ->
        val matchesSearch = searchQuery.isBlank() ||
                tx.title.contains(searchQuery, ignoreCase = true) ||
                tx.note.contains(searchQuery, ignoreCase = true) ||
                tx.category.contains(searchQuery, ignoreCase = true) ||
                tx.tags.contains(searchQuery, ignoreCase = true)

        val matchesType = selectedTypeFilter == "ALL" || tx.type == selectedTypeFilter
        val matchesCategory = selectedCategoryFilter == "ALL" || tx.category.equals(selectedCategoryFilter, ignoreCase = true)
        val matchesAccount = selectedAccountFilter == "ALL" || tx.accountId == selectedAccountFilter

        matchesSearch && matchesType && matchesCategory && matchesAccount
    }

    // Group filtered items by date header
    val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = dayFormat.format(Date(System.currentTimeMillis()))
    val yesterdayStr = dayFormat.format(Date(System.currentTimeMillis() - 86_400_000L))

    val displayDateFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())

    val groupedByDay = filteredList.groupBy { tx ->
        val key = dayFormat.format(Date(tx.timestamp))
        when (key) {
            todayStr -> "Today"
            yesterdayStr -> "Yesterday"
            else -> displayDateFormat.format(Date(tx.timestamp))
        }
    }

    // Totals for filtered set
    val filteredExpenseSum = filteredList.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
    val filteredIncomeSum = filteredList.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("transactions_screen_root")
    ) {
        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search transactions, payees, tags...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("transaction_search_bar")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Type Filter Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val types = listOf(
                Pair("ALL", "All"),
                Pair(TransactionType.EXPENSE.name, "Expenses"),
                Pair(TransactionType.INCOME.name, "Income"),
                Pair(TransactionType.TRANSFER.name, "Transfers")
            )
            items(types) { (key, label) ->
                val isSelected = selectedTypeFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { onTypeFilterChange(key) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (key) {
                            TransactionType.EXPENSE.name -> ExpenseRed
                            TransactionType.INCOME.name -> IncomeGreen
                            TransactionType.TRANSFER.name -> TransferBlue
                            else -> MaterialTheme.colorScheme.primary
                        },
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_type_$key")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category & Account dropdown quick filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Category Filter Chip
            var isCategoryMenuOpen by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { isCategoryMenuOpen = true },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (selectedCategoryFilter == "ALL") "All Categories" else selectedCategoryFilter,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                androidx.compose.material3.DropdownMenu(
                    expanded = isCategoryMenuOpen,
                    onDismissRequest = { isCategoryMenuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Categories") },
                        onClick = {
                            onCategoryFilterChange("ALL")
                            isCategoryMenuOpen = false
                        }
                    )
                    CategoryRegistry.allCategories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = null,
                                    tint = cat.color,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            onClick = {
                                onCategoryFilterChange(cat.name)
                                isCategoryMenuOpen = false
                            }
                        )
                    }
                }
            }

            // Account Filter Chip
            var isAccountMenuOpen by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { isAccountMenuOpen = true },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val accName = if (selectedAccountFilter == "ALL") "All Accounts" else accounts.find { it.id == selectedAccountFilter }?.name ?: selectedAccountFilter
                        Text(
                            text = accName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                androidx.compose.material3.DropdownMenu(
                    expanded = isAccountMenuOpen,
                    onDismissRequest = { isAccountMenuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Accounts") },
                        onClick = {
                            onAccountFilterChange("ALL")
                            isAccountMenuOpen = false
                        }
                    )
                    accounts.forEach { acc ->
                        DropdownMenuItem(
                            text = { Text(acc.name) },
                            onClick = {
                                onAccountFilterChange(acc.id)
                                isAccountMenuOpen = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Results Summary Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredList.size} Transactions",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (filteredIncomeSum > 0) {
                    Text(
                        text = String.format(Locale.US, "+%s%.2f", currencySymbol, filteredIncomeSum),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen
                    )
                }
                if (filteredExpenseSum > 0) {
                    Text(
                        text = String.format(Locale.US, "-%s%.2f", currencySymbol, filteredExpenseSum),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Transaction List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 60.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text(
                        text = "No transactions found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Try adjusting your search terms or filters",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("transactions_lazy_column"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedByDay.forEach { (dateHeader, dayTxs) ->
                    item(key = "header_$dateHeader") {
                        Text(
                            text = dateHeader,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                        )
                    }

                    items(dayTxs, key = { it.id }) { tx ->
                        TransactionItem(
                            transaction = tx,
                            currencySymbol = currencySymbol,
                            accountName = accounts.find { it.id == tx.accountId }?.name,
                            onClick = { onTransactionClick(tx) },
                            onDelete = { onDeleteTransaction(tx.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB & nav bar
                }
            }
        }
    }
}
