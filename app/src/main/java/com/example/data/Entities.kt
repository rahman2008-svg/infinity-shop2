package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.Embedded

@Entity(tableName = "shop_config")
data class ShopConfig(
    @PrimaryKey val id: Int = 1, // Single row configuration
    val shopName: String = "",
    val ownerName: String = "",
    val mobileNumber: String = "",
    val address: String = "",
    val businessType: String = "", // e.g. "Grocery", "Pharmacy"
    val currency: String = "৳", // Default BDT
    val pinLock: String? = null, // PIN code
    val isSetupCompleted: Boolean = false,
    val isPremium: Boolean = false,
    val taxRate: Double = 0.0,
    val language: String = "bn" // "bn" or "en"
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val buyPrice: Double,
    val sellPrice: Double,
    val stock: Int,
    val barcode: String? = null,
    val imageUri: String? = null // Optional image reference
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val address: String = "",
    val previousDue: Double = 0.0,
    val note: String? = null
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String, // e.g. "Rent", "Electricity", "Salary", "Transport"
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val note: String? = null
)

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerName: String? = null, // Store denormalized name for history
    val date: Long = System.currentTimeMillis(),
    val subTotal: Double,
    val discount: Double,
    val totalAmount: Double,
    val paidAmount: Double,
    val dueAmount: Double,
    val paymentMethod: String // "Cash", "bKash", "Nagad", "Card"
)

@Entity(tableName = "sale_items")
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long, // FK to Sale.id
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val sellPrice: Double
)

data class SaleWithItems(
    @Embedded val sale: Sale,
    @Relation(
        parentColumn = "id",
        entityColumn = "saleId"
    )
    val items: List<SaleItem>
)
