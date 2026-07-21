package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Loc
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    viewModel: ShopViewModel,
    onSetupComplete: () -> Unit
) {
    val lang by viewModel.appLanguage.collectAsState()

    var shopName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    val businessTypes = listOf(
        "biz_grocery", "biz_pharmacy", "biz_stationery",
        "biz_mobile", "biz_cosmetics", "biz_clothing",
        "biz_hardware", "biz_other"
    )
    var selectedBizKey by remember { mutableStateOf(businessTypes[0]) }
    var bizMenuExpanded by remember { mutableStateOf(false) }

    val currencies = listOf("৳", "₹", "$", "€", "£")
    var selectedCurrency by remember { mutableStateOf(currencies[0]) }
    var currencyMenuExpanded by remember { mutableStateOf(false) }

    var showError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = Loc.t("shop_setup", lang),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = Loc.t("shop_info", lang),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Shop Name Input
                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text(Loc.t("shop_name", lang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_shop_name"),
                        leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                        singleLine = true
                    )

                    // Owner Name Input
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text(Loc.t("owner_name", lang)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_owner_name"),
                        singleLine = true
                    )

                    // Phone Number Input
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(Loc.t("phone_num", lang)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_phone"),
                        singleLine = true
                    )

                    // Address Input
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(Loc.t("address", lang)) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Business Type Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = Loc.t(selectedBizKey, lang),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(Loc.t("biz_type", lang)) },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { bizMenuExpanded = true }
                                )
                            }
                        )
                        DropdownMenu(
                            expanded = bizMenuExpanded,
                            onDismissRequest = { bizMenuExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            businessTypes.forEach { key ->
                                DropdownMenuItem(
                                    text = { Text(Loc.t(key, lang)) },
                                    onClick = {
                                        selectedBizKey = key
                                        bizMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Currency Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedCurrency,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(Loc.t("currency_select", lang)) },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { currencyMenuExpanded = true }
                                )
                            }
                        )
                        DropdownMenu(
                            expanded = currencyMenuExpanded,
                            onDismissRequest = { currencyMenuExpanded = false }
                        ) {
                            currencies.forEach { curr ->
                                DropdownMenuItem(
                                    text = { Text(curr) },
                                    onClick = {
                                        selectedCurrency = curr
                                        currencyMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = Loc.t("pin_lock", lang),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = Loc.t("pin_lock_desc", lang),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    OutlinedTextField(
                        value = pin,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                pin = it
                            }
                        },
                        label = { Text("PIN Lock") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_pin_field"),
                        singleLine = true
                    )
                }
            }

            if (showError) {
                Text(
                    text = if (lang == "bn") "দয়া করে দোকানের নাম এবং মোবাইল নম্বর লিখুন!" else "Please enter shop name and mobile number!",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (shopName.isBlank() || phone.isBlank()) {
                        showError = true
                    } else {
                        showError = false
                        viewModel.completeSetup(
                            shopName = shopName,
                            ownerName = ownerName,
                            mobileNumber = phone,
                            address = address,
                            businessType = Loc.t(selectedBizKey, "en"),
                            currency = selectedCurrency,
                            pin = pin
                        )
                        onSetupComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("complete_setup_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = Loc.t("complete_setup", lang),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
