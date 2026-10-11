package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryInfo(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val isExpense: Boolean = true,
    val isCustom: Boolean = false,
    val id: Long = 0
)

data class IconChoice(
    val key: String,
    val label: String,
    val icon: ImageVector
)

object CategoryRegistry {

    val availableIcons = listOf(
        IconChoice("grocery", "Groceries", Icons.Default.LocalGroceryStore),
        IconChoice("dining", "Dining", Icons.Default.LocalDining),
        IconChoice("coffee", "Coffee", Icons.Default.LocalCafe),
        IconChoice("shopping", "Shopping", Icons.Default.ShoppingBag),
        IconChoice("transport", "Transport", Icons.Default.DirectionsCar),
        IconChoice("bills", "Bills", Icons.Default.Receipt),
        IconChoice("home", "Home", Icons.Default.Home),
        IconChoice("health", "Health", Icons.Default.FitnessCenter),
        IconChoice("travel", "Travel", Icons.Default.Flight),
        IconChoice("education", "Education", Icons.Default.School),
        IconChoice("entertainment", "Entertainment", Icons.Default.CardGiftcard),
        IconChoice("subscriptions", "Subscriptions", Icons.Default.Subscriptions),
        IconChoice("pets", "Pets", Icons.Default.Pets),
        IconChoice("tech", "Tech", Icons.Default.Computer),
        IconChoice("gaming", "Gaming", Icons.Default.SportsEsports),
        IconChoice("music", "Music", Icons.Default.MusicNote),
        IconChoice("book", "Books", Icons.Default.MenuBook),
        IconChoice("baby", "Kids/Family", Icons.Default.ChildCare),
        IconChoice("savings", "Savings", Icons.Default.Savings),
        IconChoice("work", "Salary/Work", Icons.Default.Work),
        IconChoice("freelance", "Freelance", Icons.Default.AttachMoney),
        IconChoice("investments", "Investments", Icons.Default.TrendingUp),
        IconChoice("other", "Other", Icons.Default.MoreHoriz)
    )

    val availableColors = listOf(
        "#10B981", // Emerald
        "#059669", // Dark Emerald
        "#0D9488", // Teal
        "#06B6D4", // Cyan
        "#3B82F6", // Blue
        "#6366F1", // Indigo
        "#8B5CF6", // Purple
        "#EC4899", // Pink
        "#F43F5E", // Rose
        "#EF4444", // Red
        "#F97316", // Orange
        "#F59E0B", // Amber
        "#84CC16", // Lime
        "#64748B"  // Slate
    )

    fun getIconByKey(key: String): ImageVector {
        return availableIcons.find { it.key.equals(key, ignoreCase = true) }?.icon ?: Icons.Default.MoreHoriz
    }

    fun parseColor(hex: String): Color {
        return try {
            val clean = hex.removePrefix("#")
            val colorInt = if (clean.length == 6) {
                ("FF$clean").toLong(16)
            } else {
                clean.toLong(16)
            }
            Color(colorInt)
        } catch (e: Exception) {
            Color(0xFF64748B)
        }
    }

    val defaultExpenseCategories = listOf(
        CategoryEntity(name = "Groceries", iconKey = "grocery", colorHex = "#10B981", isExpense = true, isCustom = false),
        CategoryEntity(name = "Dining Out", iconKey = "dining", colorHex = "#F59E0B", isExpense = true, isCustom = false),
        CategoryEntity(name = "Shopping", iconKey = "shopping", colorHex = "#EC4899", isExpense = true, isCustom = false),
        CategoryEntity(name = "Transport", iconKey = "transport", colorHex = "#3B82F6", isExpense = true, isCustom = false),
        CategoryEntity(name = "Bills & Utilities", iconKey = "bills", colorHex = "#06B6D4", isExpense = true, isCustom = false),
        CategoryEntity(name = "Health & Medical", iconKey = "health", colorHex = "#EF4444", isExpense = true, isCustom = false),
        CategoryEntity(name = "Travel", iconKey = "travel", colorHex = "#0D9488", isExpense = true, isCustom = false),
        CategoryEntity(name = "Education", iconKey = "education", colorHex = "#F97316", isExpense = true, isCustom = false),
        CategoryEntity(name = "Entertainment", iconKey = "entertainment", colorHex = "#8B5CF6", isExpense = true, isCustom = false),
        CategoryEntity(name = "Other Expense", iconKey = "other", colorHex = "#64748B", isExpense = true, isCustom = false)
    )

    val defaultIncomeCategories = listOf(
        CategoryEntity(name = "Salary", iconKey = "work", colorHex = "#10B981", isExpense = false, isCustom = false),
        CategoryEntity(name = "Freelance", iconKey = "freelance", colorHex = "#059669", isExpense = false, isCustom = false),
        CategoryEntity(name = "Investments", iconKey = "investments", colorHex = "#6366F1", isExpense = false, isCustom = false),
        CategoryEntity(name = "Refunds/Gifts", iconKey = "entertainment", colorHex = "#3B82F6", isExpense = false, isCustom = false),
        CategoryEntity(name = "Other Income", iconKey = "savings", colorHex = "#0D9488", isExpense = false, isCustom = false)
    )

    val expenseCategories: List<CategoryInfo>
        get() = defaultExpenseCategories.map { toCategoryInfo(it) }

    val incomeCategories: List<CategoryInfo>
        get() = defaultIncomeCategories.map { toCategoryInfo(it) }

    val allCategories: List<CategoryInfo>
        get() = (defaultExpenseCategories + defaultIncomeCategories).map { toCategoryInfo(it) }

    fun toCategoryInfo(entity: CategoryEntity): CategoryInfo {
        return CategoryInfo(
            name = entity.name,
            icon = getIconByKey(entity.iconKey),
            color = parseColor(entity.colorHex),
            isExpense = entity.isExpense,
            isCustom = entity.isCustom,
            id = entity.id
        )
    }

    // Static fallback lookup if categories haven't loaded yet
    fun getCategory(name: String, customList: List<CategoryEntity> = emptyList()): CategoryInfo {
        val found = customList.find { it.name.equals(name, ignoreCase = true) }
        if (found != null) {
            return toCategoryInfo(found)
        }
        val defaultFound = (defaultExpenseCategories + defaultIncomeCategories).find { it.name.equals(name, ignoreCase = true) }
        if (defaultFound != null) {
            return toCategoryInfo(defaultFound)
        }
        return CategoryInfo(name, Icons.Default.MoreHoriz, Color(0xFF64748B))
    }
}
