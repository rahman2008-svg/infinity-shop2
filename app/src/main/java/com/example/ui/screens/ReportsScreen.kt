package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Loc
import com.example.ui.theme.DueOrange
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen
import com.example.viewmodel.ShopViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: ShopViewModel) {
    val lang by viewModel.appLanguage.collectAsState()
    val config by viewModel.shopConfig.collectAsState()
    val currency = config?.currency ?: "৳"

    val salesList by viewModel.sales.collectAsState()
    val expensesList by viewModel.expenses.collectAsState()
    val productsList by viewModel.products.collectAsState()
    val salesWithItemsList by viewModel.salesWithItems.collectAsState()

    // 1. Calculations by Date Range
    val now = Calendar.getInstance()

    // Today
    val startOfToday = now.run {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        timeInMillis
    }
    val todaySales = salesList.filter { it.date >= startOfToday }.sumOf { it.totalAmount }
    val todayExpenses = expensesList.filter { it.date >= startOfToday }.sumOf { it.amount }

    // This Week
    val startOfWeek = now.run {
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        timeInMillis
    }
    val weeklySales = salesList.filter { it.date >= startOfWeek }.sumOf { it.totalAmount }
    val weeklyExpenses = expensesList.filter { it.date >= startOfWeek }.sumOf { it.amount }

    // This Month
    val startOfMonth = now.run {
        set(Calendar.DAY_OF_MONTH, 1)
        timeInMillis
    }
    val monthlySales = salesList.filter { it.date >= startOfMonth }.sumOf { it.totalAmount }
    val monthlyExpenses = expensesList.filter { it.date >= startOfMonth }.sumOf { it.amount }

    // Lifetimes totals
    val lifetimeSales = salesList.sumOf { it.totalAmount }
    val lifetimeExpenses = expensesList.sumOf { it.amount }

    // Calculate profit margins
    val totalCost = remember(salesWithItemsList, productsList) {
        var cost = 0.0
        salesWithItemsList.forEach { saleWithItems ->
            saleWithItems.items.forEach { item ->
                val prod = productsList.find { it.id == item.productId }
                val buyPrice = prod?.buyPrice ?: (item.sellPrice * 0.75)
                cost += buyPrice * item.quantity
            }
        }
        cost
    }
    val grossProfit = lifetimeSales - totalCost
    val netProfit = (grossProfit - lifetimeExpenses).coerceAtLeast(0.0)

    // Calculate Best Selling Products (Quantity sold total)
    val bestSellers = remember(salesWithItemsList) {
        val qtyMap = mutableMapOf<String, Int>()
        salesWithItemsList.forEach { saleWithItems ->
            saleWithItems.items.forEach { item ->
                qtyMap[item.productName] = (qtyMap[item.productName] ?: 0) + item.quantity
            }
        }
        qtyMap.entries.sortedByDescending { it.value }.take(5)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Profit card header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Loc.t("profit", lang) + " (Net)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = ProfitGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$currency${String.format("%.1f", netProfit)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = ProfitGreen
                    )
                    Text(
                        text = if (lang == "bn") "মোট বিক্রি এবং সমস্ত খরচ বাদ দিয়ে আসল লাভ।" else "Gross sales minus total item costs and shop expenses.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // 2. Custom Canvas Visual Charts
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (lang == "bn") "বিক্রি বনাম খরচ তুলনা" else "Sales vs Expenses Chart",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Draw dynamic bar chart
                    val maxVal = maxOf(lifetimeSales, lifetimeExpenses, 100.0).toFloat()
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val secondaryColor = ExpenseRed

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // Draw baseline
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            start = Offset(0f, canvasHeight),
                            end = Offset(canvasWidth, canvasHeight),
                            strokeWidth = 2f
                        )

                        // Bar measurements
                        val barWidth = 60.dp.toPx()
                        val spacing = 40.dp.toPx()

                        // Sales Bar
                        val salesHeight = (lifetimeSales.toFloat() / maxVal) * (canvasHeight - 40f)
                        drawRect(
                            color = primaryColor,
                            topLeft = Offset(spacing, canvasHeight - salesHeight),
                            size = Size(barWidth, salesHeight)
                        )

                        // Expense Bar
                        val expenseHeight = (lifetimeExpenses.toFloat() / maxVal) * (canvasHeight - 40f)
                        drawRect(
                            color = secondaryColor,
                            topLeft = Offset(spacing * 2 + barWidth, canvasHeight - expenseHeight),
                            size = Size(barWidth, expenseHeight)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Chart Legend Labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.primary))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "${Loc.t("total_sales", lang)}: $currency${String.format("%.0f", lifetimeSales)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(ExpenseRed))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "${Loc.t("expense", lang)}: $currency${String.format("%.0f", lifetimeExpenses)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Sales Periods Grid Info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (lang == "bn") "পর্যায়ক্রমিক প্রতিবেদন" else "Periodic Reports Summary",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PeriodReportBox(
                            title = Loc.t("daily", lang),
                            salesVal = "$currency${String.format("%.0f", todaySales)}",
                            expenseVal = "$currency${String.format("%.0f", todayExpenses)}",
                            modifier = Modifier.weight(1f)
                        )
                        PeriodReportBox(
                            title = Loc.t("weekly", lang),
                            salesVal = "$currency${String.format("%.0f", weeklySales)}",
                            expenseVal = "$currency${String.format("%.0f", weeklyExpenses)}",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PeriodReportBox(
                            title = Loc.t("monthly", lang),
                            salesVal = "$currency${String.format("%.0f", monthlySales)}",
                            expenseVal = "$currency${String.format("%.0f", monthlyExpenses)}",
                            modifier = Modifier.weight(1f)
                        )
                        PeriodReportBox(
                            title = if (lang == "bn") "বার্ষিক বিক্রি" else "Yearly Sales",
                            salesVal = "$currency${String.format("%.0f", lifetimeSales)}",
                            expenseVal = "$currency${String.format("%.0f", lifetimeExpenses)}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 4. Best Sellers
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Loc.t("best_seller", lang),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (bestSellers.isEmpty()) {
                        Text(
                            text = if (lang == "bn") "কোনো পণ্য এখনো বিক্রি হয়নি!" else "No products sold yet!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        bestSellers.forEachIndexed { index, entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (index + 1).toString(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = entry.key,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "${entry.value} pcs",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodReportBox(
    title: String,
    salesVal: String,
    expenseVal: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Sale:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                Text(salesVal, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ProfitGreen)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Cost:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                Text(expenseVal, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ExpenseRed)
            }
        }
    }
}
