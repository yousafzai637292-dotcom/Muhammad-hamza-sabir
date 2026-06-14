package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<Client>>

    @Query("SELECT * FROM clients WHERE name LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%'")
    fun searchClients(query: String): Flow<List<Client>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: Client): Long

    @Update
    suspend fun updateClient(client: Client)

    @Delete
    suspend fun deleteClient(client: Client)

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    suspend fun getClientById(id: Int): Client?
}

@Dao
interface StockItemDao {
    @Query("SELECT * FROM stock_items ORDER BY name ASC")
    fun getAllStockItems(): Flow<List<StockItem>>

    @Query("SELECT * FROM stock_items WHERE category = :category ORDER BY name ASC")
    fun getStockItemsByCategory(category: String): Flow<List<StockItem>>

    @Query("SELECT * FROM stock_items WHERE stockQuantity <= lowStockThreshold ORDER BY stockQuantity ASC")
    fun getLowStockItems(): Flow<List<StockItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockItem(item: StockItem): Long

    @Update
    suspend fun updateStockItem(item: StockItem)

    @Delete
    suspend fun deleteStockItem(item: StockItem)

    @Query("SELECT * FROM stock_items WHERE id = :id LIMIT 1")
    suspend fun getStockItemById(id: Int): StockItem?
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY invoiceDate DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE paymentStatus = :status ORDER BY invoiceDate DESC")
    fun getInvoicesByPaymentStatus(status: String): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE invoiceDate >= :startDate AND invoiceDate <= :endDate ORDER BY invoiceDate DESC")
    fun getInvoicesForDateRange(startDate: Long, endDate: Long): Flow<List<Invoice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Update
    suspend fun updateInvoice(invoice: Invoice)

    @Delete
    suspend fun deleteInvoice(invoice: Invoice)

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: Int): Invoice?

    @Query("SELECT MAX(id) FROM invoices")
    suspend fun getMaxInvoiceId(): Int?
}

@Dao
interface InvoiceItemDao {
    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    fun getItemsForInvoice(invoiceId: Int): Flow<List<InvoiceItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItem(item: InvoiceItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItem>)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteItemsForInvoice(invoiceId: Int)
}

@Dao
interface InventoryLogDao {
    @Query("SELECT * FROM inventory_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<InventoryLog>>

    @Query("SELECT * FROM inventory_logs WHERE itemId = :itemId ORDER BY timestamp DESC")
    fun getLogsForItem(itemId: Int): Flow<List<InventoryLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: InventoryLog): Long
}
