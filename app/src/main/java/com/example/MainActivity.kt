package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppDatabase
import com.example.data.Loc
import com.example.data.ShopRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ShopViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewModel using standard delegates
        val viewModel: ShopViewModel by viewModels()

        setContent {
            val isDark by viewModel.isDarkTheme.collectAsState()
            val configState by viewModel.shopConfig.collectAsState()
            val lang by viewModel.appLanguage.collectAsState()

            MyApplicationTheme(darkTheme = isDark, dynamicColor = false) {
                // Application Stateful Router
                var currentScreen by remember { mutableStateOf("loading") }
                var isVerifiedPin by remember { mutableStateOf(false) }

                var activeTab by remember { mutableStateOf("dashboard") }
                var selectedInvoiceId by remember { mutableStateOf<Long?>(null) }

                // Synchronize Router State based on Database State
                LaunchedEffect(configState) {
                    val config = configState
                    if (config == null) {
                        currentScreen = "loading"
                    } else if (!config.isSetupCompleted) {
                        currentScreen = "welcome"
                        isVerifiedPin = false
                    } else if (!config.pinLock.isNullOrBlank() && !isVerifiedPin) {
                        currentScreen = "pin_lock"
                    } else {
                        currentScreen = "main_app"
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        "loading" -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Infinity Shop is loading...",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        "welcome" -> {
                            WelcomeScreen(
                                viewModel = viewModel,
                                onGetStarted = {
                                    currentScreen = "setup"
                                }
                            )
                        }

                        "setup" -> {
                            SetupScreen(
                                viewModel = viewModel,
                                onSetupComplete = {
                                    isVerifiedPin = true
                                    currentScreen = "main_app"
                                }
                            )
                        }

                        "pin_lock" -> {
                            PinLockScreen(
                                viewModel = viewModel,
                                onVerified = {
                                    isVerifiedPin = true
                                    currentScreen = "main_app"
                                }
                            )
                        }

                        "invoice" -> {
                            selectedInvoiceId?.let { sId ->
                                InvoiceScreen(
                                    viewModel = viewModel,
                                    saleId = sId,
                                    onBack = {
                                        currentScreen = "main_app"
                                    }
                                )
                            }
                        }

                        "main_app" -> {
                            // Unified Main Scaffold with Bottom Navigation Bar
                            Scaffold(
                                modifier = Modifier.fillMaxSize(),
                                bottomBar = {
                                    NavigationBar(
                                        modifier = Modifier.testTag("bottom_nav_bar")
                                    ) {
                                        NavigationBarItem(
                                            selected = activeTab == "dashboard",
                                            onClick = { activeTab = "dashboard" },
                                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                            label = { Text(Loc.t("dashboard", lang), fontSize = 10.sp) },
                                            modifier = Modifier.testTag("nav_tab_dashboard")
                                        )
                                        NavigationBarItem(
                                            selected = activeTab == "sales",
                                            onClick = { activeTab = "sales" },
                                            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Sales") },
                                            label = { Text(Loc.t("new_sale", lang), fontSize = 10.sp) },
                                            modifier = Modifier.testTag("nav_tab_sales")
                                        )
                                        NavigationBarItem(
                                            selected = activeTab == "products",
                                            onClick = { activeTab = "products" },
                                            icon = { Icon(Icons.Default.Inventory2, contentDescription = "Products") },
                                            label = { Text(Loc.t("product", lang), fontSize = 10.sp) },
                                            modifier = Modifier.testTag("nav_tab_products")
                                        )
                                        NavigationBarItem(
                                            selected = activeTab == "customers",
                                            onClick = { activeTab = "customers" },
                                            icon = { Icon(Icons.Default.Group, contentDescription = "Customers") },
                                            label = { Text(Loc.t("customer", lang), fontSize = 10.sp) },
                                            modifier = Modifier.testTag("nav_tab_customers")
                                        )
                                        NavigationBarItem(
                                            selected = activeTab == "expenses",
                                            onClick = { activeTab = "expenses" },
                                            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Expenses") },
                                            label = { Text(Loc.t("expense", lang), fontSize = 10.sp) },
                                            modifier = Modifier.testTag("nav_tab_expenses")
                                        )
                                        NavigationBarItem(
                                            selected = activeTab == "reports",
                                            onClick = { activeTab = "reports" },
                                            icon = { Icon(Icons.Default.BarChart, contentDescription = "Reports") },
                                            label = { Text(Loc.t("reports", lang), fontSize = 10.sp) },
                                            modifier = Modifier.testTag("nav_tab_reports")
                                        )
                                        NavigationBarItem(
                                            selected = activeTab == "settings",
                                            onClick = { activeTab = "settings" },
                                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                            label = { Text(Loc.t("settings", lang), fontSize = 10.sp) },
                                            modifier = Modifier.testTag("nav_tab_settings")
                                        )
                                    }
                                }
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    when (activeTab) {
                                        "dashboard" -> {
                                            DashboardScreen(
                                                viewModel = viewModel,
                                                onNavigate = { route ->
                                                    if (route.startsWith("invoice/")) {
                                                        val id = route.substringAfter("/").toLongOrNull()
                                                        if (id != null) {
                                                            selectedInvoiceId = id
                                                            currentScreen = "invoice"
                                                        }
                                                    } else {
                                                        activeTab = route
                                                    }
                                                }
                                            )
                                        }

                                        "sales" -> {
                                            SalesScreen(
                                                viewModel = viewModel,
                                                onCheckoutSuccess = { saleId ->
                                                    selectedInvoiceId = saleId
                                                    currentScreen = "invoice"
                                                }
                                            )
                                        }

                                        "products" -> {
                                            ProductsScreen(viewModel = viewModel)
                                        }

                                        "customers" -> {
                                            CustomersScreen(viewModel = viewModel)
                                        }

                                        "expenses" -> {
                                            ExpensesScreen(viewModel = viewModel)
                                        }

                                        "reports" -> {
                                            ReportsScreen(viewModel = viewModel)
                                        }

                                        "settings" -> {
                                            SettingsScreen(
                                                viewModel = viewModel,
                                                onResetCompleted = {
                                                    isVerifiedPin = false
                                                    activeTab = "dashboard"
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
