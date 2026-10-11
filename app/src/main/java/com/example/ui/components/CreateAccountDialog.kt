package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.UUID

@Composable
fun CreateAccountDialog(
    currencySymbol: String = "₹",
    onDismiss: () -> Unit,
    onConfirm: (id: String, name: String, type: String, initialBalance: Double, colorHex: String, iconName: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("BANK") }
    var balanceText by remember { mutableStateOf("0") }
    var selectedColorHex by remember { mutableStateOf("#10B981") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val types = listOf(
        Triple("BANK", "Bank", Icons.Default.AccountBalance),
        Triple("SAVINGS", "Savings", Icons.Default.Savings),
        Triple("CASH", "Cash", Icons.Default.AccountBalanceWallet),
        Triple("CREDIT", "Credit Card", Icons.Default.CreditCard)
    )

    val colors = listOf(
        "#10B981", "#3B82F6", "#6366F1", "#8B5CF6",
        "#EC4899", "#F59E0B", "#06B6D4", "#F43F5E"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Account / Wallet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Account Name") },
                    placeholder = { Text("e.g. HDFC Bank, Cash Wallet, Paytm") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_account_name_input")
                )

                // Type Chips
                Text(
                    text = "Account Type",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    types.forEach { (typeKey, label, icon) ->
                        val isSelected = selectedType == typeKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedType = typeKey },
                            label = { Text(label) },
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Initial Balance
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^-?\\d*\\.?\\d{0,2}\$"))) {
                            balanceText = input
                            errorMessage = null
                        }
                    },
                    label = { Text("Initial Balance ($currencySymbol)") },
                    prefix = { Text("$currencySymbol ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_account_balance_input")
                )

                // Color Selection
                Text(
                    text = "Badge Color",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { hex ->
                        val color = try {
                            val clean = hex.removePrefix("#")
                            Color(("FF$clean").toLong(16))
                        } catch (e: Exception) { Color(0xFF10B981) }
                        val isSelected = selectedColorHex == hex
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColorHex = hex },
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
                    if (name.isBlank()) {
                        errorMessage = "Please enter an account name"
                        return@Button
                    }
                    val initialBal = balanceText.toDoubleOrNull() ?: 0.0
                    val accountId = "acc_" + UUID.randomUUID().toString().take(8)
                    val iconName = when (selectedType) {
                        "SAVINGS" -> "savings"
                        "CASH" -> "wallet"
                        "CREDIT" -> "credit_card"
                        else -> "account_balance"
                    }
                    onConfirm(accountId, name.trim(), selectedType, initialBal, selectedColorHex, iconName)
                },
                modifier = Modifier.testTag("confirm_create_account_button")
            ) {
                Text("Create Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
