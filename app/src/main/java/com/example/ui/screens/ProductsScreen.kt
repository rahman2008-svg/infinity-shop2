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
import com.example.data.Loc
import com.example.data.Product
import com.example.ui.theme.ExpenseRed
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(viewModel: ShopViewModel) {
    val lang by viewModel.appLanguage.collectAsState()
    val config by viewModel.shopConfig.collectAsState()
    val productsList by viewModel.products.collectAsState()
    val currency = config?.currency ?: "৳"
    val isPremium = config?.isPremium == true

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    // Form states
    var prodName by remember { mutableStateOf("") }
    var prodCategory by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }
    var sellPrice by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }

    val filteredProducts = productsList.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingProduct = null
                    prodName = ""
                    prodCategory = ""
                    buyPrice = ""
                    sellPrice = ""
                    stock = ""
                    barcode = ""
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Product",
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
            // Header Search Box
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(Loc.t("search_hint", lang)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("product_search_input"),
                    singleLine = true
                )

                // Non-premium warning
                if (!isPremium && productsList.size >= 15) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (lang == "bn")
                                    "ফ্রি ভার্সনে সর্বোচ্চ ১৫টি পণ্য যোগ করা যাবে। আনলিমিটেড পণ্যের জন্য প্রিমিয়াম অন করুন!"
                                else
                                    "Free limit reached (max 15 items). Enable premium in Settings to unlock unlimited items!",
                                fontSize = 11.sp,
                                color = ExpenseRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Products list
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (lang == "bn") "কোনো পণ্য পাওয়া যায়নি!" else "No products in store!",
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
                    items(filteredProducts) { product ->
                        val isLowStock = product.stock <= 5
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editingProduct = product
                                    prodName = product.name
                                    prodCategory = product.category
                                    buyPrice = product.buyPrice.toString()
                                    sellPrice = product.sellPrice.toString()
                                    stock = product.stock.toString()
                                    barcode = product.barcode ?: ""
                                    showAddDialog = true
                                }
                                .testTag("product_item_${product.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isLowStock) ExpenseRed.copy(alpha = if (isSystemInDarkTheme()) 0.15f else 0.06f)
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
                                            if (isLowStock) ExpenseRed.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isLowStock) Icons.Default.ProductionQuantityLimits else Icons.Default.Inventory2,
                                        contentDescription = null,
                                        tint = if (isLowStock) ExpenseRed else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${product.category} • ${Loc.t("buy_price", lang)}: $currency${product.buyPrice}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$currency${product.sellPrice}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${Loc.t("stock", lang)}: ${product.stock}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLowStock) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Product Dialog
    if (showAddDialog) {
        val isEdit = editingProduct != null
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = if (isEdit) Loc.t("edit_product", lang) else Loc.t("add_product", lang),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Name
                    OutlinedTextField(
                        value = prodName,
                        onValueChange = { prodName = it },
                        label = { Text(Loc.t("product_name", lang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prod_field_name"),
                        singleLine = true
                    )
                    // Category
                    OutlinedTextField(
                        value = prodCategory,
                        onValueChange = { prodCategory = it },
                        label = { Text(Loc.t("category", lang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prod_field_category"),
                        singleLine = true
                    )
                    // Buy Price
                    OutlinedTextField(
                        value = buyPrice,
                        onValueChange = { buyPrice = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(Loc.t("buy_price", lang) + " ($currency)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prod_field_buy"),
                        singleLine = true
                    )
                    // Sell Price
                    OutlinedTextField(
                        value = sellPrice,
                        onValueChange = { sellPrice = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(Loc.t("sell_price", lang) + " ($currency)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prod_field_sell"),
                        singleLine = true
                    )
                    // Stock
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(Loc.t("stock", lang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prod_field_stock"),
                        singleLine = true
                    )
                    // Barcode
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text(Loc.t("barcode", lang)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val bPrice = buyPrice.toDoubleOrNull() ?: 0.0
                        val sPrice = sellPrice.toDoubleOrNull() ?: 0.0
                        val stockQty = stock.toIntOrNull() ?: 0

                        if (prodName.isNotBlank() && prodCategory.isNotBlank()) {
                            if (isEdit) {
                                editingProduct?.let {
                                    viewModel.updateProduct(
                                        it.copy(
                                            name = prodName,
                                            category = prodCategory,
                                            buyPrice = bPrice,
                                            sellPrice = sPrice,
                                            stock = stockQty,
                                            barcode = barcode.ifBlank { null }
                                        )
                                    )
                                }
                            } else {
                                viewModel.addProduct(
                                    name = prodName,
                                    category = prodCategory,
                                    buyPrice = bPrice,
                                    sellPrice = sPrice,
                                    stock = stockQty,
                                    barcode = barcode.ifBlank { null }
                                )
                            }
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_product_button")
                ) {
                    Text(Loc.t("save", lang))
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isEdit) {
                        TextButton(
                            onClick = {
                                editingProduct?.let { viewModel.deleteProduct(it) }
                                showAddDialog = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
                        ) {
                            Text(Loc.t("delete", lang))
                        }
                    }
                    TextButton(onClick = { showAddDialog = false }) {
                        Text(Loc.t("cancel", lang))
                    }
                }
            }
        )
    }
}
