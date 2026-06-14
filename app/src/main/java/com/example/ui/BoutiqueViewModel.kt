package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BoutiqueViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication()

    private val database = BoutiqueDatabase.getDatabase(application, viewModelScope)
    private val repository = BoutiqueRepository(
        database.clientDao(),
        database.stockItemDao(),
        database.invoiceDao(),
        database.invoiceItemDao(),
        database.inventoryLogDao()
    )

    // UI Search Queries & Filter States
    private val _clientSearchQuery = MutableStateFlow("")
    val clientSearchQuery: StateFlow<String> = _clientSearchQuery.asStateFlow()

    private val _inventorySearchQuery = MutableStateFlow("")
    val inventorySearchQuery: StateFlow<String> = _inventorySearchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    // Database Flows
    val clients: StateFlow<List<Client>> = _clientSearchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allClients
            } else {
                repository.searchClients(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStockItems: StateFlow<List<StockItem>> = repository.allStockItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockItems: StateFlow<List<StockItem>> = repository.lowStockItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<Invoice>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventoryLogs: StateFlow<List<InventoryLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Invoice Draft State (The Cart)
    private val _selectedClientForInvoice = MutableStateFlow<Client?>(null)
    val selectedClientForInvoice: StateFlow<Client?> = _selectedClientForInvoice.asStateFlow()

    // Maps stockItemId -> Quantity
    private val _invoiceCart = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val invoiceCart: StateFlow<Map<Int, Int>> = _invoiceCart.asStateFlow()

    private val _invoiceDiscount = MutableStateFlow(0.0)
    val invoiceDiscount: StateFlow<Double> = _invoiceDiscount.asStateFlow()

    private val _invoiceTaxRate = MutableStateFlow(8.0) // 8% default VAT/Sales tax
    val invoiceTaxRate: StateFlow<Double> = _invoiceTaxRate.asStateFlow()

    private val _selectedPaymentStatus = MutableStateFlow("Paid") // Paid, Unpaid, Partial
    val selectedPaymentStatus: StateFlow<String> = _selectedPaymentStatus.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow("Cash") // Cash, Card, Online, WhatsApp Pay
    val selectedPaymentMethod: StateFlow<String> = _selectedPaymentMethod.asStateFlow()

    // Selected Invoice Design Layout id (1 to 12)
    private val _invoiceLayoutId = MutableStateFlow(3) // Default to 3: Royal Burgundy
    val invoiceLayoutId: StateFlow<Int> = _invoiceLayoutId.asStateFlow()

    fun selectInvoiceLayout(layoutId: Int) {
        _invoiceLayoutId.value = layoutId
    }

    // Selected SMS / WhatsApp Custom Template ID
    private val _smsTemplateId = MutableStateFlow(1) // Default to 1: Classic Elegance
    val smsTemplateId: StateFlow<Int> = _smsTemplateId.asStateFlow()

    fun selectSmsTemplate(templateId: Int) {
        _smsTemplateId.value = templateId
    }

    // Customizable Invoice Branding States
    private val _businessName = MutableStateFlow("Boutique Elegance")
    val businessName: StateFlow<String> = _businessName.asStateFlow()

    private val _businessLogoText = MutableStateFlow("BE")
    val businessLogoText: StateFlow<String> = _businessLogoText.asStateFlow()

    private val _businessPhone = MutableStateFlow("+1 555-867-5309")
    val businessPhone: StateFlow<String> = _businessPhone.asStateFlow()

    private val _businessAddress = MutableStateFlow("101 Luxury Mall, Boutique St.")
    val businessAddress: StateFlow<String> = _businessAddress.asStateFlow()

    private val _customInvoiceNotes = MutableStateFlow("Thank you for shopping! Exchange within 7 days with original tag.")
    val customInvoiceNotes: StateFlow<String> = _customInvoiceNotes.asStateFlow()

    // Action Methods
    fun updateClientSearch(query: String) { _clientSearchQuery.value = query }
    fun updateInventorySearch(query: String) { _inventorySearchQuery.value = query }
    fun updateCategoryFilter(category: String) { _selectedCategoryFilter.value = category }

    fun updateBranding(name: String, logoText: String, phone: String, address: String, notes: String) {
        _businessName.value = name
        _businessLogoText.value = logoText
        _businessPhone.value = phone
        _businessAddress.value = address
        _customInvoiceNotes.value = notes
    }

    // Client Management
    fun addClient(name: String, phone: String, email: String, address: String) {
        viewModelScope.launch {
            repository.insertClient(Client(name = name, phoneNumber = phone, email = email, address = address))
        }
    }

    fun updateClient(client: Client) {
        viewModelScope.launch {
            repository.updateClient(client)
        }
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            repository.deleteClient(client)
        }
    }

    // Inventory Management
    fun addStockItem(name: String, category: String, price: Double, stock: Int, threshold: Int, sku: String, description: String) {
        viewModelScope.launch {
            repository.insertStockItem(StockItem(
                name = name,
                category = category,
                price = price,
                stockQuantity = stock,
                lowStockThreshold = threshold,
                sku = sku,
                description = description
            ))
        }
    }

    fun updateStockItem(item: StockItem, manualChangeAmount: Int = 0) {
        viewModelScope.launch {
            repository.updateStockItem(item, manualChangeAmount)
        }
    }

    fun deleteStockItem(item: StockItem) {
        viewModelScope.launch {
            repository.deleteStockItem(item)
        }
    }

    // Invoice Draft Methods
    fun setClientForInvoice(client: Client?) {
        _selectedClientForInvoice.value = client
    }

    fun addItemToInvoice(itemId: Int, maxQuant: Int) {
        val current = _invoiceCart.value.toMutableMap()
        val currentQty = current[itemId] ?: 0
        if (currentQty < maxQuant) {
            current[itemId] = currentQty + 1
            _invoiceCart.value = current
        } else {
            Toast.makeText(context, "Cannot exceed available stock!", Toast.LENGTH_SHORT).show()
        }
    }

    fun subtractItemFromInvoice(itemId: Int) {
        val current = _invoiceCart.value.toMutableMap()
        val currentQty = current[itemId] ?: 0
        if (currentQty <= 1) {
            current.remove(itemId)
        } else {
            current[itemId] = currentQty - 1
        }
        _invoiceCart.value = current
    }

    fun removeItemFromInvoice(itemId: Int) {
        val current = _invoiceCart.value.toMutableMap()
        current.remove(itemId)
        _invoiceCart.value = current
    }

    fun clearInvoiceCart() {
        _invoiceCart.value = emptyMap()
        _selectedClientForInvoice.value = null
        _invoiceDiscount.value = 0.0
    }

    fun updateInvoiceFees(discount: Double, tax: Double) {
        _invoiceDiscount.value = discount
        _invoiceTaxRate.value = tax
    }

    fun setPaymentStatus(status: String) { _selectedPaymentStatus.value = status }
    fun setPaymentMethod(method: String) { _selectedPaymentMethod.value = method }

    // Save Draft Invoice & Create Records
    fun createInvoice(onSuccess: (String) -> Unit) {
        val client = _selectedClientForInvoice.value
        val cart = _invoiceCart.value
        val itemsList = allStockItems.value

        if (client == null) {
            Toast.makeText(context, "Please select or add a client first!", Toast.LENGTH_SHORT).show()
            return
        }
        if (cart.isEmpty()) {
            Toast.makeText(context, "Cart is empty. Select products to bill!", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            var subtotal = 0.0
            val invoiceItems = mutableListOf<InvoiceItem>()

            for ((itemId, qty) in cart) {
                val stockItem = itemsList.find { it.id == itemId }
                if (stockItem != null) {
                    val lineTotal = stockItem.price * qty
                    subtotal += lineTotal
                    invoiceItems.add(
                        InvoiceItem(
                            invoiceId = 0, // setup during insertion
                            itemId = itemId,
                            itemName = stockItem.name,
                            category = stockItem.category,
                            unitPrice = stockItem.price,
                            quantity = qty,
                            totalPrice = lineTotal
                        )
                    )
                }
            }

            val discount = _invoiceDiscount.value
            val taxAmount = (subtotal - discount).coerceAtLeast(0.0) * (_invoiceTaxRate.value / 100.0)
            val grandTotal = (subtotal - discount).coerceAtLeast(0.0) + taxAmount

            val invoice = Invoice(
                invoiceNumber = "", // Generated sequentially in repository
                clientId = client.id,
                clientName = client.name,
                clientPhone = client.phoneNumber,
                subtotal = subtotal,
                discount = discount,
                tax = taxAmount,
                totalAmount = grandTotal,
                paymentStatus = _selectedPaymentStatus.value,
                paymentMethod = _selectedPaymentMethod.value,
                businessName = _businessName.value,
                businessLogoText = _businessLogoText.value,
                businessPhone = _businessPhone.value,
                businessAddress = _businessAddress.value,
                notes = _customInvoiceNotes.value
            )

            val invoiceId = repository.insertInvoiceWithItems(invoice, invoiceItems)
            val savedInvoice = repository.getInvoiceById(invoiceId.toInt())
            
            clearInvoiceCart()
            if (savedInvoice != null) {
                onSuccess(savedInvoice.invoiceNumber)
            } else {
                onSuccess("INV-${invoiceId}")
            }
        }
    }

    fun updateInvoicePaymentStatus(invoiceId: Int, newStatus: String) {
        viewModelScope.launch {
            repository.updateInvoiceStatus(invoiceId, newStatus)
        }
    }

    fun deleteInvoice(invoice: Invoice) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
        }
    }

    // Direct Integration Intents
    fun shareInvoicePdf(invoiceNum: String, onInvoiceShared: (Intent) -> Unit) {
        val invoice = invoices.value.find { it.invoiceNumber == invoiceNum }
        if (invoice == null) {
            Toast.makeText(context, "Invoice not found in database!", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            val items = repository.getInvoiceItems(invoice.id)
            val file = PdfGenerator.generateInvoicePdf(context, invoice, items, _invoiceLayoutId.value)
            if (file != null) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Invoice ${invoice.invoiceNumber} from ${invoice.businessName}")
                    putExtra(Intent.EXTRA_TEXT, composePrefilledMsg(invoice))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                onInvoiceShared(intent)
            } else {
                Toast.makeText(context, "Could not generate invoice PDF!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun downloadInvoicePdfLocally(invoiceNum: String) {
        val invoice = invoices.value.find { it.invoiceNumber == invoiceNum } ?: return
        viewModelScope.launch {
            val items = repository.getInvoiceItems(invoice.id)
            val file = PdfGenerator.generateInvoicePdf(context, invoice, items, _invoiceLayoutId.value)
            if (file != null) {
                Toast.makeText(context, "Invoice downloaded to standard documents storage:\n${file.absolutePath}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Download failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Dedicated function to interface with browser's wa.me web launcher links cleanly
    fun triggerWhatsAppWaMeLink(phone: String, text: String) {
        val formattedPhone = formatPhoneNumberForWhatsApp(phone)
        val encodedText = Uri.encode(text)
        val waMeUrl = "https://wa.me/$formattedPhone?text=$encodedText"
        try {
            val uri = Uri.parse(waMeUrl)
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not launch modern wa.me routing link!", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendWhatsAppMessage(invoiceNum: String) {
        val invoice = invoices.value.find { it.invoiceNumber == invoiceNum }
        if (invoice == null) return
        
        val msgText = composePrefilledMsg(invoice)
        triggerWhatsAppWaMeLink(invoice.clientPhone, msgText)
    }

    fun sendSimSms(invoiceNum: String) {
        val invoice = invoices.value.find { it.invoiceNumber == invoiceNum }
        if (invoice == null) return

        val msg = composePrefilledMsg(invoice)
        try {
            val uri = Uri.parse("smsto:${invoice.clientPhone}")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", msg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open SMS text composer!", Toast.LENGTH_SHORT).show()
        }
    }

    // Monthly Reports Compiler
    fun downloadMonthlyReportPdf() {
        val currentPeriod = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            val invoiceList = invoices.value

            var sumTotal = 0.0
            var paidCount = 0
            var unpaidCount = 0

            val catSplits = mutableMapOf(
                "Cosmetics" to 0.0,
                "Shoes & Sandals" to 0.0,
                "Perfumes" to 0.0,
                "Children's Clothing" to 0.0
            )

            for (inv in invoiceList) {
                sumTotal += inv.totalAmount
                if (inv.paymentStatus.lowercase() == "paid") paidCount++ else unpaidCount++

                // fetch line item sales for this month list
                val items = repository.getInvoiceItems(inv.id)
                for (it in items) {
                    val cat = it.category
                    val currentVal = catSplits[cat] ?: 0.0
                    catSplits[cat] = currentVal + it.totalPrice
                }
            }

            val file = PdfGenerator.generateReportPdf(
                context = context,
                reportTitle = "Audit Report - All Registered boutique periods: $currentPeriod",
                totalSales = sumTotal,
                paidInvoicesCount = paidCount,
                unpaidInvoicesCount = unpaidCount,
                totalInvoicesCount = invoiceList.size,
                categorySplits = catSplits,
                invoices = invoiceList
            )

            if (file != null) {
                Toast.makeText(context, "Custom monthly report saved and downloaded:\n${file.absolutePath}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Report generation failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Public live templates preview generator for UI
    fun getSmsTemplatePreview(templateId: Int, invoiceNum: String = "BE-2026-0001"): String {
        val bName = _businessName.value
        val bPhone = _businessPhone.value
        return when (templateId) {
            1 -> {
                """
                Hello Samantha Parker,
                
                Thank you for shopping with us! Here are your order bill details:
                - Business: $bName
                - Invoice Ref: $invoiceNum
                - Bill Total: ${'$'}149.99
                - Payment Status: PAID
                - Payment Mode: Cash
                
                Shop Contact: $bPhone
                We have attached your printable PDF billing receipt!
                
                Boutique Elegance.
                """.trimIndent()
            }
            2 -> {
                """
                Dear valued client Samantha Parker,
                
                We are delighted to present your curated selection from $bName:
                - Collection Ref: $invoiceNum
                - Tailored Total: ${'$'}149.99
                - Receipt Status: PAID (Cash)
                
                Should you require further styling assistance, contact $bPhone.
                
                Vintage Boutique Luxury.
                """.trimIndent()
            }
            3 -> {
                """
                $bName BILLING
                Ref: $invoiceNum
                Client: Samantha Parker
                Amount: ${'$'}149.99 (PAID)
                Mode: Cash
                Support: $bPhone
                Thank you!
                """.trimIndent()
            }
            4 -> {
                """
                Hi Samantha Parker!
                
                Your stylish selection from $bName is official!
                Order Ref: $invoiceNum
                Grand Total: ${'$'}149.99 (PAID)
                
                Have any styling or custom design questions? Chat with our team at $bPhone!
                """.trimIndent()
            }
            else -> "Hello Samantha Parker, your luxury order from $bName is ready! Total: ${'$'}149.99."
        }
    }

    // Helper utilities
    private fun composePrefilledMsg(invoice: Invoice): String {
        val totalFormatted = String.format(Locale.US, "$%.2f", invoice.totalAmount)
        return when (_smsTemplateId.value) {
            1 -> {
                """
                Hello ${invoice.clientName},
                
                Thank you for shopping with us! Here are your order bill details:
                - Business: ${invoice.businessName}
                - Invoice Ref: ${invoice.invoiceNumber}
                - Bill Total: $totalFormatted
                - Payment Status: ${invoice.paymentStatus.uppercase()}
                - Payment Mode: ${invoice.paymentMethod}
                
                Shop Contact: ${invoice.businessPhone}
                We have attached your printable PDF billing receipt!
                
                Boutique Elegance.
                """.trimIndent()
            }
            2 -> {
                """
                Dear valued client ${invoice.clientName},
                
                We are delighted to present your curated selection from ${invoice.businessName}:
                - Collection Ref: ${invoice.invoiceNumber}
                - Tailored Total: $totalFormatted
                - Receipt Status: ${invoice.paymentStatus.uppercase()} (${invoice.paymentMethod})
                
                Should you require further styling assistance, contact ${invoice.businessPhone}.
                
                Vintage Boutique Luxury.
                """.trimIndent()
            }
            3 -> {
                """
                ${invoice.businessName} BILLING
                Ref: ${invoice.invoiceNumber}
                Client: ${invoice.clientName}
                Amount: $totalFormatted (${invoice.paymentStatus.uppercase()})
                Mode: ${invoice.paymentMethod}
                Support: ${invoice.businessPhone}
                Thank you!
                """.trimIndent()
            }
            4 -> {
                """
                Hi ${invoice.clientName}!
                
                Your stylish selection from ${invoice.businessName} is official!
                Order Ref: ${invoice.invoiceNumber}
                Grand Total: $totalFormatted (${invoice.paymentStatus.uppercase()})
                
                Have any styling or custom design questions? Chat with our team at ${invoice.businessPhone}!
                """.trimIndent()
            }
            else -> {
                "Hello ${invoice.clientName}, your luxury order from ${invoice.businessName} is ready! Total: $totalFormatted."
            }
        }
    }

    private fun formatPhoneNumberForWhatsApp(phone: String): String {
        // Strip out non-numeric characters except maybe leading plus
        val cleaned = phone.replace(Regex("[^0-9+]"), "")
        if (cleaned.startsWith("+")) {
            return cleaned.substring(1)
        }
        // If it does not start with a country code, you may append default prefix
        return cleaned
    }
}
