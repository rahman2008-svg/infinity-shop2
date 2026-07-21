package com.example.data

import kotlinx.coroutines.flow.Flow

class ShopRepository(private val db: AppDatabase) {
    val shopConfig: Flow<ShopConfig?> = db.shopConfigDao().getShopConfig()
    val allProducts: Flow<List<Product>> = db.productDao().getAllProducts()
    val allCustomers: Flow<List<Customer>> = db.customerDao().getAllCustomers()
    val allExpenses: Flow<List<Expense>> = db.expenseDao().getAllExpenses()
    val allSales: Flow<List<Sale>> = db.saleDao().getAllSales()
    val salesWithItems: Flow<List<SaleWithItems>> = db.saleDao().getSalesWithItems()

    suspend fun getShopConfigDirect(): ShopConfig? {
        return db.shopConfigDao().getShopConfigDirect()
    }

    suspend fun saveShopConfig(config: ShopConfig) {
        db.shopConfigDao().insertOrUpdate(config)
    }

    suspend fun insertProduct(product: Product): Long {
        return db.productDao().insertProduct(product)
    }

    suspend fun deleteProduct(product: Product) {
        db.productDao().deleteProduct(product)
    }

    suspend fun updateStock(productId: Long, newStock: Int) {
        db.productDao().updateStock(productId, newStock)
    }

    suspend fun getProductByBarcode(barcode: String): Product? {
        return db.productDao().getProductByBarcode(barcode)
    }

    fun searchProducts(query: String): Flow<List<Product>> {
        return db.productDao().searchProducts(query)
    }

    suspend fun insertCustomer(customer: Customer): Long {
        return db.customerDao().insertCustomer(customer)
    }

    suspend fun deleteCustomer(customer: Customer) {
        db.customerDao().deleteCustomer(customer)
    }

    suspend fun updateCustomerDue(customerId: Long, newDue: Double) {
        db.customerDao().updateCustomerDue(customerId, newDue)
    }

    fun searchCustomers(query: String): Flow<List<Customer>> {
        return db.customerDao().searchCustomers(query)
    }

    suspend fun insertExpense(expense: Expense): Long {
        return db.expenseDao().insertExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        db.expenseDao().deleteExpense(expense)
    }

    suspend fun insertSaleInvoice(sale: Sale, items: List<SaleItem>) {
        db.saleDao().insertSaleInvoice(sale, items)
    }

    suspend fun deleteSale(sale: Sale) {
        db.saleDao().deleteSale(sale)
    }

    suspend fun getSaleWithItemsById(id: Long): SaleWithItems? {
        return db.saleDao().getSaleWithItemsById(id)
    }

    fun searchSales(query: String): Flow<List<Sale>> {
        return db.saleDao().searchSales(query)
    }
}
