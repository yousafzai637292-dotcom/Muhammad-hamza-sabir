package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class BoutiqueRepository(
    private val clientDao: ClientDao,
    private val stockItemDao: StockItemDao,
    private val invoiceDao: InvoiceDao,
    private val invoiceItemDao: InvoiceItemDao,
    private val inventoryLogDao: InventoryLogDao
) {
    // Clients
    val allClients: Flow<List<Client>> = clientDao.getAllClients()
    fun searchClients(query: String): Flow<List<Client>> = clientDao.searchClients(query)
    suspend fun insertClient(client: Client): Long = clientDao.insertClient(client)
    suspend fun updateClient(client: Client) = clientDao.updateClient(client)
    suspend fun deleteClient(client: Client) = clientDao.deleteClient(client)
    suspend fun getClientById(id: Int): Client? = clientDao.getClientById(id)

    // Stock Items
    val allStockItems: Flow<List<StockItem>> = stockItemDao.getAllStockItems()
    val lowStockItems: Flow<List<StockItem>> = stockItemDao.getLowStockItems()
    fun getStockItemsByCategory(category: String): Flow<List<StockItem>> = stockItemDao.getStockItemsByCategory(category)
    
    suspend fun insertStockItem(item: StockItem): Long {
        val id = stockItemDao.insertStockItem(item)
        inventoryLogDao.insertLog(
            InventoryLog(
                itemId = id.toInt(),
                itemName = item.name,
                category = item.category,
                actionType = "STOCK_INITIALIZED",
                quantityChanged = item.stockQuantity,
                reorderQuantity = item.stockQuantity,
                details = "Product added to system catalog: ${item.name}"
            )
        )
        return id
    }

    suspend fun updateStockItem(item: StockItem, manualChangeAmount: Int = 0) {
        val oldItem = stockItemDao.getStockItemById(item.id)
        stockItemDao.updateStockItem(item)
        if (manualChangeAmount != 0) {
            inventoryLogDao.insertLog(
                InventoryLog(
                    itemId = item.id,
                    itemName = item.name,
                    category = item.category,
                    actionType = if (manualChangeAmount > 0) "STOCK_ADDED" else "MANUAL_ADJUST",
                    quantityChanged = manualChangeAmount,
                    reorderQuantity = item.stockQuantity,
                    details = "Manual quantity adjustment. Previous: ${oldItem?.stockQuantity ?: 0}, New: ${item.stockQuantity}"
                )
            )
        }
    }

    suspend fun deleteStockItem(item: StockItem) = stockItemDao.deleteStockItem(item)
    suspend fun getStockItemById(id: Int): StockItem? = stockItemDao.getStockItemById(id)

    // Invoices
    val allInvoices: Flow<List<Invoice>> = invoiceDao.getAllInvoices()
    fun getInvoicesByPaymentStatus(status: String): Flow<List<Invoice>> = invoiceDao.getInvoicesByPaymentStatus(status)
    fun getItemsForInvoice(invoiceId: Int): Flow<List<InvoiceItem>> = invoiceItemDao.getItemsForInvoice(invoiceId)

    suspend fun getInvoiceById(id: Int): Invoice? = invoiceDao.getInvoiceById(id)
    suspend fun getInvoiceItems(invoiceId: Int): List<InvoiceItem> {
        return invoiceItemDao.getItemsForInvoice(invoiceId).first()
    }

    suspend fun insertInvoiceWithItems(
        invoice: Invoice,
        itemsList: List<InvoiceItem>
    ): Long {
        // 1. Generate unique sequential Invoice Number if empty
        val maxId = invoiceDao.getMaxInvoiceId() ?: 0
        val nextNum = maxId + 1001
        val finalInvoice = if (invoice.invoiceNumber.isEmpty()) {
            invoice.copy(invoiceNumber = "INV-${Calendar.getInstance().get(Calendar.YEAR)}-$nextNum")
        } else {
            invoice
        }

        // 2. Write Invoice
        val invoiceId = invoiceDao.insertInvoice(finalInvoice).toInt()

        // 3. Write Line items and deduct stock levels
        for (item in itemsList) {
            val lineItem = item.copy(invoiceId = invoiceId)
            invoiceItemDao.insertInvoiceItem(lineItem)

            // Deduct from StockItem inventory
            val stockItem = stockItemDao.getStockItemById(item.itemId)
            if (stockItem != null) {
                val newQty = (stockItem.stockQuantity - item.quantity).coerceAtLeast(0)
                val updatedStock = stockItem.copy(
                    stockQuantity = newQty,
                    lastUpdated = System.currentTimeMillis()
                )
                stockItemDao.updateStockItem(updatedStock)

                // Log this auto stock deduction
                inventoryLogDao.insertLog(
                    InventoryLog(
                        itemId = stockItem.id,
                        itemName = stockItem.name,
                        category = stockItem.category,
                        actionType = "SALE_AUTO_DEDUCT",
                        quantityChanged = -item.quantity,
                        reorderQuantity = newQty,
                        details = "Auto-deducted for Invoice ${finalInvoice.invoiceNumber}"
                    )
                )
            }
        }

        return invoiceId.toLong()
    }

    suspend fun updateInvoiceStatus(invoiceId: Int, newStatus: String) {
        val invoice = invoiceDao.getInvoiceById(invoiceId)
        if (invoice != null) {
            invoiceDao.updateInvoice(invoice.copy(paymentStatus = newStatus))
        }
    }

    suspend fun deleteInvoice(invoice: Invoice) {
        invoiceItemDao.deleteItemsForInvoice(invoice.id)
        invoiceDao.deleteInvoice(invoice)
    }

    // Reports & Filtering
    fun getInvoicesForRange(startDate: Long, endDate: Long): Flow<List<Invoice>> =
        invoiceDao.getInvoicesForDateRange(startDate, endDate)

    // Logs
    val allLogs: Flow<List<InventoryLog>> = inventoryLogDao.getAllLogs()
    fun getLogsForItem(itemId: Int): Flow<List<InventoryLog>> = inventoryLogDao.getLogsForItem(itemId)
}
