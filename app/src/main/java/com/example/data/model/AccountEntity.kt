package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: String, // e.g. "checking", "savings", "cash", "credit_card"
    val name: String,
    val type: String, // "CHECKING", "SAVINGS", "CASH", "CREDIT"
    val initialBalance: Double,
    val colorHex: String,
    val iconName: String
)
