package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val email: String = "",
    val businessName: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stock_items")
data class StockItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val sku: String = "",
    val category: String, // "Cosmetics", "Shoes & Sandals", "Perfumes", "Children's Clothing"
    val price: Double, // This is the standard S.P. (Selling Price)
    val costPrice: Double = 0.0, // This is the standard C.P. (Cost Price)
    val stockQuantity: Int,
    val lowStockThreshold: Int = 5,
    val description: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceNumber: String, // e.g. "INV-1001"
    val clientId: Int,
    val clientName: String,
    val clientPhone: String,
    val invoiceDate: Long = System.currentTimeMillis(),
    val subtotal: Double,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val totalAmount: Double,
    val paymentStatus: String, // "Paid", "Unpaid", "Partial"
    val paymentMethod: String = "Cash", // "Cash", "Card", "Online", "WhatsApp Pay"
    val businessName: String = "Invoice & Inventory System", // customizable branding
    val businessLogoText: String = "BE",
    val businessPhone: String = "",
    val businessAddress: String = "",
    val notes: String = "",
    val deliveryCharges: Double = 0.0,
    val cancelReason: String = ""
)

@Entity(tableName = "invoice_items")
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceId: Int, // references Invoice.id
    val itemId: Int,    // references StockItem.id
    val itemName: String,
    val category: String,
    val unitPrice: Double, // Billed S.P. after itemized discount
    val originalPrice: Double = 0.0, // Original S.P. prior to discount
    val discountPerUnit: Double = 0.0, // Discount amount per item unit
    val quantity: Int,
    val totalPrice: Double
)

@Entity(tableName = "inventory_logs")
data class InventoryLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemId: Int,
    val itemName: String,
    val category: String,
    val actionType: String, // "STOCK_INITIALIZED", "STOCK_ADDED", "SALE_AUTO_DEDUCT", "MANUAL_ADJUST"
    val quantityChanged: Int,
    val reorderQuantity: Int, // quantity after change
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = ""
)
