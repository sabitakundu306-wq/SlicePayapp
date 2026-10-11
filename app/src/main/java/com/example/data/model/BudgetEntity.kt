package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val monthlyLimit: Double,
    val monthYear: String = "ALL", // Format "YYYY-MM" or "ALL" for persistent monthly target
    val alertThreshold: Double = 0.90 // 90%
)
