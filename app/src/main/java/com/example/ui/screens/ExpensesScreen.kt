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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Expense
import com.example.data.Loc
import com.example.ui.theme.ExpenseRed
import com.example.viewmodel.ShopViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(viewModel: ShopViewModel) {
    val lang by viewModel.appLanguage.collectAsState()
    val config by viewModel.shopConfig.collectAsState()
    val currency = config?.currency ?: "৳"

    val expensesList by viewModel.expenses.collectAsState()
    val totalExpenseSum = expensesList.sumOf { it.amount }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedExpenseForDelete by remember { mutableStateOf<Expense?>(null) }

    // Form inputs
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val expensePresets = listOf(
        "rent", "electricity", "salary", "transport", "other"
    )
    var selectedPresetIndex by remember { mutableStateOf(0) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    title = ""
                    amount = ""
                    note = ""
                    selectedPresetIndex = 0
                    showAddDialog = true
                },
                containerColor = ExpenseRed,
                modifier = Modifier.testTag("add_expense_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Expense",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Total Expense Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (lang == "bn") "মোট খরচের পরিমাণ" else "Total Expenses",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ExpenseRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currency${String.format("%.1f", totalExpenseSum)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = ExpenseRed
                    )
                }
            }

            // List Title
            Text(
                text = Loc.t("expense_list", lang),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            // Expense List
            if (expensesList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MoneyOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (lang == "bn") "কোনো খরচের হিসাব নেই!" else "No expenses recorded!",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(expensesList) { exp ->
                        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(exp.date))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedExpenseForDelete = exp }
                                .testTag("expense_item_${exp.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF334155) else Color(0xFFF1F5F9))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(ExpenseRed.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoneyOff,
                                        contentDescription = null,
                                        tint = ExpenseRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = exp.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = dateStr + if (exp.note.isNullOrBlank()) "" else " • ${exp.note}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                Text(
                                    text = "$currency${String.format("%.1f", exp.amount)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ExpenseRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Expense Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = Loc.t("add_expense", lang),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Presets
                    Text(
                        text = if (lang == "bn") "ক্যাটাগরি নির্বাচন করুন" else "Select Category:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        expensePresets.take(3).forEachIndexed { index, preset ->
                            val isSelected = selectedPresetIndex == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) ExpenseRed
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { selectedPresetIndex = index }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Loc.t(preset, lang),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        expensePresets.drop(3).forEachIndexed { idx, preset ->
                            val actualIndex = idx + 3
                            val isSelected = selectedPresetIndex == actualIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) ExpenseRed
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { selectedPresetIndex = actualIndex }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Loc.t(preset, lang),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(Loc.t("expense_title", lang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("exp_field_title"),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { title = Loc.t(expensePresets[selectedPresetIndex], lang) }) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = "Autofill")
                            }
                        }
                    )

                    // Amount
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(Loc.t("amount", lang) + " ($currency)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("exp_field_amount"),
                        singleLine = true
                    )

                    // Note
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(Loc.t("note", lang)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amtVal = amount.toDoubleOrNull() ?: 0.0
                        val finalTitle = title.ifBlank { Loc. t(expensePresets[selectedPresetIndex], lang) }
                        if (amtVal > 0) {
                            viewModel.addExpense(
                                title = finalTitle,
                                amount = amtVal,
                                note = note.ifBlank { null }
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    modifier = Modifier.testTag("save_expense_button")
                ) {
                    Text(Loc.t("save", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(Loc.t("cancel", lang))
                }
            }
        )
    }

    // Delete confirmation dialog
    if (selectedExpenseForDelete != null) {
        AlertDialog(
            onDismissRequest = { selectedExpenseForDelete = null },
            title = { Text(if (lang == "bn") "খরচের হিসাব ডিলিট" else "Delete Expense") },
            text = { Text(if (lang == "bn") "আপনি কি নিশ্চিতভাবে এই খরচের হিসাবটি ডিলিট করতে চান?" else "Are you sure you want to delete this expense record?") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedExpenseForDelete?.let { viewModel.deleteExpense(it) }
                        selectedExpenseForDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text(Loc.t("delete", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedExpenseForDelete = null }) {
                    Text(Loc.t("cancel", lang))
                }
            }
        )
    }
}
