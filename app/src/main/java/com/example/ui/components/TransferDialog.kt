package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.AccountEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferDialog(
    currencySymbol: String,
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (fromId: String, toId: String, amount: Double, note: String) -> Unit
) {
    var fromAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    var toAccountId by remember { mutableStateOf(accounts.getOrNull(1)?.id ?: accounts.firstOrNull()?.id ?: "") }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("Account Transfer") }
    var isFromExpanded by remember { mutableStateOf(false) }
    var isToExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Transfer Between Accounts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (accounts.size < 2) {
                Text(
                    text = "You need at least two accounts or wallets to transfer money. Please create your accounts from the Accounts tab first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                // From Account
                ExposedDropdownMenuBox(
                    expanded = isFromExpanded,
                    onExpandedChange = { isFromExpanded = !isFromExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currentFrom = accounts.find { it.id == fromAccountId }?.name ?: fromAccountId
                    OutlinedTextField(
                        value = currentFrom,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("From Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isFromExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isFromExpanded,
                        onDismissRequest = { isFromExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.name) },
                                onClick = {
                                    fromAccountId = acc.id
                                    isFromExpanded = false
                                }
                            )
                        }
                    }
                }

                // To Account
                ExposedDropdownMenuBox(
                    expanded = isToExpanded,
                    onExpandedChange = { isToExpanded = !isToExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currentTo = accounts.find { it.id == toAccountId }?.name ?: toAccountId
                    OutlinedTextField(
                        value = currentTo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isToExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isToExpanded,
                        onDismissRequest = { isToExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.name) },
                                onClick = {
                                    toAccountId = acc.id
                                    isToExpanded = false
                                }
                            )
                        }
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
                            amountText = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Amount ($currencySymbol)") },
                    prefix = { Text(currencySymbol) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transfer_amount_input")
                )

                // Note
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    },
        confirmButton = {
            if (accounts.size >= 2) {
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (amount == null || amount <= 0) {
                            errorMessage = "Please enter an amount greater than 0"
                            return@Button
                        }
                        if (fromAccountId == toAccountId) {
                            errorMessage = "Source and destination accounts must be different"
                            return@Button
                        }
                        onConfirm(fromAccountId, toAccountId, amount, noteText)
                    },
                    modifier = Modifier.testTag("confirm_transfer_button")
                ) {
                    Text("Confirm Transfer")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
