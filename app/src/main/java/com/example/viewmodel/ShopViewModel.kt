package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ShopViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = ShopRepository(db)

    // Language setting, default to "bn" (Bengali)
    val appLanguage = repository.shopConfig.map { it?.language ?: "bn" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "bn")

    val shopConfig = repository.shopConfig.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val products = repository.allProducts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val customers = repository.allCustomers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val expenses = repository.allExpenses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val sales = repository.allSales.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val salesWithItems = repository.salesWithItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Active Checkout State
    val cartItems = MutableStateFlow<Map<Product, Int>>(emptyMap())
    val cartDiscount = MutableStateFlow(0.0)
    val cartPaidAmount = MutableStateFlow(0.0)
    val cartCustomerId = MutableStateFlow<Long?>(null)
    val cartPaymentMethod = MutableStateFlow("Cash") // "Cash", "bKash", "Nagad", "Card"

    // Search query
    val searchQuery = MutableStateFlow("")

    // Notification Alerts (Low stock, due reminders, etc.)
    private val _notifications = MutableStateFlow<List<String>>(emptyList())
    val notifications = _notifications.asStateFlow()

    // Dark Theme setting, default true (Cosmic dark theme)
    val isDarkTheme = MutableStateFlow(true)

    init {
        // Build initial config if none exists
        viewModelScope.launch {
            val current = repository.getShopConfigDirect()
            if (current == null) {
                repository.saveShopConfig(ShopConfig())
            }
            // Populate low stock and payment alerts
            triggerNotificationAlerts()
        }
    }

    fun triggerNotificationAlerts() {
        viewModelScope.launch {
            val alerts = mutableListOf<String>()
            val config = repository.getShopConfigDirect() ?: ShopConfig()
            val lang = config.language

            // Check Low Stock
            val lowStockProducts = products.value.filter { it.stock <= 5 }
            if (lowStockProducts.isNotEmpty()) {
                alerts.add(Loc.t("low_stock_notif", lang))
            }

            // Check Customer Dues
            val highDues = customers.value.filter { it.previousDue > 0 }
            if (highDues.isNotEmpty()) {
                alerts.add(Loc.t("due_payment_notif", lang))
            }

            // Sales Alert
            if (sales.value.isNotEmpty()) {
                alerts.add(Loc.t("daily_sales_notif", lang))
            }

            _notifications.value = alerts
        }
    }

    // 1. Setup functions
    fun completeSetup(
        shopName: String,
        ownerName: String,
        mobileNumber: String,
        address: String,
        businessType: String,
        currency: String,
        pin: String?
    ) {
        viewModelScope.launch {
            val current = repository.getShopConfigDirect() ?: ShopConfig()
            val updated = current.copy(
                shopName = shopName,
                ownerName = ownerName,
                mobileNumber = mobileNumber,
                address = address,
                businessType = businessType,
                currency = currency,
                pinLock = if (pin.isNullOrBlank()) null else pin,
                isSetupCompleted = true
            )
            repository.saveShopConfig(updated)
            triggerNotificationAlerts()
        }
    }

    // 2. Product operations
    fun addProduct(
        name: String,
        category: String,
        buyPrice: Double,
        sellPrice: Double,
        stock: Int,
        barcode: String?,
        imageUri: String? = null
    ) {
        viewModelScope.launch {
            val config = repository.getShopConfigDirect() ?: ShopConfig()
            // Limit products for non-premium users
            if (!config.isPremium && products.value.size >= 15) {
                // Limit reached
                return@launch
            }
            val product = Product(
                name = name,
                category = category,
                buyPrice = buyPrice,
                sellPrice = sellPrice,
                stock = stock,
                barcode = if (barcode.isNullOrBlank()) null else barcode,
                imageUri = imageUri
            )
            repository.insertProduct(product)
            triggerNotificationAlerts()
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.insertProduct(product)
            triggerNotificationAlerts()
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            triggerNotificationAlerts()
        }
    }

    // 3. Customer operations
    fun addCustomer(
        name: String,
        phone: String,
        address: String = "",
        prevDue: Double = 0.0,
        note: String? = null
    ) {
        viewModelScope.launch {
            val customer = Customer(
                name = name,
                phone = phone,
                address = address,
                previousDue = prevDue,
                note = note
            )
            repository.insertCustomer(customer)
            triggerNotificationAlerts()
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            triggerNotificationAlerts()
        }
    }

    fun receiveDuePayment(customerId: Long, payAmount: Double) {
        viewModelScope.launch {
            val customer = customers.value.find { it.id == customerId }
            if (customer != null) {
                val newDue = (customer.previousDue - payAmount).coerceAtLeast(0.0)
                repository.updateCustomerDue(customerId, newDue)
                
                // Add an expense or credit note, let's keep it simple by just updating the due.
                triggerNotificationAlerts()
            }
        }
    }

    // 4. Expense operations
    fun addExpense(title: String, amount: Double, note: String? = null) {
        viewModelScope.launch {
            val expense = Expense(
                title = title,
                amount = amount,
                note = note
            )
            repository.insertExpense(expense)
            triggerNotificationAlerts()
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            triggerNotificationAlerts()
        }
    }

    // 5. Checkout Cart operations
    fun addToCart(product: Product, quantity: Int = 1) {
        val current = cartItems.value.toMutableMap()
        val existingQty = current[product] ?: 0
        val targetQty = (existingQty + quantity).coerceIn(1, product.stock)
        current[product] = targetQty
        cartItems.value = current
    }

    fun updateCartQuantity(product: Product, quantity: Int) {
        val current = cartItems.value.toMutableMap()
        if (quantity <= 0) {
            current.remove(product)
        } else {
            current[product] = quantity.coerceAtMost(product.stock)
        }
        cartItems.value = current
    }

    fun removeFromCart(product: Product) {
        val current = cartItems.value.toMutableMap()
        current.remove(product)
        cartItems.value = current
    }

    fun clearCart() {
        cartItems.value = emptyMap()
        cartDiscount.value = 0.0
        cartPaidAmount.value = 0.0
        cartCustomerId.value = null
        cartPaymentMethod.value = "Cash"
    }

    // Process complete checkout and save sale invoice
    fun checkout(onSuccess: (Long) -> Unit) {
        val items = cartItems.value
        if (items.isEmpty()) return

        viewModelScope.launch {
            val config = repository.getShopConfigDirect() ?: ShopConfig()
            
            // Generate invoice number
            val count = sales.value.size + 1
            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val invoiceNo = "INV-$dateStr-$count"

            // Compute subtotal
            var subTotal = 0.0
            items.forEach { (prod, qty) ->
                subTotal += prod.sellPrice * qty
            }

            val discount = cartDiscount.value
            val taxAmount = subTotal * (config.taxRate / 100.0)
            val totalAmount = (subTotal + taxAmount - discount).coerceAtLeast(0.0)
            val paidAmount = cartPaidAmount.value.coerceAtMost(totalAmount)
            val dueAmount = (totalAmount - paidAmount).coerceAtLeast(0.0)

            // Customer relation
            val selectedCustId = cartCustomerId.value
            val customerName = if (selectedCustId != null) {
                customers.value.find { it.id == selectedCustId }?.name
            } else {
                "Anonymous"
            }

            // Save Sale Invoice
            val sale = Sale(
                invoiceNumber = invoiceNo,
                customerId = selectedCustId,
                customerName = customerName,
                subTotal = subTotal,
                discount = discount,
                totalAmount = totalAmount,
                paidAmount = paidAmount,
                dueAmount = dueAmount,
                paymentMethod = cartPaymentMethod.value
            )

            // Prepare items
            val saleItems = items.map { (prod, qty) ->
                SaleItem(
                    saleId = 0, // Assigned inside the transaction
                    productId = prod.id,
                    productName = prod.name,
                    quantity = qty,
                    sellPrice = prod.sellPrice
                )
            }

            // Save via Transaction Dao
            repository.insertSaleInvoice(sale, saleItems)

            // Reduce product stock in DB
            items.forEach { (prod, qty) ->
                val newStock = (prod.stock - qty).coerceAtLeast(0)
                repository.updateStock(prod.id, newStock)
            }

            // If dueAmount > 0 and registered customer selected, update customer's due balance
            if (dueAmount > 0 && selectedCustId != null) {
                val cust = customers.value.find { it.id == selectedCustId }
                if (cust != null) {
                    val updatedDue = cust.previousDue + dueAmount
                    repository.updateCustomerDue(selectedCustId, updatedDue)
                }
            }

            // Get last saved sale ID to pass to receipt
            val updatedSalesList = db.saleDao().getAllSales().first()
            val newlyCreatedSale = updatedSalesList.find { it.invoiceNumber == invoiceNo }
            val newlyCreatedId = newlyCreatedSale?.id ?: 0L

            // Reset checkout state
            clearCart()
            triggerNotificationAlerts()

            onSuccess(newlyCreatedId)
        }
    }

    // 6. Settings & Config editing
    fun togglePremium() {
        viewModelScope.launch {
            val current = repository.getShopConfigDirect() ?: ShopConfig()
            val updated = current.copy(isPremium = !current.isPremium)
            repository.saveShopConfig(updated)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            val current = repository.getShopConfigDirect() ?: ShopConfig()
            val updated = current.copy(language = lang)
            repository.saveShopConfig(updated)
            triggerNotificationAlerts()
        }
    }

    fun setTaxRate(rate: Double) {
        viewModelScope.launch {
            val current = repository.getShopConfigDirect() ?: ShopConfig()
            val updated = current.copy(taxRate = rate)
            repository.saveShopConfig(updated)
        }
    }

    fun updatePinLock(pin: String?) {
        viewModelScope.launch {
            val current = repository.getShopConfigDirect() ?: ShopConfig()
            val updated = current.copy(pinLock = if (pin.isNullOrBlank()) null else pin)
            repository.saveShopConfig(updated)
        }
    }

    fun resetShop() {
        viewModelScope.launch {
            // Delete all tables and reset config
            db.clearAllTables()
            repository.saveShopConfig(ShopConfig())
            clearCart()
            triggerNotificationAlerts()
        }
    }

    // 7. Real File JSON Backup and Restore
    fun backupData(context: Context, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val backupObject = JSONObject()

                // 1. ShopConfig
                val config = repository.getShopConfigDirect() ?: ShopConfig()
                val configObj = JSONObject().apply {
                    put("shopName", config.shopName)
                    put("ownerName", config.ownerName)
                    put("mobileNumber", config.mobileNumber)
                    put("address", config.address)
                    put("businessType", config.businessType)
                    put("currency", config.currency)
                    put("pinLock", config.pinLock ?: "")
                    put("isSetupCompleted", config.isSetupCompleted)
                    put("isPremium", config.isPremium)
                    put("taxRate", config.taxRate)
                    put("language", config.language)
                }
                backupObject.put("config", configObj)

                // 2. Products
                val prodArray = JSONArray()
                products.value.forEach { prod ->
                    prodArray.put(JSONObject().apply {
                        put("name", prod.name)
                        put("category", prod.category)
                        put("buyPrice", prod.buyPrice)
                        put("sellPrice", prod.sellPrice)
                        put("stock", prod.stock)
                        put("barcode", prod.barcode ?: "")
                    })
                }
                backupObject.put("products", prodArray)

                // 3. Customers
                val custArray = JSONArray()
                customers.value.forEach { cust ->
                    custArray.put(JSONObject().apply {
                        put("name", cust.name)
                        put("phone", cust.phone)
                        put("address", cust.address)
                        put("previousDue", cust.previousDue)
                        put("note", cust.note ?: "")
                    })
                }
                backupObject.put("customers", custArray)

                // 4. Expenses
                val expArray = JSONArray()
                expenses.value.forEach { exp ->
                    expArray.put(JSONObject().apply {
                        put("title", exp.title)
                        put("amount", exp.amount)
                        put("date", exp.date)
                        put("note", exp.note ?: "")
                    })
                }
                backupObject.put("expenses", expArray)

                // Write backup to standard app directory
                val backupFile = File(context.filesDir, "infinity_shop_backup.json")
                backupFile.writeText(backupObject.toString(2))

                onResult(true, backupFile.absolutePath)
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Backup failed")
            }
        }
    }

    fun restoreData(context: Context, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val backupFile = File(context.filesDir, "infinity_shop_backup.json")
                if (!backupFile.exists()) {
                    onResult(false, "No backup file found at ${backupFile.name}")
                    return@launch
                }

                val backupStr = backupFile.readText()
                val backupObj = JSONObject(backupStr)

                // Clear existing
                db.clearAllTables()

                // Restore Config
                if (backupObj.has("config")) {
                    val configObj = backupObj.getJSONObject("config")
                    val config = ShopConfig(
                        shopName = configObj.optString("shopName"),
                        ownerName = configObj.optString("ownerName"),
                        mobileNumber = configObj.optString("mobileNumber"),
                        address = configObj.optString("address"),
                        businessType = configObj.optString("businessType"),
                        currency = configObj.optString("currency", "৳"),
                        pinLock = configObj.optString("pinLock").let { if (it.isEmpty()) null else it },
                        isSetupCompleted = configObj.optBoolean("isSetupCompleted", true),
                        isPremium = configObj.optBoolean("isPremium", false),
                        taxRate = configObj.optDouble("taxRate", 0.0),
                        language = configObj.optString("language", "bn")
                    )
                    repository.saveShopConfig(config)
                }

                // Restore Products
                if (backupObj.has("products")) {
                    val prods = backupObj.getJSONArray("products")
                    for (i in 0 until prods.length()) {
                        val item = prods.getJSONObject(i)
                        repository.insertProduct(
                            Product(
                                name = item.getString("name"),
                                category = item.getString("category"),
                                buyPrice = item.getDouble("buyPrice"),
                                sellPrice = item.getDouble("sellPrice"),
                                stock = item.getInt("stock"),
                                barcode = item.optString("barcode").let { if (it.isEmpty()) null else it }
                            )
                        )
                    }
                }

                // Restore Customers
                if (backupObj.has("customers")) {
                    val custs = backupObj.getJSONArray("customers")
                    for (i in 0 until custs.length()) {
                        val item = custs.getJSONObject(i)
                        repository.insertCustomer(
                            Customer(
                                name = item.getString("name"),
                                phone = item.getString("phone"),
                                address = item.optString("address"),
                                previousDue = item.optDouble("previousDue", 0.0),
                                note = item.optString("note").let { if (it.isEmpty()) null else it }
                            )
                        )
                    }
                }

                // Restore Expenses
                if (backupObj.has("expenses")) {
                    val exps = backupObj.getJSONArray("expenses")
                    for (i in 0 until exps.length()) {
                        val item = exps.getJSONObject(i)
                        repository.insertExpense(
                            Expense(
                                title = item.getString("title"),
                                amount = item.getDouble("amount"),
                                date = item.optLong("date", System.currentTimeMillis()),
                                note = item.optString("note").let { if (it.isEmpty()) null else it }
                            )
                        )
                    }
                }

                triggerNotificationAlerts()
                onResult(true, "Data restored successfully")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Restore failed")
            }
        }
    }
}
