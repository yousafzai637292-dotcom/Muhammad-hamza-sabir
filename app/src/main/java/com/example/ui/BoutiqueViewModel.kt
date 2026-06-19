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
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
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

    // Dynamic Categories list states (Users can add or remove categories)
    private val _categories = MutableStateFlow<List<String>>(
        listOf("Cosmetics", "Shoes & Sandals", "Perfumes", "Children's Clothing")
    )
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    // Invoice Draft State (The Cart)
    private val _selectedClientForInvoice = MutableStateFlow<Client?>(null)
    val selectedClientForInvoice: StateFlow<Client?> = _selectedClientForInvoice.asStateFlow()

    // Maps stockItemId -> Quantity
    private val _invoiceCart = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val invoiceCart: StateFlow<Map<Int, Int>> = _invoiceCart.asStateFlow()

    // Maps stockItemId -> Billed S.P. (customizable)
    private val _cartCustomPrices = MutableStateFlow<Map<Int, Double>>(emptyMap())
    val cartCustomPrices: StateFlow<Map<Int, Double>> = _cartCustomPrices.asStateFlow()

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
        prefs.edit().putInt("invoice_layout_id", layoutId).apply()
    }

    // App Style Theme Mode (Light, Dark, Standard)
    private val _themeMode = MutableStateFlow("Standard")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode).apply()
    }

    // Selected SMS / WhatsApp Custom Template ID
    private val _smsTemplateId = MutableStateFlow(1) // Default to 1: Classic Elegance
    val smsTemplateId: StateFlow<Int> = _smsTemplateId.asStateFlow()

    fun selectSmsTemplate(templateId: Int) {
        _smsTemplateId.value = templateId
    }

    // Customizable Invoice Branding States
    private val _businessName = MutableStateFlow("Invoice & Inventory System")
    val businessName: StateFlow<String> = _businessName.asStateFlow()

    private val _businessLogoText = MutableStateFlow("BE")
    val businessLogoText: StateFlow<String> = _businessLogoText.asStateFlow()

    private val _businessPhone = MutableStateFlow("+92 300 1234567")
    val businessPhone: StateFlow<String> = _businessPhone.asStateFlow()

    private val _businessAddress = MutableStateFlow("One Business Suite, Suite 101")
    val businessAddress: StateFlow<String> = _businessAddress.asStateFlow()

    private val _customInvoiceNotes = MutableStateFlow("Thank you for shopping! Exchange within 7 days with original tag.")
    val customInvoiceNotes: StateFlow<String> = _customInvoiceNotes.asStateFlow()

    private val _invoiceDeliveryCharges = MutableStateFlow(0.0)
    val invoiceDeliveryCharges: StateFlow<Double> = _invoiceDeliveryCharges.asStateFlow()

    fun setInvoiceDeliveryCharges(charges: Double) {
        _invoiceDeliveryCharges.value = charges
    }

    private val _appLanguage = MutableStateFlow("English")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    fun setAppLanguage(lang: String) {
        _appLanguage.value = lang
        prefs.edit().putString("app_lang", lang).apply()
    }

    private val _selectedFontType = MutableStateFlow("Royal Serif")
    val selectedFontType: StateFlow<String> = _selectedFontType.asStateFlow()

    fun setSelectedFontType(font: String) {
        _selectedFontType.value = font
        prefs.edit().putString("app_font", font).apply()
    }

    private val _messagingMode = MutableStateFlow(0)
    val messagingMode: StateFlow<Int> = _messagingMode.asStateFlow()

    private val _whatsappApiUrl = MutableStateFlow("")
    val whatsappApiUrl: StateFlow<String> = _whatsappApiUrl.asStateFlow()

    private val _whatsappApiKey = MutableStateFlow("")
    val whatsappApiKey: StateFlow<String> = _whatsappApiKey.asStateFlow()

    private val _smsApiUrl = MutableStateFlow("")
    val smsApiUrl: StateFlow<String> = _smsApiUrl.asStateFlow()

    private val _smsApiKey = MutableStateFlow("")
    val smsApiKey: StateFlow<String> = _smsApiKey.asStateFlow()

    fun updateMessagingSettings(mode: Int, waUrl: String, waKey: String, smsUrl: String, smsKey: String) {
        _messagingMode.value = mode
        _whatsappApiUrl.value = waUrl
        _whatsappApiKey.value = waKey
        _smsApiUrl.value = smsUrl
        _smsApiKey.value = smsKey

        prefs.edit().apply {
            putInt("messaging_mode", mode)
            putString("wa_api_url", waUrl)
            putString("wa_api_key", waKey)
            putString("sms_api_url", smsUrl)
            putString("sms_api_key", smsKey)
            apply()
        }
    }

    fun addCategory(categoryName: String) {
        val trimmed = categoryName.trim()
        if (trimmed.isEmpty()) return
        val current = _categories.value.toMutableList()
        if (!current.contains(trimmed)) {
            current.add(trimmed)
            _categories.value = current
            prefs.edit().putString("custom_categories", current.joinToString(";;;")).apply()
        }
    }

    fun removeCategory(categoryName: String) {
        val current = _categories.value.toMutableList()
        if (current.contains(categoryName)) {
            current.remove(categoryName)
            _categories.value = current
            prefs.edit().putString("custom_categories", current.joinToString(";;;")).apply()
        }
    }

    private val _businessLogoUri = MutableStateFlow<String?>(null)
    val businessLogoUri: StateFlow<String?> = _businessLogoUri.asStateFlow()

    private val _showLogoOnInvoice = MutableStateFlow(true)
    val showLogoOnInvoice: StateFlow<Boolean> = _showLogoOnInvoice.asStateFlow()

    private val prefs = context.getSharedPreferences("boutique_prefs", Context.MODE_PRIVATE)

    init {
        _businessName.value = prefs.getString("biz_name", "Invoice & Inventory System") ?: "Invoice & Inventory System"
        _businessLogoText.value = prefs.getString("biz_logo_text", "BE") ?: "BE"
        _businessPhone.value = prefs.getString("biz_phone", "+92 300 1234567") ?: "+92 300 1234567"
        _businessAddress.value = prefs.getString("biz_address", "One Business Suite, Suite 101") ?: "One Business Suite, Suite 101"
        _customInvoiceNotes.value = prefs.getString("biz_notes", "Thank you for shopping! Exchange within 7 days with original tag.") ?: "Thank you for shopping! Exchange within 7 days with original tag."
        _showLogoOnInvoice.value = prefs.getBoolean("show_logo", true)
        _appLanguage.value = prefs.getString("app_lang", "English") ?: "English"
        _selectedFontType.value = prefs.getString("app_font", "Royal Serif") ?: "Royal Serif"
        _invoiceLayoutId.value = prefs.getInt("invoice_layout_id", 3)
        _themeMode.value = prefs.getString("theme_mode", "Standard") ?: "Standard"
        
        _messagingMode.value = prefs.getInt("messaging_mode", 0)
        _whatsappApiUrl.value = prefs.getString("wa_api_url", "https://api.sms-gateway.example/whatsapp/send") ?: "https://api.sms-gateway.example/whatsapp/send"
        _whatsappApiKey.value = prefs.getString("wa_api_key", "") ?: ""
        _smsApiUrl.value = prefs.getString("sms_api_url", "https://api.sms-gateway.example/sms/send") ?: "https://api.sms-gateway.example/sms/send"
        _smsApiKey.value = prefs.getString("sms_api_key", "") ?: ""
        
        val savedCategories = prefs.getString("custom_categories", null)
        if (savedCategories != null) {
            _categories.value = savedCategories.split(";;;").filter { it.isNotBlank() }
        } else {
            _categories.value = listOf("Cosmetics", "Shoes & Sandals", "Perfumes", "Children's Clothing")
        }
        
        val file = File(context.filesDir, "brand_logo.png")
        if (file.exists()) {
            _businessLogoUri.value = file.absolutePath
        }
    }

    fun uploadLogoImage(uri: Uri) {
        viewModelScope.launch {
            try {
                val file = File(context.filesDir, "brand_logo.png")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                _businessLogoUri.value = file.absolutePath
                _showLogoOnInvoice.value = true
                prefs.edit().putBoolean("show_logo", true).apply()
                Toast.makeText(context, "Logo uploaded successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to upload logo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun removeLogoImage() {
        val file = File(context.filesDir, "brand_logo.png")
        if (file.exists()) {
            file.delete()
        }
        _businessLogoUri.value = null
        _showLogoOnInvoice.value = false
        prefs.edit().putBoolean("show_logo", false).apply()
        Toast.makeText(context, "Logo removed!", Toast.LENGTH_SHORT).show()
    }

    fun setShowLogoOnInvoice(show: Boolean) {
        _showLogoOnInvoice.value = show
        prefs.edit().putBoolean("show_logo", show).apply()
    }

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

        prefs.edit().apply {
            putString("biz_name", name)
            putString("biz_logo_text", logoText)
            putString("biz_phone", phone)
            putString("biz_address", address)
            putString("biz_notes", notes)
            apply()
        }
        Toast.makeText(context, "Branding settings saved!", Toast.LENGTH_SHORT).show()
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
    fun addStockItem(name: String, category: String, price: Double, costPrice: Double, stock: Int, threshold: Int, sku: String, description: String) {
        viewModelScope.launch {
            repository.insertStockItem(StockItem(
                name = name,
                category = category,
                price = price,
                costPrice = costPrice,
                stockQuantity = stock,
                lowStockThreshold = threshold,
                sku = sku,
                description = description
            ))
        }
    }

    fun prepopulateDemoStocks() {
        viewModelScope.launch {
            val demoItems = listOf(
                StockItem(
                    name = "Velvet Matte Crimson Lipstick",
                    category = "Cosmetics",
                    price = 1200.0,
                    costPrice = 750.0,
                    stockQuantity = 25,
                    lowStockThreshold = 5,
                    sku = "COS-LIP-CRM",
                    description = "Charming high-pigmentation lipstick featuring a continuous 12h comfortable wear finish."
                ),
                StockItem(
                    name = "Rose Gold Glitter Powder",
                    category = "Cosmetics",
                    price = 1850.0,
                    costPrice = 1100.0,
                    stockQuantity = 4,
                    lowStockThreshold = 5,
                    sku = "COS-POW-RGD",
                    description = "Ultra-fine sparkling body dust and highlight powder with luxury silk base extracts."
                ),
                StockItem(
                    name = "Aqua Hydrating Matte Foundation",
                    category = "Cosmetics",
                    price = 2600.0,
                    costPrice = 1500.0,
                    stockQuantity = 3,
                    lowStockThreshold = 5,
                    sku = "COS-FND-AQM",
                    description = "Sweatproof and non-greasy full protection face finish, suitable for humid climates."
                ),
                StockItem(
                    name = "Stella Pearl Court Heels",
                    category = "Shoes & Sandals",
                    price = 4500.0,
                    costPrice = 2200.0,
                    stockQuantity = 12,
                    lowStockThreshold = 3,
                    sku = "SH-HLS-PRL",
                    description = "Charming 3-inch white silk wedding court pumps elegantly laden with hand-sewn beads."
                ),
                StockItem(
                    name = "Comfy Meadow Summer Slides",
                    category = "Shoes & Sandals",
                    price = 2200.0,
                    costPrice = 1200.0,
                    stockQuantity = 1,
                    lowStockThreshold = 3,
                    sku = "SH-SLD-MDS",
                    description = "Super lightweight slip-resistant rubber cushion memory soles in aesthetic lavender."
                ),
                StockItem(
                    name = "Nectar Bloom Cologne Mist",
                    category = "Perfumes",
                    price = 6800.0,
                    costPrice = 3800.0,
                    stockQuantity = 15,
                    lowStockThreshold = 4,
                    sku = "PER-MIST-NCT",
                    description = "Sweet delightful refreshing summer peony aroma infused with natural white orchid oil."
                ),
                StockItem(
                    name = "Imperial Oud Intense Eau de Parfum",
                    category = "Perfumes",
                    price = 13500.0,
                    costPrice = 7500.0,
                    stockQuantity = 5,
                    lowStockThreshold = 2,
                    sku = "PER-OUD-IMP",
                    description = "Premium high-grade Cambodian Oud oil accompanied by rich spiced sandalwood base notes."
                ),
                StockItem(
                    name = "Cotton Flower Frock (Ages 2-6)",
                    category = "Children's Clothing",
                    price = 1950.0,
                    costPrice = 900.0,
                    stockQuantity = 18,
                    lowStockThreshold = 5,
                    sku = "CLO-KID-FRK",
                    description = "Lovely breathable 100% organic cotton pastel yellow frock, absolute child skin-safe."
                ),
                StockItem(
                    name = "Charming Linen Suspenders Set",
                    category = "Children's Clothing",
                    price = 2400.0,
                    costPrice = 1250.0,
                    stockQuantity = 2,
                    lowStockThreshold = 4,
                    sku = "CLO-KID-SUP",
                    description = "Stylish beige linen shorts and soft cotton shirt combo designed with cute adjustable suspenders."
                )
            )
            for (item in demoItems) {
                // Ensure dynamic categories updated if user does not have default ones
                addCategory(item.category)
                repository.insertStockItem(item)
            }
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

    fun updateCartCustomPrice(itemId: Int, customPrice: Double) {
        val current = _cartCustomPrices.value.toMutableMap()
        current[itemId] = customPrice
        _cartCustomPrices.value = current
    }

    fun removeItemFromInvoice(itemId: Int) {
        val current = _invoiceCart.value.toMutableMap()
        current.remove(itemId)
        _invoiceCart.value = current
        
        val prices = _cartCustomPrices.value.toMutableMap()
        prices.remove(itemId)
        _cartCustomPrices.value = prices
    }

    fun clearInvoiceCart() {
        _invoiceCart.value = emptyMap()
        _cartCustomPrices.value = emptyMap()
        _selectedClientForInvoice.value = null
        _invoiceDiscount.value = 0.0
        _invoiceDeliveryCharges.value = 0.0
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
            var totalItemDiscounts = 0.0
            val invoiceItems = mutableListOf<InvoiceItem>()

            for ((itemId, qty) in cart) {
                val stockItem = itemsList.find { it.id == itemId }
                if (stockItem != null) {
                    val standardSP = stockItem.price
                    val billedSP = _cartCustomPrices.value[itemId] ?: standardSP
                    val discountPerUnitVal = (standardSP - billedSP).coerceAtLeast(0.0)
                    
                    val originalLineTotal = standardSP * qty
                    val billedLineTotal = billedSP * qty
                    
                    subtotal += originalLineTotal
                    totalItemDiscounts += (discountPerUnitVal * qty)
                    
                    invoiceItems.add(
                        InvoiceItem(
                            invoiceId = 0, // setup during insertion
                            itemId = itemId,
                            itemName = stockItem.name,
                            category = stockItem.category,
                            unitPrice = billedSP,
                            originalPrice = standardSP,
                            discountPerUnit = discountPerUnitVal,
                            quantity = qty,
                            totalPrice = billedLineTotal
                        )
                    )
                }
            }

            val globalDiscount = _invoiceDiscount.value
            val combinedDiscount = totalItemDiscounts + globalDiscount
            val finalSubtotalAfterDiscount = (subtotal - combinedDiscount).coerceAtLeast(0.0)
            val taxAmount = finalSubtotalAfterDiscount * (_invoiceTaxRate.value / 100.0)
            val dCharges = _invoiceDeliveryCharges.value
            val grandTotal = finalSubtotalAfterDiscount + taxAmount + dCharges

            val invoice = Invoice(
                invoiceNumber = "", // Generated sequentially in repository
                clientId = client.id,
                clientName = client.name,
                clientPhone = client.phoneNumber,
                subtotal = subtotal,
                discount = combinedDiscount,
                tax = taxAmount,
                totalAmount = grandTotal,
                paymentStatus = _selectedPaymentStatus.value,
                paymentMethod = _selectedPaymentMethod.value,
                businessName = _businessName.value,
                businessLogoText = _businessLogoText.value,
                businessPhone = _businessPhone.value,
                businessAddress = _businessAddress.value,
                notes = _customInvoiceNotes.value,
                deliveryCharges = dCharges
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

    fun cancelOrReverseInvoice(invoice: Invoice, reason: String) {
        viewModelScope.launch {
            try {
                // 1. Mark status as "CANCELED" and set cancellation reason
                val updatedInvoice = invoice.copy(
                    paymentStatus = "Canceled",
                    cancelReason = reason,
                    notes = "${invoice.notes} [CANCELED: $reason]"
                )
                repository.updateInvoiceEntire(updatedInvoice)

                // 2. Restore items block
                val items = repository.getInvoiceItems(invoice.id)
                for (item in items) {
                    val stockItem = allStockItems.value.find { it.id == item.itemId }
                    if (stockItem != null) {
                        val restoredStock = stockItem.copy(
                            stockQuantity = stockItem.stockQuantity + item.quantity
                        )
                        repository.updateStockItem(
                            restoredStock,
                            manualChangeAmount = item.quantity
                        )
                        // Log detailing recovery
                        repository.insertLog(
                            InventoryLog(
                                itemId = item.itemId,
                                itemName = item.itemName,
                                category = item.category,
                                actionType = "MANUAL_ADJUST",
                                quantityChanged = item.quantity,
                                reorderQuantity = restoredStock.stockQuantity,
                                details = "Reversal: Billed items returned from canceled invoice ${invoice.invoiceNumber}. Reason: $reason"
                            )
                        )
                    }
                }
                Toast.makeText(context, "Invoice ${invoice.invoiceNumber} Canceled & Stock Reversed! Reasons recoded.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error reversing invoice: ${e.message}", Toast.LENGTH_LONG).show()
            }
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

    fun sendWhatsAppMessage(invoiceNum: String, customPhone: String? = null) {
        val invoice = invoices.value.find { it.invoiceNumber == invoiceNum }
        if (invoice == null) return
        
        val msgText = composePrefilledMsg(invoice)
        val targetPhone = customPhone ?: invoice.clientPhone
        
        if (_messagingMode.value == 1) {
            sendDirectGatewayApi(
                url = _whatsappApiUrl.value,
                key = _whatsappApiKey.value,
                phone = targetPhone,
                message = msgText,
                type = "WhatsApp"
            )
        } else {
            triggerWhatsAppWaMeLink(targetPhone, msgText)
        }
    }

    fun sendSimSms(invoiceNum: String, customPhone: String? = null) {
        val invoice = invoices.value.find { it.invoiceNumber == invoiceNum }
        if (invoice == null) return

        val msg = composePrefilledMsg(invoice)
        val targetPhone = customPhone ?: invoice.clientPhone
        
        if (_messagingMode.value == 1) {
            sendDirectGatewayApi(
                url = _smsApiUrl.value,
                key = _smsApiKey.value,
                phone = targetPhone,
                message = msg,
                type = "SMS"
            )
        } else {
            try {
                val uri = Uri.parse("smsto:$targetPhone")
                val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                    putExtra("sms_body", msg)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open SMS text composer!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendDirectGatewayApi(url: String, key: String, phone: String, message: String, type: String) {
        if (url.isBlank()) {
            Toast.makeText(context, "Error: $type Gateway URL is empty. Please set it in Settings.", Toast.LENGTH_LONG).show()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val jsonObject = JSONObject().apply {
                    put("to", phone)
                    put("message", message)
                    put("apiKey", key)
                }
                
                val body = jsonObject.toString().toRequestBody(mediaType)
                
                val finalUrl = if (url.contains("{phone}") || url.contains("{message}")) {
                    url.replace("{phone}", Uri.encode(phone))
                       .replace("{message}", Uri.encode(message))
                       .replace("{key}", Uri.encode(key))
                } else {
                    url
                }

                val requestBuilder = Request.Builder().url(finalUrl)
                
                if (key.isNotBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer $key")
                }
                
                val request = if (url.contains("{phone}")) {
                    requestBuilder.get().build()
                } else {
                    requestBuilder.post(body).build()
                }

                client.newCall(request).execute().use { response ->
                    val isSuccessful = response.isSuccessful
                    val code = response.code
                    launch(Dispatchers.Main) {
                        if (isSuccessful) {
                            Toast.makeText(context, "✅ $type Gateway Alert Sent Successfully!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "❌ $type Gateway Failed: Code $code", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown Connection Error"
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "❌ Gateway Error: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }
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
                reportTitle = "Audit Report - All Registered business periods: $currentPeriod",
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
                - Bill Total: PKR 149.99
                - Payment Status: PAID
                - Payment Mode: Cash
                
                Shop Contact: $bPhone
                We have attached your printable PDF billing receipt!
                
                Invoice & Inventory System.
                """.trimIndent()
            }
            2 -> {
                """
                Dear valued client Samantha Parker,
                
                We are delighted to present your curated selection from $bName:
                - Collection Ref: $invoiceNum
                - Tailored Total: PKR 149.99
                - Receipt Status: PAID (Cash)
                
                Should you require further styling assistance, contact $bPhone.
                
                Vintage Premium Luxury.
                """.trimIndent()
            }
            3 -> {
                """
                $bName BILLING
                Ref: $invoiceNum
                Client: Samantha Parker
                Amount: PKR 149.99 (PAID)
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
                Grand Total: PKR 149.99 (PAID)
                
                Have any styling or custom design questions? Chat with our team at $bPhone!
                """.trimIndent()
            }
            else -> "Hello Samantha Parker, your luxury order from $bName is ready! Total: PKR 149.99."
        }
    }

    // Helper utilities
    private fun composePrefilledMsg(invoice: Invoice): String {
        val totalFormatted = String.format(Locale.US, "PKR %.2f", invoice.totalAmount)
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
                
                Invoice & Inventory System.
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
                
                Vintage Premium Luxury.
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
