package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopConfigDao {
    @Query("SELECT * FROM shop_config WHERE id = 1 LIMIT 1")
    fun getShopConfig(): Flow<ShopConfig?>

    @Query("SELECT * FROM shop_config WHERE id = 1 LIMIT 1")
    suspend fun getShopConfigDirect(): ShopConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: ShopConfig)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET stock = :newStock WHERE id = :productId")
    suspend fun updateStock(productId: Long, newStock: Int)

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<Product>>
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("UPDATE customers SET previousDue = :newDue WHERE id = :customerId")
    suspend fun updateCustomerDue(customerId: Long, newDue: Double)

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'")
    fun searchCustomers(query: String): Flow<List<Customer>>
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Delete
    suspend fun deleteExpense(expense: Expense)
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Transaction
    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getSalesWithItems(): Flow<List<SaleWithItems>>

    @Transaction
    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    suspend fun getSaleWithItemsById(id: Long): SaleWithItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItem>)

    @Delete
    suspend fun deleteSale(sale: Sale)

    @Query("SELECT * FROM sales WHERE invoiceNumber LIKE '%' || :query || '%' OR customerName LIKE '%' || :query || '%'")
    fun searchSales(query: String): Flow<List<Sale>>

    @Transaction
    suspend fun insertSaleInvoice(sale: Sale, items: List<SaleItem>) {
        val saleId = insertSale(sale)
        val itemsWithSaleId = items.map { it.copy(saleId = saleId) }
        insertSaleItems(itemsWithSaleId)
    }
}
