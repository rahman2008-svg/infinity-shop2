package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.DueOrange
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.IndigoSecondary
import com.example.viewmodel.ShopViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ShopViewModel,
    onNavigate: (String) -> Unit
) {
    val config by viewModel.shopConfig.collectAsState()
    val lang by viewModel.appLanguage.collectAsState()

    val productsList by viewModel.products.collectAsState()
    val customersList by viewModel.customers.collectAsState()
    val expensesList by viewModel.expenses.collectAsState()
    val salesList by viewModel.sales.collectAsState()
    val salesWithItemsList by viewModel.salesWithItems.collectAsState()

    val currency = config?.currency ?: "৳"

    // Calculate Todays Metrics
    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val todaySalesList = salesList.filter { it.date >= todayStart }
    val todaySalesSum = todaySalesList.sumOf { it.totalAmount }

    val todayExpensesSum = expensesList.filter { it.date >= todayStart }.sumOf { it.amount }

    // Profit = Revenue - Cost - Expenses
    val todayProfit = remember(salesWithItemsList, productsList, todayExpensesSum) {
        val todaySalesWithItems = salesWithItemsList.filter { it.sale.date >= todayStart }
        var totalCost = 0.0
        var totalRev = 0.0
        todaySalesWithItems.forEach { saleWithItems ->
            saleWithItems.items.forEach { item ->
                val prod = productsList.find { it.id == item.productId }
                val buyPrice = prod?.buyPrice ?: (item.sellPrice * 0.75) // fallback to 25% margin
                totalCost += buyPrice * item.quantity
                totalRev += item.sellPrice * item.quantity
            }
        }
        val grossProfit = totalRev - totalCost
        (grossProfit - todayExpensesSum).coerceAtLeast(0.0)
    }

    val totalSalesSum = salesList.sumOf { it.totalAmount }
    val totalDuesSum = customersList.sumOf { it.previousDue } + salesList.sumOf { it.dueAmount }

    val lowStockCount = productsList.count { it.stock <= 5 }
    val lowStockList = productsList.filter { it.stock <= 5 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Banner Card (Bento Style Header)
        item {
            val isDark = isSystemInDarkTheme()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bento Brand Logo container
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF4F46E5), Color(0xFF6366F1))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = config?.shopName ?: Loc.t("app_title", lang),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF4F46E5),
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = if (lang == "bn") "ব্যবসা ড্যাশবোর্ড" else "Business Dashboard",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f) else Color(0xFF94A3B8)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Settings/Bell alert button
                    IconButton(
                        onClick = { onNavigate("settings") },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF1F5F9))
                    ) {
                        BadgedBox(
                            badge = {
                                if (lowStockCount > 0) {
                                    Badge(containerColor = ExpenseRed) { Text(lowStockCount.toString(), color = Color.White, fontSize = 9.sp) }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Settings",
                                tint = if (isDark) MaterialTheme.colorScheme.onSurface else Color(0xFF475569),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Avatar component
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E7FF))
                            .clickable { onNavigate("settings") },
                        contentAlignment = Alignment.Center
                    ) {
                        val initial = (config?.ownerName ?: "S").take(1).uppercase()
                        Text(
                            text = initial,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4F46E5)
                        )
                    }
                }
            }
        }

        // 2. Metrics Block (Today's performance - Bento Grid)
        item {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Large primary today stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = Loc.t("today_sales", lang),
                        value = "$currency${String.format("%.1f", todaySalesSum)}",
                        icon = Icons.Default.TrendingUp,
                        backgroundBrush = Brush.linearGradient(
                            colors = listOf(Color(0xFF4F46E5), Color(0xFF6366F1))
                        ),
                        subtitle = if (lang == "bn") "গতকাল থেকে ১২% বৃদ্ধি" else "↑ 12% from yesterday",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = Loc.t("today_profit", lang),
                        value = "$currency${String.format("%.1f", todayProfit)}",
                        icon = Icons.Default.Paid,
                        backgroundBrush = Brush.linearGradient(
                            colors = listOf(Color(0xFF10B981), Color(0xFF059669))
                        ),
                        subtitle = if (lang == "bn") "মোট লাভ মার্জিন: ১৯%" else "Total margin: 19%",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Grid of lifetime totals
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniMetricCard(
                        title = Loc.t("total_sales", lang),
                        value = "$currency${String.format("%.0f", totalSalesSum)}",
                        modifier = Modifier.weight(1f)
                    )
                    MiniMetricCard(
                        title = Loc.t("total_due", lang),
                        value = "$currency${String.format("%.0f", totalDuesSum)}",
                        valueColor = DueOrange,
                        modifier = Modifier.weight(1f)
                    )
                    MiniMetricCard(
                        title = Loc.t("total_products", lang),
                        value = productsList.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Low Stock warning banner (Renders if lowStockCount > 0 - Bento Style Alert)
        if (lowStockCount > 0) {
            item {
                val isDark = isSystemInDarkTheme()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .testTag("low_stock_warning_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFFEF3C7)), // Warm Amber Background
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = DueOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = Loc.t("low_stock_warning", lang).uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DueOrange,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (lang == "bn")
                                        "আপনার $lowStockCount টি পণ্যের স্টক কম!"
                                    else
                                        "$lowStockCount items are critically low!",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) MaterialTheme.colorScheme.onSurface else Color(0xFF1E293B)
                                )
                            }
                        }
                        
                        Button(
                            onClick = { onNavigate("products") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF1F5F9),
                                contentColor = if (isDark) MaterialTheme.colorScheme.onSurface else Color(0xFF334155)
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = if (lang == "bn") "দেখুন" else "View",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 4. Quick Actions
        item {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = Loc.t("quick_actions", lang),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        title = Loc.t("new_sale", lang),
                        icon = Icons.Default.ShoppingCart,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("sales") }
                    )
                    QuickActionButton(
                        title = Loc.t("add_product", lang),
                        icon = Icons.Default.AddBox,
                        color = IndigoSecondary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("products") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        title = Loc.t("new_customer", lang),
                        icon = Icons.Default.PersonAdd,
                        color = ProfitGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("customers") }
                    )
                    QuickActionButton(
                        title = Loc.t("add_expense", lang),
                        icon = Icons.Default.MoneyOff,
                        color = ExpenseRed,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("expenses") }
                    )
                }
            }
        }

        // 5. Today's Transactions title
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Loc.t("today_tx", lang),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${todaySalesList.size} Trx",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Empty State / Transaction items list
        if (todaySalesList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (lang == "bn") "আজ কোনো ট্রানজেকশন হয়নি!" else "No transactions today!",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(todaySalesList) { sale ->
                TransactionRow(
                    sale = sale,
                    currency = currency,
                    onClick = { onNavigate("invoice/${sale.id}") }
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    backgroundBrush: Brush,
    textColor: Color = Color.White,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(135.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor.copy(alpha = 0.85f),
                        letterSpacing = 0.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(textColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = value,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor,
                        lineHeight = 26.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun MiniMetricCard(
    title: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White
        ),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else Color(0xFF94A3B8),
                letterSpacing = 0.5.sp
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = valueColor
            )
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White
        ),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = if (isDark) 0.22f else 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f) else Color(0xFF475569),
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
fun TransactionRow(
    sale: Sale,
    currency: String,
    onClick: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val timeString = formatter.format(Date(sale.date))
    val isDark = isSystemInDarkTheme()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sale.invoiceNumber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${sale.customerName ?: "Guest"} • $timeString",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$currency${String.format("%.1f", sale.totalAmount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Payment Badge / Due Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (sale.dueAmount > 0) DueOrange.copy(alpha = 0.15f)
                            else ProfitGreen.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (sale.dueAmount > 0) "DUE" else sale.paymentMethod.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (sale.dueAmount > 0) DueOrange else ProfitGreen
                    )
                }
            }
        }
    }
}
