package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Client
import com.example.data.Invoice
import com.example.data.StockItem
import com.example.data.InventoryLog
import com.example.data.InvoiceItem
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// Compatibility aliases for Compose colors
private val Color.Companion.WHITE: Color get() = Color.White
private val Color.Companion.BLACK: Color get() = Color.Black
private val Color.Companion.GRAY: Color get() = Color.Gray

fun getThemePreviewColors(id: Int): Pair<Color, Color> {
    return when (id) {
        1 -> Pair(Color(0xFF1B4D3E), Color(0xFF4C7B6B))
        2 -> Pair(Color(0xFF8E4162), Color(0xFFC38D9E))
        3 -> Pair(Color(0xFF7A2048), Color(0xFFB58A30))
        4 -> Pair(Color(0xFF1E293B), Color(0xFF64748B))
        5 -> Pair(Color(0xFF111111), Color(0xFFC5A059))
        6 -> Pair(Color(0xFF000000), Color(0xFF555555))
        7 -> Pair(Color(0xFF2E5A44), Color(0xFF4CA873))
        8 -> Pair(Color(0xFFD35400), Color(0xFFE67E22))
        9 -> Pair(Color(0xFF1A365D), Color(0xFF319795))
        10 -> Pair(Color(0xFF7E4E30), Color(0xFFD4A373))
        11 -> Pair(Color(0xFF5F4B8B), Color(0xFFA29BFE))
        12 -> Pair(Color(0xFFE0115F), Color(0xFF00F5FF))
        else -> Pair(Color(0xFF7A2048), Color(0xFFB58A30))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoutiqueAppScreen(viewModel: BoutiqueViewModel) {
    var activeTab by remember { mutableStateOf(0) } // 0: Billing, 1: Inventory, 2: Customers, 3: Reports & Settings, 4: Audit Logs

    // Observe data variables
    val clients by viewModel.clients.collectAsStateWithLifecycle()
    val stockItems by viewModel.allStockItems.collectAsStateWithLifecycle()
    val lowStockItems by viewModel.lowStockItems.collectAsStateWithLifecycle()
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()
    val inventoryLogs by viewModel.inventoryLogs.collectAsStateWithLifecycle()

    // Invoice completed dialog control
    var completeInvoiceNum by remember { mutableStateOf<String?>(null) }
    var showInvoiceCompleteDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                val logoText by viewModel.businessLogoText.collectAsStateWithLifecycle()
                                Text(
                                    text = logoText,
                                    color = Color.WHITE,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        val shopName by viewModel.businessName.collectAsStateWithLifecycle()
                        Column {
                            Text(
                                text = shopName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Boutique Retail Suite",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Billing") },
                    label = { Text("Billing", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_billing")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = {
                        BadgedBox(badge = {
                            if (lowStockItems.isNotEmpty()) {
                                Badge(containerColor = LowStockAlertColor) {
                                    Text(lowStockItems.size.toString(), color = Color.WHITE)
                                }
                            }
                        }) {
                            Icon(Icons.Default.List, contentDescription = "Inventory")
                        }
                    },
                    label = { Text("Stock", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_stock")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Customers") },
                    label = { Text("CRM", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_crm")
                )
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") },
                    label = { Text("Reports", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_reports")
                )
                NavigationBarItem(
                    selected = activeTab == 4,
                    onClick = { activeTab = 4 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Logs") },
                    label = { Text("Logs", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_logs")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (activeTab) {
                0 -> BillingCoordinatorScreen(
                    viewModel = viewModel,
                    clients = clients,
                    products = stockItems,
                    onBillingComplete = { num ->
                        completeInvoiceNum = num
                        showInvoiceCompleteDialog = true
                    }
                )
                1 -> InventoryScreen(viewModel = viewModel, products = stockItems, lowStock = lowStockItems)
                2 -> CustomerCrmScreen(viewModel = viewModel, clients = clients, invoices = invoices)
                3 -> ReportsAndBrandingScreen(viewModel = viewModel, invoices = invoices)
                4 -> StockLogsScreen(logs = inventoryLogs)
            }

            // SUCCESS DIALOG after checkout completes
            if (showInvoiceCompleteDialog && completeInvoiceNum != null) {
                InvoiceCreatedSuccessDialog(
                    invoiceNum = completeInvoiceNum!!,
                    viewModel = viewModel,
                    onDismiss = {
                        showInvoiceCompleteDialog = false
                        completeInvoiceNum = null
                    }
                )
            }
        }
    }
}

// ==========================================
// 1. BILLING / SALES SCREEN
// ==========================================
@Composable
fun BillingCoordinatorScreen(
    viewModel: BoutiqueViewModel,
    clients: List<Client>,
    products: List<StockItem>,
    onBillingComplete: (String) -> Unit
) {
    val selectedClient by viewModel.selectedClientForInvoice.collectAsStateWithLifecycle()
    val cart by viewModel.invoiceCart.collectAsStateWithLifecycle()
    val discount by viewModel.invoiceDiscount.collectAsStateWithLifecycle()
    val taxRate by viewModel.invoiceTaxRate.collectAsStateWithLifecycle()
    val paymentStatus by viewModel.selectedPaymentStatus.collectAsStateWithLifecycle()
    val paymentMethod by viewModel.selectedPaymentMethod.collectAsStateWithLifecycle()

    var showClientDialog by remember { mutableStateOf(false) }
    var selectedCategoryTab by remember { mutableStateOf("All") }

    val categories = listOf("All", "Cosmetics", "Shoes & Sandals", "Perfumes", "Children's Clothing")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // STEP 1: Select Customer Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. CUSTOMER PROFILE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (selectedClient == null) {
                        Button(
                            onClick = { showClientDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("select_client_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Add Client")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select or Create Customer Profile")
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = selectedClient!!.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Phone: " + selectedClient!!.phoneNumber,
                                    fontSize = 13.sp,
                                    color = Color.GRAY
                                )
                                if (selectedClient!!.address.isNotEmpty()) {
                                    Text(
                                        text = "Address: " + selectedClient!!.address,
                                        fontSize = 11.sp,
                                        color = Color.GRAY
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.setClientForInvoice(null) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Clear", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        // STEP 2: Catalog Products Picker
        item {
            Text(
                text = "2. ADD BOUTIQUE ITEMS TO BILL",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Category Tabs Scroll
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategoryTab).coerceAtLeast(0),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                categories.forEach { cat ->
                    Tab(
                        selected = selectedCategoryTab == cat,
                        onClick = { selectedCategoryTab = cat },
                        text = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }
        }

        // Filter products and display
        val filteredProducts = products.filter {
            selectedCategoryTab == "All" || it.category == selectedCategoryTab
        }

        if (filteredProducts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No boutique stock available in $selectedCategoryTab", color = Color.GRAY)
                }
            }
        } else {
            items(filteredProducts) { item ->
                val quantityInCart = cart[item.id] ?: 0
                val availableAfterCart = (item.stockQuantity - quantityInCart).coerceAtLeast(0)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.category,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (item.sku.isNotEmpty()) {
                                    Text(
                                        text = "SKU: ${item.sku}",
                                        fontSize = 10.sp,
                                        color = Color.GRAY
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "$%.2f", item.price),
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 15.sp
                            )
                        }

                        // Stock Badge & Add Controls
                        Column(horizontalAlignment = Alignment.End) {
                            val badgeColor = if (availableAfterCart <= item.lowStockThreshold) LowStockAlertColor else PaymentPaidColor
                            Surface(
                                color = badgeColor.copy(alpha = 0.1f),
                                modifier = Modifier.clip(RoundedCornerShape(4.dp))
                            ) {
                                Text(
                                    text = "$availableAfterCart in stock",
                                    color = badgeColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (quantityInCart > 0) {
                                    IconButton(
                                        onClick = { viewModel.subtractItemFromInvoice(item.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.RemoveCircle, contentDescription = "Sub", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    Text(
                                        text = quantityInCart.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.addItemToInvoice(item.id, item.stockQuantity) },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("add_item_${item.id}"),
                                    enabled = availableAfterCart > 0
                                ) {
                                    Icon(
                                        Icons.Default.AddCircle,
                                        contentDescription = "Add",
                                        tint = if (availableAfterCart > 0) MaterialTheme.colorScheme.primary else Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // STEP 3: Current Bill calculations
        if (cart.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "3. INVOICE ORDER DETAILS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Calculations panel
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Subtotal
                        var cartSubtotal = 0.0
                        cart.forEach { (id, qty) ->
                            val prod = products.find { it.id == id }
                            if (prod != null) {
                                cartSubtotal += prod.price * qty
                            }
                        }

                        // Inputs for Discounts & Tax
                        var discountInput by remember { mutableStateOf(discount.toString()) }
                        var taxInput by remember { mutableStateOf(taxRate.toString()) }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = discountInput,
                                onValueChange = {
                                    discountInput = it
                                    val disc = it.toDoubleOrNull() ?: 0.0
                                    viewModel.updateInvoiceFees(disc, taxRate)
                                },
                                label = { Text("Discount ($)", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = taxInput,
                                onValueChange = {
                                    taxInput = it
                                    val tax = it.toDoubleOrNull() ?: 0.0
                                    viewModel.updateInvoiceFees(discount, tax)
                                },
                                label = { Text("Sales Tax % (GST)", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val calculatedTax = (cartSubtotal - discount).coerceAtLeast(0.0) * (taxRate / 100.0)
                        val finalTotal = (cartSubtotal - discount).coerceAtLeast(0.0) + calculatedTax

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal:", fontSize = 13.sp, color = Color.GRAY)
                            Text(String.format(Locale.getDefault(), "$%.2f", cartSubtotal), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Discount:", fontSize = 13.sp, color = Color.GRAY)
                            Text(String.format(Locale.getDefault(), "-$%.2f", discount), fontSize = 13.sp, color = LowStockAlertColor)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tax (GST):", fontSize = 13.sp, color = Color.GRAY)
                            Text(String.format(Locale.getDefault(), "+$%.2f", calculatedTax), fontSize = 13.sp)
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("GRAND TOTAL:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(String.format(Locale.getDefault(), "$%.2f", finalTotal), fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }

                        // STEP 4: Live Payment Status Selecting
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(text = "LIVE PAYMENT STATUS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.GRAY)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Paid", "Unpaid", "Partial").forEach { status ->
                                val selected = paymentStatus == status
                                Button(
                                    onClick = { viewModel.setPaymentStatus(status) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(status, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // STEP 5: Payment method
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "PAYMENT METHOD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.GRAY)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Cash", "Card", "Online", "WhatsApp Pay").forEach { method ->
                                val selected = paymentMethod == method
                                Button(
                                    onClick = { viewModel.setPaymentMethod(method) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(method, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // Checkout Button
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                viewModel.createInvoice { invNum ->
                                    onBillingComplete(invNum)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("save_invoice_btn"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = "Checkout")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SAVE & COMPILE BILL RECEIPT", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Client picker dialogue
    if (showClientDialog) {
        Dialog(onDismissRequest = { showClientDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
            ) {
                var searchQ by remember { mutableStateOf("") }
                var showAddClientForm by remember { mutableStateOf(false) }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select customer Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (!showAddClientForm) {
                        OutlinedTextField(
                            value = searchQ,
                            onValueChange = {
                                searchQ = it
                                viewModel.updateClientSearch(it)
                            },
                            placeholder = { Text("Search clients by name or phone") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Scroll list of clients
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (clients.isEmpty()) {
                                item {
                                    Text("No customers found.", color = Color.GRAY, modifier = Modifier.padding(8.dp))
                                }
                            } else {
                                items(clients) { client ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.setClientForInvoice(client)
                                                showClientDialog = false
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(client.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(client.phoneNumber, fontSize = 12.sp, color = Color.GRAY)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { showAddClientForm = true },
                            modifier = Modifier.fillMaxWidth().testTag("add_crm_client_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create New Client Profile")
                        }
                    } else {
                        // Quick Add Client Form
                        var name by remember { mutableStateOf("") }
                        var phone by remember { mutableStateOf("") }
                        var email by remember { mutableStateOf("") }
                        var address by remember { mutableStateOf("") }

                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Client Full Name") }, modifier = Modifier.fillMaxWidth().testTag("client_name_input"))
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Primary Phone Number") }, modifier = Modifier.fillMaxWidth().testTag("client_phone_input"))
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email (Optional)") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Boutique Address (Optional)") }, modifier = Modifier.fillMaxWidth())

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { showAddClientForm = false }, modifier = Modifier.weight(1f)) {
                                Text("Back")
                            }
                            Button(
                                onClick = {
                                    if (name.isNotBlank() && phone.isNotBlank()) {
                                        viewModel.addClient(name, phone, email, address)
                                        showAddClientForm = false
                                        searchQ = ""
                                        viewModel.updateClientSearch("")
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("save_quick_client_btn"),
                                enabled = name.isNotBlank() && phone.isNotBlank()
                            ) {
                                Text("Register")
                            }
                        }
                    }
                }
            }
        }
    }
}

// SUCCESS DIALOG WITH PDF & CHANNELS SHARING
@Composable
fun InvoiceCreatedSuccessDialog(
    invoiceNum: String,
    viewModel: BoutiqueViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = PaymentPaidColor,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Invoice Created Successfully!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Sequential bill code: $invoiceNum",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.GRAY
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Deducted original stock levels & tracked payment status. Send directly to customers using native integrations:",
                    fontSize = 11.sp,
                    color = Color.GRAY,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons for sharing channels
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // WhatsApp
                    Button(
                        onClick = { viewModel.sendWhatsAppMessage(invoiceNum) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        modifier = Modifier.fillMaxWidth().testTag("whatsapp_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "WhatsApp", tint = Color.WHITE)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send via WhatsApp Direct", color = Color.WHITE, fontWeight = FontWeight.Bold)
                    }

                    // offline SIM SMS
                    Button(
                        onClick = { viewModel.sendSimSms(invoiceNum) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().testTag("sms_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "SMS", tint = Color.WHITE)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Direct Send Offline SIM SMS", color = Color.WHITE, fontWeight = FontWeight.Bold)
                    }

                    // Share PDF
                    OutlinedButton(
                        onClick = {
                            viewModel.shareInvoicePdf(invoiceNum) { shareIntent ->
                                val chooser = Intent.createChooser(shareIntent, "Share Invoice PDF Receipt")
                                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(chooser)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("share_pdf_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Document", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Printable PDF File")
                    }

                    // Download PDF
                    OutlinedButton(
                        onClick = { viewModel.downloadInvoicePdfLocally(invoiceNum) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download Receipt", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download PDF Receipt Locally")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.testTag("dismiss_success_btn")) {
                    Text("Done & Done", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// 2. INVENTORY CONTROL & ALERTS SCREEN
// ==========================================
@Composable
fun InventoryScreen(
    viewModel: BoutiqueViewModel,
    products: List<StockItem>,
    lowStock: List<StockItem>
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("All") }
    var showAddProductDialog by remember { mutableStateOf(false) }

    val filterCategories = listOf("All", "Cosmetics", "Shoes & Sandals", "Perfumes", "Children's Clothing")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // TOP LOW STOCK HIGHLIGHT ALERTS CARD
        if (lowStock.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = LowStockAlertColor.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, LowStockAlertColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Low Stock Alert", tint = LowStockAlertColor, modifier = Modifier.size(34.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("AUTOMATIC LOW STOCK WARNING!", color = LowStockAlertColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            text = "${lowStock.size} boutique products require immediate restock replenishment.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        // Search and Add controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search stocks...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
            Button(
                onClick = { showAddProductDialog = true },
                modifier = Modifier.testTag("add_product_btn"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Catalog")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Categories Scroll
        ScrollableTabRow(
            selectedTabIndex = filterCategories.indexOf(selectedCat).coerceAtLeast(0),
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            filterCategories.forEach { cat ->
                Tab(
                    selected = selectedCat == cat,
                    onClick = { selectedCat = cat },
                    text = { Text(cat, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Grid listing
        val filtered = products.filter {
            (selectedCat == "All" || it.category == selectedCat) &&
                    (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.sku.contains(searchQuery, ignoreCase = true))
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No inventory item matches search criteria.", color = Color.GRAY)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { item ->
                    InventoryItemCard(item = item, viewModel = viewModel)
                }
            }
        }
    }

    // Dialogue to Add stock item
    if (showAddProductDialog) {
        Dialog(onDismissRequest = { showAddProductDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                var name by remember { mutableStateOf("") }
                var category by remember { mutableStateOf("Cosmetics") }
                var price by remember { mutableStateOf("") }
                var stock by remember { mutableStateOf("") }
                var limit by remember { mutableStateOf("5") }
                var sku by remember { mutableStateOf("") }
                var desc by remember { mutableStateOf("") }

                var expandedCat by remember { mutableStateOf(false) }

                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("Add Boutique Item Catalog", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product / Style Name") }, modifier = Modifier.fillMaxWidth().testTag("product_name_input")) }
                    
                    item {
                        // Category Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { expandedCat = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Category: $category")
                            }
                            DropdownMenu(expanded = expandedCat, onDismissRequest = { expandedCat = false }) {
                                listOf("Cosmetics", "Shoes & Sandals", "Perfumes", "Children's Clothing").forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            category = cat
                                            expandedCat = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item { OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Selling Price ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Initial Stock Quantity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = limit, onValueChange = { limit = it }, label = { Text("Low Stock Alert Limit") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("Bar SKU / code") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Style Details Description") }, modifier = Modifier.fillMaxWidth(), maxLines = 2) }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { showAddProductDialog = false }, modifier = Modifier.weight(1f)) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    val pr = price.toDoubleOrNull() ?: 0.0
                                    val st = stock.toIntOrNull() ?: 0
                                    val lim = limit.toIntOrNull() ?: 5
                                    if (name.isNotBlank()) {
                                        viewModel.addStockItem(name, category, pr, st, lim, sku, desc)
                                        showAddProductDialog = false
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("save_product_btn"),
                                enabled = name.isNotBlank() && price.toDoubleOrNull() != null
                            ) {
                                Text("Add Product")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryItemCard(item: StockItem, viewModel: BoutiqueViewModel) {
    val isAlert = item.stockQuantity <= item.lowStockThreshold
    var showAdjustDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isAlert) BorderStroke(1.5.dp, LowStockAlertColor) else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.category,
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    if (isAlert) {
                        Icon(Icons.Default.NotificationImportant, contentDescription = "Alert", tint = LowStockAlertColor, modifier = Modifier.size(16.dp))
                    }
                }
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                if (item.sku.isNotEmpty()) {
                    Text("SKU: " + item.sku, fontSize = 9.sp, color = Color.GRAY)
                }
            }

            Column {
                Text(
                    text = String.format(Locale.getDefault(), "$%.2f", item.price),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stock text display
                    Column {
                        val stockCol = if (isAlert) LowStockAlertColor else PaymentPaidColor
                        Text(
                            text = "${item.stockQuantity} units",
                            color = stockCol,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text("Limit alert: ${item.lowStockThreshold}", fontSize = 8.sp, color = Color.GRAY)
                    }

                    // Stock adjust button
                    IconButton(
                        onClick = { showAdjustDialog = true },
                        modifier = Modifier
                            .size(28.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape)
                            .testTag("adjust_qty_${item.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Adjust", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                    }
                }
            }
        }
    }

    if (showAdjustDialog) {
        Dialog(onDismissRequest = { showAdjustDialog = false }) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface) {
                var changeAmountText by remember { mutableStateOf("") }
                var isAddition by remember { mutableStateOf(true) }

                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Adjust inventory: ${item.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("Current stock quantity: ${item.stockQuantity}", fontSize = 12.sp, color = Color.GRAY)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isAddition = true },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isAddition) PaymentPaidColor else Color.LightGray.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("RESTOCK (+)", fontSize = 11.sp, color = if (isAddition) Color.WHITE else Color.BLACK)
                        }
                        Button(
                            onClick = { isAddition = false },
                            colors = ButtonDefaults.buttonColors(containerColor = if (!isAddition) LowStockAlertColor else Color.LightGray.copy(alpha = 0.2f)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("DEDUCT (-)", fontSize = 11.sp, color = if (!isAddition) Color.WHITE else Color.BLACK)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = changeAmountText,
                        onValueChange = { changeAmountText = it },
                        label = { Text("Quantity change") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { showAdjustDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val changeVal = changeAmountText.toIntOrNull() ?: 0
                                if (changeVal > 0) {
                                    val logChangeAmount = if (isAddition) changeVal else -changeVal
                                    val finalStock = (item.stockQuantity + logChangeAmount).coerceAtLeast(0)
                                    viewModel.updateStockItem(
                                        item.copy(stockQuantity = finalStock),
                                        manualChangeAmount = logChangeAmount
                                    )
                                    showAdjustDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("save_adjust_btn"),
                            enabled = changeAmountText.toIntOrNull() != null
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. CUSTOMER CRM DATABASE SCREEN
// ==========================================
@Composable
fun CustomerCrmScreen(
    viewModel: BoutiqueViewModel,
    clients: List<Client>,
    invoices: List<Invoice>
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedClientDetails by remember { mutableStateOf<Client?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (selectedClientDetails == null) {
            // CRM Dashboard view
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.updateClientSearch(it)
                    },
                    placeholder = { Text("Search customers...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_client_crm_btn"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add")
                    Text("New")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (clients.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No clients registered in boutique database.", color = Color.GRAY)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(clients) { client ->
                        // Calculate metrics: Money spent & invoices generated
                        val clientInvoices = invoices.filter { it.clientId == client.id }
                        val totalMoneySpent = clientInvoices.sumOf { it.totalAmount }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedClientDetails = client },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(38.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(client.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(client.phoneNumber, fontSize = 12.sp, color = Color.GRAY)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = String.format(Locale.getDefault(), "$%.2f", totalMoneySpent),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${clientInvoices.size} bills",
                                        fontSize = 10.sp,
                                        color = Color.GRAY
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // CRM CUSTOMER DETAIL RECIEPTS HISTORIES VIEW
            val client = selectedClientDetails!!
            val customerInvoices = invoices.filter { it.clientId == client.id }
            val sumSpent = customerInvoices.sumOf { it.totalAmount }

            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedClientDetails = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Customer details profile", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(client.name, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Phone Number: ${client.phoneNumber}", fontSize = 13.sp)
                        if (client.email.isNotEmpty()) Text("Email Addr: ${client.email}", fontSize = 13.sp)
                        if (client.address.isNotEmpty()) Text("Boutique Address: ${client.address}", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("BILLING HISTORY AUDIT", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.GRAY)
                    Text("Cumulative: " + String.format(Locale.getDefault(), "$%.2f", sumSpent), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
                
                Spacer(modifier = Modifier.height(6.dp))

                if (customerInvoices.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No invoice billing logs available.", color = Color.GRAY)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(customerInvoices) { inv ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(inv.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        val formDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(inv.invoiceDate))
                                        Text(formDate, fontSize = 10.sp, color = Color.GRAY)
                                        Text("Paid via: ${inv.paymentMethod}", fontSize = 10.sp, color = Color.GRAY)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = String.format(Locale.getDefault(), "$%.2f", inv.totalAmount),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        // Status dropdown adjustment capability
                                        var isMenuExp by remember { mutableStateOf(false) }
                                        Box {
                                            val col = if (inv.paymentStatus.lowercase() == "paid") PaymentPaidColor else if (inv.paymentStatus.lowercase() == "unpaid") LowStockAlertColor else PaymentPendingColor
                                            Surface(
                                                color = col,
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .clickable { isMenuExp = true }
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) {
                                                    Text(inv.paymentStatus.uppercase(), color = Color.WHITE, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Edit Status", tint = Color.WHITE, modifier = Modifier.size(12.dp))
                                                }
                                            }
                                            DropdownMenu(expanded = isMenuExp, onDismissRequest = { isMenuExp = false }) {
                                                listOf("Paid", "Unpaid", "Partial").forEach { st ->
                                                    DropdownMenuItem(
                                                        text = { Text(st) },
                                                        onClick = {
                                                            viewModel.updateInvoicePaymentStatus(inv.id, st)
                                                            isMenuExp = false
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
    }

    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                var name by remember { mutableStateOf("") }
                var phone by remember { mutableStateOf("") }
                var email by remember { mutableStateOf("") }
                var addr by remember { mutableStateOf("") }

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Create Customer Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Customer Name") }, modifier = Modifier.fillMaxWidth().testTag("crm_name_input"))
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Primary Phone Number") }, modifier = Modifier.fillMaxWidth().testTag("crm_phone_input"))
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email identifier") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = addr, onValueChange = { addr = it }, label = { Text("Delivery Address") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showAddDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Back")
                        }
                        Button(
                            onClick = {
                                if (name.isNotBlank() && phone.isNotBlank()) {
                                    viewModel.addClient(name, phone, email, addr)
                                    showAddDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("save_crm_client_btn"),
                            enabled = name.isNotBlank() && phone.isNotBlank()
                        ) {
                            Text("Register")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. MONTHLY ANALYTICS REPORTS & BRANDING CONFIG
// ==========================================
@Composable
fun ReportsAndBrandingScreen(
    viewModel: BoutiqueViewModel,
    invoices: List<Invoice>
) {
    // Branding settings state fields
    val bName by viewModel.businessName.collectAsStateWithLifecycle()
    val bLogo by viewModel.businessLogoText.collectAsStateWithLifecycle()
    val bPhone by viewModel.businessPhone.collectAsStateWithLifecycle()
    val bAddress by viewModel.businessAddress.collectAsStateWithLifecycle()
    val bNotes by viewModel.customInvoiceNotes.collectAsStateWithLifecycle()
    val activeLayoutId by viewModel.invoiceLayoutId.collectAsStateWithLifecycle()
    val activeSmsTemplateId by viewModel.smsTemplateId.collectAsStateWithLifecycle()

    var editingName by remember { mutableStateOf(bName) }
    var editingLogo by remember { mutableStateOf(bLogo) }
    var editingPhone by remember { mutableStateOf(bPhone) }
    var editingAddress by remember { mutableStateOf(bAddress) }
    var editingNotes by remember { mutableStateOf(bNotes) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SUMMARY METRIC CARDS
        item {
            Text("BOUTIQUE SALES PERFORMANCE ANALYTICS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            
            val totalRevenue = invoices.sumOf { it.totalAmount }
            val paidReceiptsSum = invoices.filter { it.paymentStatus.lowercase() == "paid" }.sumOf { it.totalAmount }
            val pendingMoneyReceivable = invoices.filter { it.paymentStatus.lowercase() != "paid" }.sumOf { it.totalAmount }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Metric 1: Total Sale
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("TOTAL BILLING", fontSize = 9.sp, color = Color.GRAY, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(String.format(Locale.getDefault(), "$%.2f", totalRevenue), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        Text("${invoices.size} generated slips", fontSize = 8.sp, color = Color.GRAY)
                    }
                }

                // Metric 2: Paid Cash
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = PaymentPaidColor.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("PAID REVENUES", fontSize = 9.sp, color = Color.GRAY, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(String.format(Locale.getDefault(), "$%.2f", paidReceiptsSum), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = PaymentPaidColor)
                        Text("${invoices.filter { it.paymentStatus.lowercase() == "paid" }.size} paid tickets", fontSize = 8.sp, color = Color.GRAY)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Metric 3: Pending Bills
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = LowStockAlertColor.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("REAL-TIME RECEIVABLES (PENDING UNPAID)", fontSize = 10.sp, color = Color.GRAY, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Outstanding credits pending to clear safely", fontSize = 10.sp, color = Color.GRAY)
                    }
                    Text(String.format(Locale.getDefault(), "$%.2f", pendingMoneyReceivable), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = LowStockAlertColor)
                }
            }
        }

        // REPORT EXPORTER BUTTON
        item {
            Button(
                onClick = { viewModel.downloadMonthlyReportPdf() },
                modifier = Modifier.fillMaxWidth().testTag("export_report_btn"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF Report")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Monthly Sales PDF Report", fontWeight = FontWeight.Bold)
            }
        }

        // 12 PREMIUM INVOICE DESIGN LAYOUT SELECTOR
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "SELECT BOUTIQUE INVOICE DESIGN (12 THEMES)", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 13.sp, 
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Try any of the 12 beautiful, high-contrast, professional layout templates. Updates both the sharing PDF and active printing styles instantly.", 
                        fontSize = 10.sp, 
                        color = Color.GRAY
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val layoutsList = listOf(
                        Triple(1, "Classic Emerald", "Traditional forest emerald look, elegant serif layout, clean margins."),
                        Triple(2, "Vintage Rose", "Warm blush copper mauve styling, serif accents and decorative elements."),
                        Triple(3, "Royal Burgundy", "Rich velvet plum burgundy and elegant muted gold accents (Default)."),
                        Triple(4, "Midnight Slate", "Clean sharp slate anthracite grey, modern high-contrast corporate finish."),
                        Triple(5, "Golden Luxury", "Elite haute couture styling featuring rich black and pure gold lines."),
                        Triple(6, "Minimalist Clean", "Pure monochrome stark grid configuration with clean industrial lines."),
                        Triple(7, "Eco Mint", "Calming botanical mint sage tones, airy font, natural organic fibers look."),
                        Triple(8, "Vibrant Peach", "Playful Peach Apricot and energetic warm Rust tones for lively boutiques."),
                        Triple(9, "Ocean Breeze", "Seaside deep navy maritime blue with teal accents, crisp coastal textures."),
                        Triple(10, "Retro Chic", "Charming retro sand and warm terracotta rust lines, vintage threads aesthetic."),
                        Triple(11, "Lavender Mist", "Soft delicate lavender periwinkle background, ideal for cosmetics and perfume."),
                        Triple(12, "Neon Noir", "Futuristic custom black theme with vibrant cyber pink and electric cyan lasers.")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        layoutsList.forEach { (id, name, desc) ->
                            val isSelected = activeLayoutId == id
                            val (primaryColor, secondaryColor) = getThemePreviewColors(id)
                            val isThemeDark = id == 12

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectInvoiceLayout(id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    } else {
                                        MaterialTheme.colorScheme.background
                                    }
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.2f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Circular Live Color preview badge
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(if (isThemeDark) Color(0xFF111215) else Color(0xFFFFFFFF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(1.5.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(primaryColor))
                                            Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(secondaryColor))
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isSelected) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = RoundedCornerShape(3.dp),
                                                    modifier = Modifier.padding(horizontal = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "ACTIVE",
                                                        fontSize = 7.sp,
                                                        color = Color.WHITE,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = desc,
                                            fontSize = 11.sp,
                                            color = Color.GRAY,
                                            lineHeight = 14.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.selectInvoiceLayout(id) },
                                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4 PREMIUM SMS & WHATSAPP CUSTOMER COMMUNICATION TEMPLATES
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "SMS & WHATSAPP CHAT COMMUNICATION TEMPLATES", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 13.sp, 
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Configure structured layout formats for instant customer digital messaging. Triggered dynamically via modern wa.me and direct SMS channels.", 
                        fontSize = 10.sp, 
                        color = Color.GRAY
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val templatesList = listOf(
                        Pair(1, "Classic Elegance Receipt"),
                        Pair(2, "Formal Curated Luxury"),
                        Pair(3, "Stark Minimal Invoice Info"),
                        Pair(4, "Friendly Selection & Styling Greeting")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        templatesList.forEach { (id, title) ->
                            val isSelected = activeSmsTemplateId == id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.background
                                    )
                                    .border(
                                        width = if (isSelected) 1.8.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.selectSmsTemplate(id) }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Style $id",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = title.split(" ").first(),
                                        fontSize = 8.sp,
                                        color = Color.GRAY,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // LIVE INTERACTIVE MESSAGE PREVIEW CONTAINER
                    Text(
                        "REAL-TIME TEXT PREVIEW:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                            .border(1.dp, Color.LightGray.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = viewModel.getSmsTemplatePreview(activeSmsTemplateId),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        // BRANDING CONFIGURATION FORM
        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            Text("CUSTOMIZABLE INVOICE BUSINESS BRANDING", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            Text("Binds custom name, logo icon letters, phone, address, and conditions on PDF receipts and customers communications", fontSize = 10.sp, color = Color.GRAY)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editingName,
                        onValueChange = { editingName = it },
                        label = { Text("Business Brand Name") },
                        modifier = Modifier.fillMaxWidth().testTag("config_biz_name")
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editingLogo,
                            onValueChange = { editingLogo = it.take(2) },
                            label = { Text("Logo Initials (2 char)") },
                            modifier = Modifier.weight(1f).testTag("config_biz_logo"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editingPhone,
                            onValueChange = { editingPhone = it },
                            label = { Text("Store Phone No.") },
                            modifier = Modifier.weight(1.5f).testTag("config_biz_phone")
                        )
                    }

                    OutlinedTextField(
                        value = editingAddress,
                        onValueChange = { editingAddress = it },
                        label = { Text("Physical Shop Address") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editingNotes,
                        onValueChange = { editingNotes = it },
                        label = { Text("Receipt Policy Terms Note") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.updateBranding(editingName, editingLogo, editingPhone, editingAddress, editingNotes)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("save_branding_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Branding Details", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. STOCK MOVEMENT LOGS TIMELINE
// ==========================================
@Composable
fun StockLogsScreen(logs: List<InventoryLog>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("INVENTORY AUDIT TRANSACTION HISTORY LOGS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        Text("Automatic chronological audits of sales deductions, restocks, and manual count alterations.", fontSize = 10.sp, color = Color.GRAY)
        Spacer(modifier = Modifier.height(10.dp))

        if (logs.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No inventory logs captured path yet.", color = Color.GRAY)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(log.itemName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                
                                val actionText = when (log.actionType) {
                                    "STOCK_INITIALIZED" -> "Setup"
                                    "STOCK_ADDED" -> "Restock"
                                    "SALE_AUTO_DEDUCT" -> "Sale Auto"
                                    else -> "Adjust"
                                }
                                val actionCol = when (log.actionType) {
                                    "STOCK_ADDED" -> PaymentPaidColor
                                    "SALE_AUTO_DEDUCT" -> MaterialTheme.colorScheme.primary
                                    else -> Color.Gray
                                }

                                Surface(color = actionCol.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                                    Text(
                                        text = actionText,
                                        color = actionCol,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Department: " + log.category,
                                    fontSize = 11.sp,
                                    color = Color.GRAY
                                )
                                val direction = if (log.quantityChanged >= 0) "+" else ""
                                Text(
                                    text = "$direction${log.quantityChanged} units (Closing: ${log.reorderQuantity})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (log.quantityChanged >= 0) PaymentPaidColor else LowStockAlertColor
                                )
                            }
                            
                            if (log.details.isNotEmpty()) {
                                Text(
                                    text = log.details,
                                    fontSize = 10.sp,
                                    color = Color.GRAY,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            val tDate = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
                            Text(
                                text = "Logged: $tDate",
                                fontSize = 8.sp,
                                color = Color.LightGray,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }
    }
}
