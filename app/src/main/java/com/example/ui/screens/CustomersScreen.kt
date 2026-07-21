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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Customer
import com.example.data.Loc
import com.example.ui.theme.DueOrange
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(viewModel: ShopViewModel) {
    val lang by viewModel.appLanguage.collectAsState()
    val config by viewModel.shopConfig.collectAsState()
    val currency = config?.currency ?: "৳"

    val customersList by viewModel.customers.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCust by remember { mutableStateOf<Customer?>(null) }
    var showPayDialog by remember { mutableStateOf(false) }

    // Form fields
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var prevDue by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    // Due Payment Field
    var payInputAmount by remember { mutableStateOf("") }

    val filteredCustomers = customersList.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedCust = null
                    name = ""
                    phone = ""
                    address = ""
                    prevDue = ""
                    note = ""
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("add_customer_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Add Customer",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Input
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(Loc.t("search_hint", lang)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_search_input"),
                    singleLine = true
                )
            }

            // Customer List
            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (lang == "bn") "কোনো কাস্টমার পাওয়া যায়নি!" else "No customers recorded!",
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
                    items(filteredCustomers) { customer ->
                        val hasDue = customer.previousDue > 0
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCust = customer
                                    payInputAmount = ""
                                    showPayDialog = true
                                }
                                .testTag("customer_item_${customer.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (hasDue) DueOrange.copy(alpha = if (isSystemInDarkTheme()) 0.15f else 0.06f)
                                else if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else Color.White
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
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (hasDue) DueOrange.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (hasDue) DueOrange else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = customer.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = customer.phone,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                if (hasDue) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "$currency${String.format("%.1f", customer.previousDue)}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = DueOrange
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = Loc.t("total_due", lang),
                                            fontSize = 10.sp,
                                            color = DueOrange,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Cleared",
                                        tint = ProfitGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Customer Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = Loc.t("new_customer", lang),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(Loc.t("customer_name", lang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cust_field_name"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        label = { Text(Loc.t("customer_phone", lang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cust_field_phone"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(Loc.t("address", lang)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = prevDue,
                        onValueChange = { prevDue = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(Loc.t("prev_due", lang) + " ($currency)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cust_field_due"),
                        singleLine = true
                    )
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
                        val dueVal = prevDue.toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank() && phone.isNotBlank()) {
                            viewModel.addCustomer(
                                name = name,
                                phone = phone,
                                address = address,
                                prevDue = dueVal,
                                note = note.ifBlank { null }
                            )
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_customer_button")
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

    // Customer Detail and Due Payment collection dialog
    if (showPayDialog && selectedCust != null) {
        val customer = selectedCust!!
        AlertDialog(
            onDismissRequest = { showPayDialog = false },
            title = {
                Text(
                    text = customer.name,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${Loc.t("customer_phone", lang)}: ${customer.phone}",
                        fontSize = 14.sp
                    )
                    if (customer.address.isNotEmpty()) {
                        Text(
                            text = "${Loc.t("address", lang)}: ${customer.address}",
                            fontSize = 14.sp
                        )
                    }
                    if (!customer.note.isNullOrEmpty()) {
                        Text(
                            text = "${Loc.t("note", lang)}: ${customer.note}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    if (customer.previousDue > 0) {
                        Divider()
                        Text(
                            text = "${Loc.t("total_due", lang)}: $currency${customer.previousDue}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DueOrange
                        )

                        Text(
                            text = Loc.t("due_payment", lang),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = payInputAmount,
                            onValueChange = { payInputAmount = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text(Loc.t("pay_amount", lang)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("due_pay_input"),
                            singleLine = true,
                            trailingIcon = {
                                // Full Payment Button
                                Text(
                                    text = "Full",
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .clickable { payInputAmount = customer.previousDue.toString() }
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (customer.previousDue > 0) {
                        Button(
                            onClick = {
                                val amt = payInputAmount.toDoubleOrNull() ?: 0.0
                                if (amt > 0) {
                                    viewModel.receiveDuePayment(customer.id, amt)
                                    showPayDialog = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("confirm_due_pay")
                        ) {
                            Text(Loc.t("save", lang))
                        }
                    }

                    TextButton(
                        onClick = {
                            viewModel.deleteCustomer(customer)
                            showPayDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }

                    TextButton(onClick = { showPayDialog = false }) {
                        Text(Loc.t("cancel", lang))
                    }
                }
            }
        )
    }
}
