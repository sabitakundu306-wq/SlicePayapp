package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TransferBlue
import com.example.ui.viewmodel.CategoryYearComparison
import com.example.ui.viewmodel.MonthlyComparison
import com.example.ui.viewmodel.YearOverYearState
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

/**
 * Summary card designed for the Dashboard to highlight Year-over-Year comparison metrics.
 */
@Composable
fun YearComparisonSummaryCard(
    yoyState: YearOverYearState,
    currencySymbol: String,
    onViewDetailedComparison: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("yoy_summary_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Year-over-Year Comparison",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${yoyState.selectedYear} vs ${yoyState.previousYear}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "YoY Analytics",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 KPI Cards: Expenses, Income, Net
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Expenses YoY
                val expenseDown = yoyState.expenseChangePct <= 0
                val expenseBadgeColor = if (expenseDown) Color(0xFF047857) else Color(0xFFBE123C)
                YoYMetricTile(
                    title = "Expenses",
                    currentAmount = String.format(Locale.US, "%s%,.0f", currencySymbol, yoyState.currentYearExpense),
                    previousAmount = String.format(Locale.US, "vs %s%,.0f", currencySymbol, yoyState.previousYearExpense),
                    pctChange = yoyState.expenseChangePct,
                    isPositiveSentiment = expenseDown,
                    badgeColor = expenseBadgeColor,
                    modifier = Modifier.weight(1f)
                )

                // Income YoY
                val incomeUp = yoyState.incomeChangePct >= 0
                val incomeBadgeColor = if (incomeUp) Color(0xFF047857) else Color(0xFFBE123C)
                YoYMetricTile(
                    title = "Income",
                    currentAmount = String.format(Locale.US, "%s%,.0f", currencySymbol, yoyState.currentYearIncome),
                    previousAmount = String.format(Locale.US, "vs %s%,.0f", currencySymbol, yoyState.previousYearIncome),
                    pctChange = yoyState.incomeChangePct,
                    isPositiveSentiment = incomeUp,
                    badgeColor = incomeBadgeColor,
                    modifier = Modifier.weight(1f)
                )

                // Net YoY
                val netUp = yoyState.netChangePct >= 0
                val netBadgeColor = if (netUp) Color(0xFF047857) else Color(0xFFBE123C)
                YoYMetricTile(
                    title = "Net Savings",
                    currentAmount = String.format(Locale.US, "%s%,.0f", currencySymbol, yoyState.currentYearNet),
                    previousAmount = String.format(Locale.US, "vs %s%,.0f", currencySymbol, yoyState.previousYearNet),
                    pctChange = yoyState.netChangePct,
                    isPositiveSentiment = netUp,
                    badgeColor = netBadgeColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mini 12-Month Sparkline Preview
            MiniYoYSparkline(
                monthlyComparisons = yoyState.monthlyComparisons,
                selectedYear = yoyState.selectedYear,
                previousYear = yoyState.previousYear
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Insight banner text
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💡 ${yoyState.insightText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CTA Button
            Button(
                onClick = onViewDetailedComparison,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_yoy_detailed_btn")
            ) {
                Text("Explore Full YoY Comparison", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun YoYMetricTile(
    title: String,
    currentAmount: String,
    previousAmount: String,
    pctChange: Double,
    isPositiveSentiment: Boolean,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = currentAmount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = previousAmount,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Percentage badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = badgeColor.copy(alpha = 0.14f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isPositiveSentiment) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = String.format(Locale.US, "%+.1f%%", pctChange),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }
        }
    }
}

/**
 * Mini 12-month visual preview comparing expenses between two years.
 */
@Composable
private fun MiniYoYSparkline(
    monthlyComparisons: List<MonthlyComparison>,
    selectedYear: Int,
    previousYear: Int,
    modifier: Modifier = Modifier
) {
    val maxVal = max(
        monthlyComparisons.maxOfOrNull { max(it.currentYearExpense, it.previousYearExpense) } ?: 1.0,
        1.0
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "12-Month Outflow Momentum",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(EmeraldPrimary, CircleShape))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("$selectedYear", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF6366F1), CircleShape))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("$previousYear", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            monthlyComparisons.forEach { item ->
                val curHeightFrac = (item.currentYearExpense / maxVal).toFloat().coerceIn(0.05f, 1f)
                val prevHeightFrac = (item.previousYearExpense / maxVal).toFloat().coerceIn(0.05f, 1f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.height(34.dp)
                    ) {
                        // Current Year bar
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight(curHeightFrac)
                                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                .background(EmeraldPrimary)
                        )
                        // Previous Year bar
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight(prevHeightFrac)
                                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                .background(Color(0xFF6366F1).copy(alpha = 0.65f))
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.monthShortName.first().toString(),
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Interactive Full 12-Month Grouped Bar Chart with toggle for Expenses vs Income.
 */
@Composable
fun YearOverYearGroupedBarChart(
    monthlyComparisons: List<MonthlyComparison>,
    selectedYear: Int,
    previousYear: Int,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    var isExpenseMode by remember { mutableStateOf(true) }
    var selectedMonthIndex by remember { mutableStateOf<Int?>(null) }

    val currentYearColor = if (isExpenseMode) EmeraldPrimary else Color(0xFF047857)
    val previousYearColor = Color(0xFF6366F1)

    val maxVal = max(
        monthlyComparisons.maxOfOrNull {
            if (isExpenseMode) max(it.currentYearExpense, it.previousYearExpense)
            else max(it.currentYearIncome, it.previousYearIncome)
        } ?: 1.0,
        1.0
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("yoy_grouped_bar_chart")
    ) {
        // Toggle Buttons (Expenses vs Income)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(modifier = Modifier.padding(3.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isExpenseMode) MaterialTheme.colorScheme.surface else Color.Transparent,
                        modifier = Modifier.clickable { isExpenseMode = true }
                    ) {
                        Text(
                            text = "Expenses",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isExpenseMode) FontWeight.Bold else FontWeight.Normal,
                            color = if (isExpenseMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (!isExpenseMode) MaterialTheme.colorScheme.surface else Color.Transparent,
                        modifier = Modifier.clickable { isExpenseMode = false }
                    ) {
                        Text(
                            text = "Income",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (!isExpenseMode) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isExpenseMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Legend
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(currentYearColor, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$selectedYear", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(previousYearColor, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$previousYear", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Month Tooltip Detail
        val activeItem = selectedMonthIndex?.let { monthlyComparisons.getOrNull(it) }
        if (activeItem != null) {
            val curVal = if (isExpenseMode) activeItem.currentYearExpense else activeItem.currentYearIncome
            val prevVal = if (isExpenseMode) activeItem.previousYearExpense else activeItem.previousYearIncome
            val diff = curVal - prevVal
            val pct = if (prevVal > 0) ((curVal - prevVal) / prevVal) * 100 else 0.0

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${activeItem.monthShortName} Breakdown",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$selectedYear: ${String.format(Locale.US, "%s%,.2f", currencySymbol, curVal)}  |  $previousYear: ${String.format(Locale.US, "%s%,.2f", currencySymbol, prevVal)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isExpenseMode) {
                            if (diff <= 0) Color(0xFF047857).copy(alpha = 0.15f) else Color(0xFFBE123C).copy(alpha = 0.15f)
                        } else {
                            if (diff >= 0) Color(0xFF047857).copy(alpha = 0.15f) else Color(0xFFBE123C).copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            text = String.format(Locale.US, "%s%,.0f (%+.1f%%)", if (diff >= 0) "+" else "", diff, pct),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isExpenseMode) {
                                if (diff <= 0) Color(0xFF047857) else Color(0xFFBE123C)
                            } else {
                                if (diff >= 0) Color(0xFF047857) else Color(0xFFBE123C)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // 12-Month Grouped Bar Chart Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            monthlyComparisons.forEachIndexed { index, item ->
                val isSelected = selectedMonthIndex == index
                val curVal = if (isExpenseMode) item.currentYearExpense else item.currentYearIncome
                val prevVal = if (isExpenseMode) item.previousYearExpense else item.previousYearIncome

                val curFrac = (curVal / maxVal).toFloat().coerceIn(0.04f, 1f)
                val prevFrac = (prevVal / maxVal).toFloat().coerceIn(0.04f, 1f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            selectedMonthIndex = if (selectedMonthIndex == index) null else index
                        }
                ) {
                    Row(
                        modifier = Modifier.height(150.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Current Year Bar
                        Box(
                            modifier = Modifier
                                .width(9.dp)
                                .fillMaxHeight(curFrac)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    if (isSelected) currentYearColor else currentYearColor.copy(alpha = 0.9f)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )

                        // Previous Year Bar
                        Box(
                            modifier = Modifier
                                .width(9.dp)
                                .fillMaxHeight(prevFrac)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    if (isSelected) previousYearColor else previousYearColor.copy(alpha = 0.65f)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.monthShortName,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Full detail Modal Bottom Sheet for Year-over-Year comparison.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearComparisonDetailSheet(
    yoyState: YearOverYearState,
    currencySymbol: String,
    onYearChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var isYearPickerOpen by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("yoy_detail_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with Close and Year Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Yearly Comparison",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Annual financial growth & trends",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Year Selector Chip
                    Box {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.clickable { isYearPickerOpen = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${yoyState.selectedYear} vs ${yoyState.previousYear}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select Year",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isYearPickerOpen,
                            onDismissRequest = { isYearPickerOpen = false }
                        ) {
                            yoyState.availableYears.forEach { yr ->
                                DropdownMenuItem(
                                    text = { Text("$yr vs ${yr - 1}") },
                                    onClick = {
                                        onYearChange(yr)
                                        isYearPickerOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main KPI Summary Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Annual Overview (${yoyState.selectedYear} vs ${yoyState.previousYear})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Row 1: Total Expenses
                    YoYSummaryRow(
                        label = "Total Expenses",
                        currentAmount = yoyState.currentYearExpense,
                        previousAmount = yoyState.previousYearExpense,
                        pctChange = yoyState.expenseChangePct,
                        currencySymbol = currencySymbol,
                        isExpense = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: Total Income
                    YoYSummaryRow(
                        label = "Total Income",
                        currentAmount = yoyState.currentYearIncome,
                        previousAmount = yoyState.previousYearIncome,
                        pctChange = yoyState.incomeChangePct,
                        currencySymbol = currencySymbol,
                        isExpense = false
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 3: Net Savings
                    YoYSummaryRow(
                        label = "Net Balance",
                        currentAmount = yoyState.currentYearNet,
                        previousAmount = yoyState.previousYearNet,
                        pctChange = yoyState.netChangePct,
                        currencySymbol = currencySymbol,
                        isExpense = false
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Savings Rate progress comparison
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Savings Rate",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${yoyState.selectedYear}: ${yoyState.currentSavingsRate}%  |  ${yoyState.previousYear}: ${yoyState.previousSavingsRate}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { (yoyState.currentSavingsRate.coerceIn(0, 100) / 100f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EmeraldPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 12-Month Grouped Bar Chart
            Text(
                text = "12-Month Month-by-Month Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap on any month column to inspect the exact variance",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            YearOverYearGroupedBarChart(
                monthlyComparisons = yoyState.monthlyComparisons,
                selectedYear = yoyState.selectedYear,
                previousYear = yoyState.previousYear,
                currencySymbol = currencySymbol
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Category Breakdown Comparison
            Text(
                text = "Spending by Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Where spending grew or dropped between ${yoyState.selectedYear} and ${yoyState.previousYear}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (yoyState.categoryComparisons.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No category expense records found for ${yoyState.selectedYear} or ${yoyState.previousYear}.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    yoyState.categoryComparisons.forEach { catComp ->
                        CategoryYoYCard(
                            item = catComp,
                            selectedYear = yoyState.selectedYear,
                            previousYear = yoyState.previousYear,
                            currencySymbol = currencySymbol
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 12-Month Table / List
            Text(
                text = "Detailed Monthly Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            ) {
                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Month", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("${yoyState.selectedYear} Exp", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                    Text("${yoyState.previousYear} Exp", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                    Text("Variance", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                }

                yoyState.monthlyComparisons.forEachIndexed { idx, m ->
                    val diff = m.expenseDiff
                    val isLower = diff <= 0
                    val rowBg = if (idx % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowBg)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(m.monthShortName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Text(
                            String.format(Locale.US, "%s%,.0f", currencySymbol, m.currentYearExpense),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1.3f),
                            textAlign = TextAlign.End
                        )
                        Text(
                            String.format(Locale.US, "%s%,.0f", currencySymbol, m.previousYearExpense),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1.3f),
                            textAlign = TextAlign.End
                        )
                        Text(
                            text = String.format(Locale.US, "%s%,.0f", if (diff > 0) "+" else "", diff),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isLower) Color(0xFF047857) else Color(0xFFBE123C),
                            modifier = Modifier.weight(1.2f),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun YoYSummaryRow(
    label: String,
    currentAmount: Double,
    previousAmount: Double,
    pctChange: Double,
    currencySymbol: String,
    isExpense: Boolean
) {
    val isGood = if (isExpense) pctChange <= 0 else pctChange >= 0
    val badgeColor = if (isGood) Color(0xFF047857) else Color(0xFFBE123C)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = String.format(Locale.US, "%s%,.2f", currencySymbol, currentAmount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.US, "Prior: %s%,.2f", currencySymbol, previousAmount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeColor.copy(alpha = 0.14f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (pctChange >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = String.format(Locale.US, "%+.1f%%", pctChange),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryYoYCard(
    item: CategoryYearComparison,
    selectedYear: Int,
    previousYear: Int,
    currencySymbol: String
) {
    val maxVal = max(item.currentYearAmount, item.previousYearAmount).coerceAtLeast(1.0)
    val curFrac = (item.currentYearAmount / maxVal).toFloat().coerceIn(0f, 1f)
    val prevFrac = (item.previousYearAmount / maxVal).toFloat().coerceIn(0f, 1f)

    val isExpenseDown = item.diff <= 0
    val badgeColor = if (isExpenseDown) Color(0xFF047857) else Color(0xFFBE123C)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(item.categoryInfo.color.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.categoryInfo.icon,
                            contentDescription = item.category,
                            tint = item.categoryInfo.color,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.14f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (item.diff >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = String.format(Locale.US, "%s%,.0f (%+.1f%%)", if (item.diff >= 0) "+" else "", item.diff, item.percentChange),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dual Progress Comparison Bars
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Current Year Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$selectedYear", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        String.format(Locale.US, "%s%,.2f", currencySymbol, item.currentYearAmount),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                LinearProgressIndicator(
                    progress = { curFrac },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = item.categoryInfo.color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                // Previous Year Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$previousYear", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        String.format(Locale.US, "%s%,.2f", currencySymbol, item.previousYearAmount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                LinearProgressIndicator(
                    progress = { prevFrac },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF6366F1).copy(alpha = 0.7f),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}
