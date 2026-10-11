package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.AccountEntity
import java.util.Locale
import java.util.UUID

data class AccountTypeOption(
    val code: String,
    val label: String,
    val icon: ImageVector,
    val iconName: String
)

data class AccountColorOption(
    val hex: String,
    val color: Color
)

@Composable
fun AddEditAccountDialog(
    currencySymbol: String,
    existingAccount: AccountEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        id: String,
        name: String,
        type: String,
        initialBalance: Double,
        colorHex: String,
        iconName: String
    ) -> Unit
) {
    val typeOptions = listOf(
        AccountTypeOption("BANK", "Bank", Icons.Default.AccountBalance, "bank"),
        AccountTypeOption("CASH", "Cash / Wallet", Icons.Default.AccountBalanceWallet, "wallet"),
        AccountTypeOption("CARD", "Credit Card", Icons.Default.CreditCard, "credit_card"),
        AccountTypeOption("SAVINGS", "Savings", Icons.Default.Savings, "savings"),
        AccountTypeOption("INVESTMENT", "Investment", Icons.AutoMirrored.Filled.TrendingUp, "investment"),
        AccountTypeOption("OTHER", "Other", Icons.Default.Payments, "payments")
    )

    val colorOptions = listOf(
        AccountColorOption("#059669", Color(0xFF059669)), // Emerald
        AccountColorOption("#4F46E5", Color(0xFF4F46E5)), // Indigo
        AccountColorOption("#0284C7", Color(0xFF0284C7)), // Sky
        AccountColorOption("#D97706", Color(0xFFD97706)), // Amber
        AccountColorOption("#E11D48", Color(0xFFE11D48)), // Rose
        AccountColorOption("#7C3AED", Color(0xFF7C3AED)), // Violet
        AccountColorOption("#0D9488", Color(0xFF0D9488)), // Teal
        AccountColorOption("#334155", Color(0xFF334155))  // Slate
    )

    var nameText by remember { mutableStateOf(existingAccount?.name ?: "") }
    var selectedType by remember {
        mutableStateOf(existingAccount?.type ?: "BANK")
    }
    var balanceText by remember {
        mutableStateOf(
            if (existingAccount != null) {
                String.format(Locale.US, "%.2f", existingAccount.initialBalance)
            } else "0"
        )
    }
    var selectedColorHex by remember {
        mutableStateOf(existingAccount?.colorHex ?: "#059669")
    }
    var selectedIconName by remember {
        mutableStateOf(
            existingAccount?.iconName ?: (typeOptions.find { it.code == selectedType }?.iconName ?: "bank")
        )
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingAccount == null) "Create Account / Wallet" else "Edit Account / Wallet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Name Field
                OutlinedTextField(
                    value = nameText,
                    onValueChange = {
                        nameText = it
                        errorMessage = null
                    },
                    label = { Text("Account / Wallet Name") },
                    placeholder = { Text("e.g. HDFC Bank, Cash Wallet, Paytm") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_name_input")
                )

                // Type Selection
                Column {
                    Text(
                        text = "Account Type",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        typeOptions.forEach { opt ->
                            val isSelected = selectedType == opt.code
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedType = opt.code
                                    selectedIconName = opt.iconName
                                },
                                label = { Text(opt.label) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = opt.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.testTag("account_type_${opt.code}")
                            )
                        }
                    }
                }

                // Initial Balance Field
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^-?\\d*\\.?\\d{0,2}\$"))) {
                            balanceText = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Initial Balance ($currencySymbol)") },
                    prefix = { Text(currencySymbol) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_balance_input")
                )

                // Color Selection
                Column {
                    Text(
                        text = "Theme Color",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        colorOptions.forEach { opt ->
                            val isSelected = selectedColorHex.equals(opt.hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(opt.color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorHex = opt.hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Icon Selection
                Column {
                    Text(
                        text = "Icon",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        typeOptions.forEach { opt ->
                            val isSelected = selectedIconName == opt.iconName
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedIconName = opt.iconName },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = opt.icon,
                                    contentDescription = opt.label,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val name = nameText.trim()
                    if (name.isBlank()) {
                        errorMessage = "Please enter an account name"
                        return@Button
                    }
                    val balance = balanceText.toDoubleOrNull() ?: 0.0
                    val id = existingAccount?.id ?: UUID.randomUUID().toString()

                    onSave(id, name, selectedType, balance, selectedColorHex, selectedIconName)
                },
                modifier = Modifier.testTag("save_account_button")
            ) {
                Text(if (existingAccount == null) "Create" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
