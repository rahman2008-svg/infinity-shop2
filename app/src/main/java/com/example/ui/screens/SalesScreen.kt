package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.data.*
import com.example.ui.theme.DueOrange
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    viewModel: ShopViewModel,
    onCheckoutSuccess: (Long) -> Unit
) {
    val lang by viewModel.appLanguage.collectAsState()
    val config by viewModel.shopConfig.collectAsState()
    val currency = config?.currency ?: "৳"
    val isPremium = config?.isPremium == true

    val productsList by viewModel.products.collectAsState()
    val customersList by viewModel.customers.collectAsState()

    val cart by viewModel.cartItems.collectAsState()
    val discount by viewModel.cartDiscount.collectAsState()
    val paidAmount by viewModel.cartPaidAmount.collectAsState()
    val selectedCustId by viewModel.cartCustomerId.collectAsState()
    val paymentMethod by viewModel.cartPaymentMethod.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var barcodeInputQuery by remember { mutableStateOf("") }
    var showBarcodeDialog by remember { mutableStateOf(false) }

    var custDropdownExpanded by remember { mutableStateOf(false) }

    // Filtered Products
    val filteredProducts = productsList.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    // Calculations
    val subtotal = cart.entries.sumOf { it.key.sellPrice * it.value }
    val taxAmount = subtotal * ((config?.taxRate ?: 0.0) / 100.0)
    val totalAmount = (subtotal + taxAmount - discount).coerceAtLeast(0.0)
    val dueAmount = (totalAmount - paidAmount).coerceAtLeast(0.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Barcode Scanner Sim Banner (Premium)
        if (isPremium) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .clickable { showBarcodeDialog = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Barcode",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (lang == "bn") "বারকোড স্ক্যানার সক্রিয়" else "Barcode Scanner Active",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (lang == "bn") "এখানে ক্লিক করে বারকোড স্ক্যান করুন" else "Click here to simulate barcode scanning",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 2. Product Search & Add Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Loc.t("select_product", lang),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("product_sales_search"),
                        placeholder = { Text(Loc.t("search_hint", lang)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Products Quick-add Chips
                    if (filteredProducts.isEmpty()) {
                        Text(
                            text = if (lang == "bn") "কোনো পণ্য পাওয়া যায়নি!" else "No products found!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 160.dp)
                        ) {
                            filteredProducts.take(5).forEach { prod ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable {
                                            if (prod.stock > 0) {
                                                viewModel.addToCart(prod, 1)
                                            }
                                        }
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = prod.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${Loc.t("stock", lang)}: ${prod.stock}",
                                            fontSize = 11.sp,
                                            color = if (prod.stock <= 5) ExpenseRed else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$currency${prod.sellPrice}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Active Checkout Cart List
        if (cart.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = Loc.t("cart", lang) + " (${cart.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        cart.forEach { (prod, qty) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prod.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$currency${prod.sellPrice} x $qty = $currency${prod.sellPrice * qty}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.updateCartQuantity(prod, qty - 1) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant,
                                                CircleShape
                                            )
                                    ) {
                                        Icon(
                                            Icons.Default.Remove,
                                            contentDescription = "Less",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = qty.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    IconButton(
                                        onClick = { viewModel.updateCartQuantity(prod, qty + 1) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant,
                                                CircleShape
                                            )
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "More",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeFromCart(prod) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = ExpenseRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }
        }

        // 4. Checkout Configurations (Customer, Discount, Payment Method)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (lang == "bn") "পরিশোধ বিবরণী" else "Payment Details",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Select Customer Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val currentCustomerName =
                            customersList.find { it.id == selectedCustId }?.name ?: (if (lang == "bn") "অতিথি কাস্টমার" else "Guest Customer")
                        OutlinedTextField(
                            value = currentCustomerName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(Loc.t("new_customer", lang)) },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { custDropdownExpanded = true }
                                )
                            }
                        )
                        DropdownMenu(
                            expanded = custDropdownExpanded,
                            onDismissRequest = { custDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (lang == "bn") "অতিথি কাস্টমার (guest)" else "Guest Customer (guest)") },
                                onClick = {
                                    viewModel.cartCustomerId.value = null
                                    custDropdownExpanded = false
                                }
                            )
                            customersList.forEach { cust ->
                                DropdownMenuItem(
                                    text = { Text("${cust.name} (${cust.phone})") },
                                    onClick = {
                                        viewModel.cartCustomerId.value = cust.id
                                        custDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Discount Field
                    OutlinedTextField(
                        value = if (discount == 0.0) "" else discount.toString(),
                        onValueChange = {
                            viewModel.cartDiscount.value = it.toDoubleOrNull() ?: 0.0
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(Loc.t("discount", lang) + " ($currency)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Payment Method selector
                    Text(
                        text = Loc.t("pay_method", lang),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    val payMethods = listOf("Cash", "bKash", "Nagad", "Card")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        payMethods.forEach { method ->
                            val isSelected = paymentMethod == method
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { viewModel.cartPaymentMethod.value = method }
                                    .padding(vertical = 10.dp)
                                    .testTag("pay_method_$method"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = method,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Paid Amount Field
                    OutlinedTextField(
                        value = if (paidAmount == 0.0) "" else paidAmount.toString(),
                        onValueChange = {
                            viewModel.cartPaidAmount.value = it.toDoubleOrNull() ?: 0.0
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(Loc.t("paid_amount", lang) + " ($currency)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sales_paid_field"),
                        singleLine = true,
                        trailingIcon = {
                            // Set Auto Pay button
                            Text(
                                text = "Auto",
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable { viewModel.cartPaidAmount.value = totalAmount }
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }
        }

        // 5. Invoice calculation card & Complete Sale button
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(Loc.t("subtotal", lang), fontSize = 14.sp)
                        Text("$currency${String.format("%.1f", subtotal)}", fontSize = 14.sp)
                    }

                    if (taxAmount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tax (${config?.taxRate}%)", fontSize = 14.sp)
                            Text("$currency${String.format("%.1f", taxAmount)}", fontSize = 14.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(Loc.t("discount", lang), fontSize = 14.sp, color = ExpenseRed)
                        Text("-$currency${String.format("%.1f", discount)}", fontSize = 14.sp, color = ExpenseRed)
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(Loc.t("net_total", lang), fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text(
                            text = "$currency${String.format("%.1f", totalAmount)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(Loc.t("due_amount", lang), fontSize = 14.sp, color = DueOrange)
                        Text("$currency${String.format("%.1f", dueAmount)}", fontSize = 14.sp, color = DueOrange, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (cart.isNotEmpty()) {
                                viewModel.checkout { saleId ->
                                    onCheckoutSuccess(saleId)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("checkout_button"),
                        enabled = cart.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = Loc.t("checkout", lang),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Barcode scanning Simulation dialog (Premium only)
    if (showBarcodeDialog) {
        AlertDialog(
            onDismissRequest = { showBarcodeDialog = false },
            title = { Text(if (lang == "bn") "বারকোড দিয়ে খুঁজুন" else "Find Product by Barcode") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (lang == "bn") "পণ্য খুজে পেতে বারকোড নম্বরটি লিখুন" else "Enter a product's barcode to simulate scan:",
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = barcodeInputQuery,
                        onValueChange = { barcodeInputQuery = it },
                        placeholder = { Text("e.g. 123456") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val matchingProd = productsList.find { it.barcode == barcodeInputQuery }
                        if (matchingProd != null && matchingProd.stock > 0) {
                            viewModel.addToCart(matchingProd, 1)
                        }
                        barcodeInputQuery = ""
                        showBarcodeDialog = false
                    }
                ) {
                    Text(Loc.t("search", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBarcodeDialog = false }) {
                    Text(Loc.t("cancel", lang))
                }
            }
        )
    }
}
