package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // EXPENSE, INCOME, TRANSFER
    val category: String,
    val accountId: String, // checking, savings, cash, credit_card
    val toAccountId: String? = null, // for TRANSFER
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val tags: String = "",
    val isRecurring: Boolean = false,
    val recurringFrequency: String? = null // DAILY, WEEKLY, MONTHLY, YEARLY
)
