package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryRegistry
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TransferBlue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionSheet(
    sheetState: SheetState,
    currencySymbol: String,
    accounts: List<AccountEntity>,
    initialTransaction: TransactionEntity? = null,
    onAddNewAccount: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        amount: Double,
        type: String,
        category: String,
        accountId: String,
        toAccountId: String?,
        note: String,
        tags: String,
        isRecurring: Boolean
    ) -> Unit
) {
    var selectedType by remember {
        mutableStateOf(initialTransaction?.type ?: TransactionType.EXPENSE.name)
    }
    var amountText by remember {
        mutableStateOf(if (initialTransaction != null) String.format("%.2f", initialTransaction.amount) else "")
    }
    var titleText by remember {
        mutableStateOf(initialTransaction?.title ?: "")
    }
    var selectedCategory by remember {
        mutableStateOf(initialTransaction?.category ?: if (selectedType == TransactionType.INCOME.name) "Salary" else "Groceries")
    }
    var selectedAccountId by remember {
        mutableStateOf(initialTransaction?.accountId ?: (accounts.firstOrNull()?.id ?: ""))
    }
    var selectedToAccountId by remember {
        mutableStateOf(initialTransaction?.toAccountId ?: (accounts.getOrNull(1)?.id ?: accounts.firstOrNull()?.id ?: ""))
    }
    var noteText by remember {
        mutableStateOf(initialTransaction?.note ?: "")
    }
    var tagsText by remember {
        mutableStateOf(initialTransaction?.tags ?: "")
    }
    var isRecurring by remember {
        mutableStateOf(initialTransaction?.isRecurring ?: false)
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = if (selectedType == TransactionType.INCOME.name) {
        CategoryRegistry.incomeCategories
    } else {
        CategoryRegistry.expenseCategories
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("add_transaction_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialTransaction == null) "New Transaction" else "Edit Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Transaction Type Selector (Segmented Row)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val types = listOf(
                    Triple(TransactionType.EXPENSE.name, "Expense", ExpenseRed),
                    Triple(TransactionType.INCOME.name, "Income", IncomeGreen),
                    Triple(TransactionType.TRANSFER.name, "Transfer", TransferBlue)
                )

                types.forEach { (typeKey, label, color) ->
                    val isSelected = selectedType == typeKey
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedType = typeKey
                                if (typeKey == TransactionType.INCOME.name) {
                                    selectedCategory = "Salary"
                                } else if (typeKey == TransactionType.EXPENSE.name) {
                                    selectedCategory = "Groceries"
                                }
                            }
                            .testTag("type_selector_$typeKey"),
                        color = if (isSelected) color else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Amount Input Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currencySymbol,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (selectedType) {
                                TransactionType.INCOME.name -> IncomeGreen
                                TransactionType.EXPENSE.name -> ExpenseRed
                                else -> TransferBlue
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
                                    amountText = input
                                    errorMessage = null
                                }
                            },
                            placeholder = { Text("0.00", fontSize = 28.sp) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .width(200.dp)
                                .testTag("amount_input_field")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Add Pills
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(10, 25, 50, 100).forEach { bump ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val cur = amountText.toDoubleOrNull() ?: 0.0
                                        amountText = String.format(java.util.Locale.US, "%.2f", cur + bump)
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    text = "+$bump",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title / Payee
            OutlinedTextField(
                value = titleText,
                onValueChange = { titleText = it },
                label = { Text("Title / Payee") },
                placeholder = {
                    Text(if (selectedType == TransactionType.INCOME.name) "e.g. Salary, Client payment" else "e.g. Whole Foods, Uber")
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("title_input_field")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Category Selection (Only if not transfer)
            if (selectedType != TransactionType.TRANSFER.name) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat.name
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat.name },
                            label = { Text(cat.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = cat.name,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isSelected) Color.White else cat.color
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = cat.color,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("category_chip_${cat.name}")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Account Selection (Horizontal Single-Line)
            Text(
                text = if (selectedType == TransactionType.TRANSFER.name) "Transfer From" else "Account / Payment Method",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (accounts.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "No account created yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (onAddNewAccount != null) {
                        Button(
                            onClick = onAddNewAccount,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("sheet_create_account_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Create Account", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { acc ->
                        val isSelected = selectedAccountId == acc.id
                        val accIcon = when (acc.type) {
                            "SAVINGS" -> Icons.Default.Savings
                            "CASH" -> Icons.Default.AccountBalanceWallet
                            "CREDIT" -> Icons.Default.CreditCard
                            else -> Icons.Default.AccountBalance
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedAccountId = acc.id },
                            label = {
                                Text(
                                    text = acc.name,
                                    maxLines = 1,
                                    softWrap = false,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = accIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier.testTag("account_chip_${acc.id}")
                        )
                    }

                    if (onAddNewAccount != null) {
                        FilterChip(
                            selected = false,
                            onClick = onAddNewAccount,
                            label = { Text("+ Add") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            modifier = Modifier.testTag("sheet_add_account_chip")
                        )
                    }
                }
            }

            // Transfer To Account (Horizontal Single-Line)
            if (selectedType == TransactionType.TRANSFER.name) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Transfer To",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { acc ->
                        val isSelected = selectedToAccountId == acc.id
                        val accIcon = when (acc.type) {
                            "SAVINGS" -> Icons.Default.Savings
                            "CASH" -> Icons.Default.AccountBalanceWallet
                            "CREDIT" -> Icons.Default.CreditCard
                            else -> Icons.Default.AccountBalance
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedToAccountId = acc.id },
                            label = {
                                Text(
                                    text = acc.name,
                                    maxLines = 1,
                                    softWrap = false,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = accIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier.testTag("to_account_chip_${acc.id}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Note & Tags
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Note / Description (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("note_input_field")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = tagsText,
                onValueChange = { tagsText = it },
                label = { Text("Tags (e.g. travel, dinner, work)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("tags_input_field")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Recurring Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Recurring Transaction",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Repeats monthly",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isRecurring,
                    onCheckedChange = { isRecurring = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("recurring_switch")
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save Button
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        errorMessage = "Please enter a valid amount greater than 0"
                        return@Button
                    }
                    val effectiveAccountId = if (selectedAccountId.isNotBlank()) {
                        selectedAccountId
                    } else if (accounts.isNotEmpty()) {
                        accounts.first().id
                    } else {
                        errorMessage = "Please create an account or wallet first to log this transaction"
                        return@Button
                    }

                    val effectiveToAccountId = if (selectedType == TransactionType.TRANSFER.name) {
                        if (selectedToAccountId.isNotBlank()) selectedToAccountId
                        else accounts.getOrNull(1)?.id ?: ""
                    } else null

                    if (selectedType == TransactionType.TRANSFER.name && effectiveAccountId == effectiveToAccountId) {
                        errorMessage = "Source and destination accounts must be different"
                        return@Button
                    }

                    val finalTitle = titleText.ifBlank {
                        if (selectedType == TransactionType.TRANSFER.name) "Transfer" else selectedCategory
                    }

                    onSave(
                        finalTitle,
                        amount,
                        selectedType,
                        if (selectedType == TransactionType.TRANSFER.name) "Transfer" else selectedCategory,
                        effectiveAccountId,
                        effectiveToAccountId,
                        noteText,
                        tagsText,
                        isRecurring
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_transaction_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (selectedType) {
                        TransactionType.INCOME.name -> IncomeGreen
                        TransactionType.EXPENSE.name -> ExpenseRed
                        else -> TransferBlue
                    }
                )
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (initialTransaction == null) "Add Transaction" else "Save Changes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
