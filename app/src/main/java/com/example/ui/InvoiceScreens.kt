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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import coil.compose.AsyncImage
import android.net.Uri
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.defaultMinSize
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

fun getNavTitle(item: String, language: String): String {
    return when (language) {
        "Urdu" -> when (item) {
            "Billing" -> "بلنگ"
            "Stock" -> "سٹاک"
            "CRM" -> "صارفین"
            "Reports" -> "رپورٹس"
            "Logs" -> "لاگز"
            else -> item
        }
        "Pashto" -> when (item) {
            "Billing" -> "بلنګ"
            "Stock" -> "سټاک"
            "CRM" -> "پیرودونکي"
            "Reports" -> "رپورټونه"
            "Logs" -> "سوانح"
            else -> item
        }
        "Arabic" -> when (item) {
            "Billing" -> "الفواتير"
            "Stock" -> "المخزون"
            "CRM" -> "العملاء"
            "Reports" -> "التقارير"
            "Logs" -> "السجلات"
            else -> item
        }
        "Spanish" -> when (item) {
            "Billing" -> "Facturación"
            "Stock" -> "Inventario"
            "CRM" -> "Clientes"
            "Reports" -> "Informes"
            "Logs" -> "Registros"
            else -> item
        }
        else -> item
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
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()

    // Invoice completed dialog control
    var completeInvoiceNum by remember { mutableStateOf<String?>(null) }
    var showInvoiceCompleteDialog by remember { mutableStateOf(false) }
    var showWelcomeSplash by remember { mutableStateOf(true) }
    var showUserGuideDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

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
                actions = {
                    IconButton(
                        onClick = { showUserGuideDialog = true },
                        modifier = Modifier.testTag("action_guide_help")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Help,
                            contentDescription = "Quick Walkthrough Guide",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("action_app_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "App Settings, About & Sharing",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
                    label = { Text(getNavTitle("Billing", appLanguage), fontSize = 11.sp) },
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
                    label = { Text(getNavTitle("Stock", appLanguage), fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_stock")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Customers") },
                    label = { Text(getNavTitle("CRM", appLanguage), fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_crm")
                )
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") },
                    label = { Text(getNavTitle("Reports", appLanguage), fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_reports")
                )
                NavigationBarItem(
                    selected = activeTab == 4,
                    onClick = { activeTab = 4 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Logs") },
                    label = { Text(getNavTitle("Logs", appLanguage), fontSize = 11.sp) },
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
                3 -> ReportsAndBrandingScreen(viewModel = viewModel, invoices = invoices, onLaunchGuide = { showUserGuideDialog = true })
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

            // startup luxury welcome intro dialog
            if (showWelcomeSplash) {
                androidx.compose.ui.window.Dialog(onDismissRequest = { }) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth().padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Royal Crown Logo",
                                    tint = Color.WHITE,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Text(
                                text = "ONE BUSINESS SOLUTION",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )

                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "MADE BY MUHAMMAD HAMZA SABIR",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Text(
                                text = "Welcome to your professional Boutique retail suite. Built with high-graphics color customizations and stylish typography elements to impress clients and organize business workflows.",
                                fontSize = 11.5.sp,
                                color = Color.GRAY,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 16.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        showWelcomeSplash = false
                                        showUserGuideDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Quick Guide", fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { showWelcomeSplash = false },
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Launch Suite", fontWeight = FontWeight.Bold, color = Color.WHITE)
                                }
                            }
                        }
                    }
                }
            }

            if (showSettingsDialog) {
                AppSettingsAndAboutDialog(
                    viewModel = viewModel,
                    onDismiss = { showSettingsDialog = false }
                )
            }

            // Interactive User Walkthrough & Suite Guide Dialog
            if (showUserGuideDialog) {
                androidx.compose.ui.window.Dialog(onDismissRequest = { showUserGuideDialog = false }) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        var guideTab by remember { mutableStateOf(0) } // 0: Vision, 1: Quick Steps, 2: Smart Features

                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Top Banner Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Help,
                                        contentDescription = "Guide",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Interactive Guide",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = { showUserGuideDialog = false },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close Guide",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Dynamic Tab Indicator Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("🌟 Intro", "🔄 Easy Steps", "🛠️ Core Tech").forEachIndexed { index, label ->
                                    val isSelected = guideTab == index
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else Color.Transparent
                                            )
                                            .clickable { guideTab = index }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isSelected) Color.WHITE else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            Divider(color = Color.LightGray.copy(alpha = 0.2f))

                            // Tab Body Content
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp)
                            ) {
                                when (guideTab) {
                                    0 -> { // VISION / APP INTRODUCTION
                                        LazyColumn(
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            item {
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)),
                                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(10.dp)) {
                                                        Text(
                                                            text = "Our Core Aim & Vision",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.5.sp,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = "To empower and equip boutique owners with a premium \"One Business Solution\" ecosystem that is fast, smooth, reliable, and absolutely offline-secure. High-graphics look paired with dynamic customization sets a new benchmark in retail management.",
                                                            fontSize = 11.sp,
                                                            lineHeight = 15.sp,
                                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
                                                        )
                                                    }
                                                }
                                            }

                                            item {
                                                Text("🌟 Fast, Smooth, and Robust Framework", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.secondary)
                                                Text(
                                                    text = "Runs instantly with no cloud delays. Everything works right inside your device's offline secure storage. Features beautiful visual ripple transitions and premium adaptive components.",
                                                    fontSize = 11.sp,
                                                    lineHeight = 14.sp,
                                                    color = Color.GRAY
                                                )
                                            }

                                            item {
                                                Text("🎨 Elegant Graphics & Personalized Branding", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.secondary)
                                                Text(
                                                    text = "Select among 12 gorgeous preset color themes (Classic Emerald, Vintage Rose, Midnight Slate) & dynamic premium typography styles to represent your unique brand identity professionally.",
                                                    fontSize = 11.sp,
                                                    lineHeight = 14.sp,
                                                    color = Color.GRAY
                                                )
                                            }
                                        }
                                    }
                                    1 -> { // CONCISE STEP-BY-STEP USE MANUAL
                                        LazyColumn(
                                            verticalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            val stepList = listOf(
                                                Triple("1", "Manage Inventory first", "Navigate to the 'Stock' tab. Add current product categories, buy prices, retail prices, and initial stock quantities. Real-time low stock levels will be marked in amber/red automatically to prevent running short."),
                                                Triple("2", "Generate Invoices", "In 'Billing' tab, simple click interactive product catalog bubbles to fill your cart. Declare custom client profiles, apply discount percentages, & input Delivery Charges easily before checking out!"),
                                                Triple("3", "Get Receipts & Print PDFs", "On payment, an elegant thermal invoice PDF generates instantly incorporating your custom branding, logo text, contact, terms instructions, and dynamic subtotal summary matrices!")
                                            )
                                            items(stepList) { (stepNum, title, desc) ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.primary),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(stepNum, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color.WHITE)
                                                    }
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(title, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.secondary)
                                                        Text(desc, fontSize = 11.sp, lineHeight = 14.sp, color = Color.GRAY)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    2 -> { // CORE TECHNOLOGY & APP FUNCTIONS HIGHLIGHT
                                        LazyColumn(
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            val features = listOf(
                                                "📈 Real-time Inventory Audits: View comprehensive timeline activity records explaining exactly when items are manually added, updated, or returned.",
                                                "🔄 Canceled Status & Reversal: Easily flag canceled bills inside logs to reverse and return items automatically back into your current stock count inventory.",
                                                "🗣️ Native Languages Prefs: Full localized support for English, Urdu, Pashto, Arabic, and Spanish navigation parameters for easy multi-cultural deployment.",
                                                "📥 Custom SMS & Shares: Swift one-click share buttons to dispatch clean text receipt alerts or cast the raw APK Installer file to other companion devices instantly!"
                                            )
                                            items(features) { feat ->
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f)),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = feat,
                                                        fontSize = 11.sp,
                                                        lineHeight = 15.sp,
                                                        modifier = Modifier.padding(8.dp),
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Divider(color = Color.LightGray.copy(alpha = 0.2f))

                            // Bottom Dismiss & Next Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (guideTab < 2) {
                                    TextButton(
                                        onClick = { guideTab += 1 },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Next Panel ➜", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(10.dp))
                                }

                                Button(
                                    onClick = { showUserGuideDialog = false },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Got It, Close", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.WHITE)
                                }
                            }
                        }
                    }
                }
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
    val activeLayoutId by viewModel.invoiceLayoutId.collectAsStateWithLifecycle()
    val cart by viewModel.invoiceCart.collectAsStateWithLifecycle()
    val discount by viewModel.invoiceDiscount.collectAsStateWithLifecycle()
    val taxRate by viewModel.invoiceTaxRate.collectAsStateWithLifecycle()
    val paymentStatus by viewModel.selectedPaymentStatus.collectAsStateWithLifecycle()
    val paymentMethod by viewModel.selectedPaymentMethod.collectAsStateWithLifecycle()
    val deliveryCharges by viewModel.invoiceDeliveryCharges.collectAsStateWithLifecycle()
    val customPrices by viewModel.cartCustomPrices.collectAsStateWithLifecycle()

    var showClientDialog by remember { mutableStateOf(false) }
    var selectedCategoryTab by remember { mutableStateOf("All") }

    val customCategories by viewModel.categories.collectAsStateWithLifecycle()
    val categories = listOf("All") + customCategories

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Aesthetic Top Hero Banner with real-time dynamic overlay
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box {
                    Image(
                        painter = painterResource(id = com.example.R.drawable.img_boutique_hero),
                        contentDescription = "Boutique Hero",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.65f)
                                    )
                                )
                            )
                    )
                    
                    val brandingName by viewModel.businessName.collectAsStateWithLifecycle()
                    
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Text(
                                text = "PREMIUM BUSINESS WORKSPACE",
                                color = Color.WHITE,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = brandingName,
                            color = Color.WHITE,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

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
                                text = String.format(Locale.getDefault(), "PKR %.2f", item.price),
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
                        // Calculate itemized custom prices and discounts
                        var originalProductsSubtotal = 0.0
                        var totalItemizedDiscounts = 0.0
                        
                        cart.forEach { (id, qty) ->
                            val prod = products.find { it.id == id }
                            if (prod != null) {
                                val standardSP = prod.price
                                val billedSP = customPrices[id] ?: standardSP
                                val discPerUnit = (standardSP - billedSP).coerceAtLeast(0.0)
                                
                                originalProductsSubtotal += standardSP * qty
                                totalItemizedDiscounts += discPerUnit * qty
                            }
                        }

                        Text(
                            text = "Itemized Override Sale Prices / Discounts:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        
                        cart.forEach { (id, qty) ->
                            val prod = products.find { it.id == id }
                            if (prod != null) {
                                val standardSP = prod.price
                                val billedSP = customPrices[id] ?: standardSP
                                val discPerUnit = (standardSP - billedSP).coerceAtLeast(0.0)
                                
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1.2f)) {
                                            Text(prod.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                            Text("Orig S.P: PKR ${String.format(Locale.US, "%.1f", standardSP)} | Qty: $qty", fontSize = 10.sp, color = Color.Gray)
                                            if (discPerUnit > 0.0) {
                                                Text("Disc: -PKR ${String.format(Locale.US, "%.1f", discPerUnit * qty)} total", fontSize = 9.sp, color = LowStockAlertColor, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        
                                        var billedSPInput by remember(billedSP) { mutableStateOf(if (billedSP == standardSP) "" else billedSP.toString()) }
                                        OutlinedTextField(
                                            value = billedSPInput,
                                            onValueChange = { input ->
                                                billedSPInput = input
                                                val parsed = input.toDoubleOrNull()
                                                if (parsed != null && parsed >= 0.0) {
                                                    viewModel.updateCartCustomPrice(id, parsed)
                                                } else if (input.isEmpty()) {
                                                    viewModel.updateCartCustomPrice(id, standardSP)
                                                }
                                            },
                                            label = { Text("Billed S.P.", fontSize = 9.sp) },
                                            placeholder = { Text(standardSP.toString(), fontSize = 10.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            textStyle = TextStyle(fontSize = 11.sp),
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            singleLine = true
                                        )
                                    }
                                }
                            }
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.LightGray.copy(alpha = 0.3f))

                        // Inputs for Discounts & Tax
                        var discountInput by remember { mutableStateOf(discount.toString()) }
                        var taxInput by remember { mutableStateOf(taxRate.toString()) }
                        var deliveryInput by remember { mutableStateOf(if (deliveryCharges == 0.0) "" else deliveryCharges.toString()) }

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
                                label = { Text("Global Disc (PKR)", fontSize = 11.sp) },
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

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = deliveryInput,
                            onValueChange = {
                                deliveryInput = it
                                val del = it.toDoubleOrNull() ?: 0.0
                                viewModel.setInvoiceDeliveryCharges(del)
                             },
                            label = { Text("Delivery Charges (PKR)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            leadingIcon = { Icon(Icons.Default.LocalShipping, contentDescription = "Delivery", modifier = Modifier.size(16.dp)) }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        val combinedDiscountTotal = totalItemizedDiscounts + discount
                        val calculatedTax = (originalProductsSubtotal - combinedDiscountTotal).coerceAtLeast(0.0) * (taxRate / 100.0)
                        val finalTotal = (originalProductsSubtotal - combinedDiscountTotal).coerceAtLeast(0.0) + calculatedTax + deliveryCharges

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Retail Subtotal:", fontSize = 13.sp, color = Color.GRAY)
                            Text(String.format(Locale.getDefault(), "PKR %.2f", originalProductsSubtotal), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Combined Discounts:", fontSize = 13.sp, color = Color.GRAY)
                            Text(String.format(Locale.getDefault(), "-PKR %.2f", combinedDiscountTotal), fontSize = 13.sp, color = LowStockAlertColor)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tax (GST):", fontSize = 13.sp, color = Color.GRAY)
                            Text(String.format(Locale.getDefault(), "+PKR %.2f", calculatedTax), fontSize = 13.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Delivery Charges:", fontSize = 13.sp, color = Color.GRAY)
                            Text(String.format(Locale.getDefault(), "+PKR %.2f", deliveryCharges), fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("GRAND TOTAL:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(String.format(Locale.getDefault(), "PKR %.2f", finalTotal), fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
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

                        // STEP 6: Invoice Layout & Design Theme Selector
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ColorLens, 
                                contentDescription = "Design Layout", 
                                tint = MaterialTheme.colorScheme.primary, 
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SELECT INVOICE DESIGN / THEME (16 PREMIUM LAYOUTS)", 
                                fontSize = 11.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        val themesList = listOf(
                            Triple(1, "Classic Emerald", Color(0xFF1B4D3E)),
                            Triple(2, "Vintage Rose", Color(0xFF8E4162)),
                            Triple(3, "Royal Burgundy", Color(0xFF7A2048)),
                            Triple(4, "Midnight Slate", Color(0xFF1E293B)),
                            Triple(5, "Golden Luxury", Color(0xFFD4A373)),
                            Triple(6, "Minimalist Clean", Color(0xFF424242)),
                            Triple(7, "Eco Mint", Color(0xFF2E5A44)),
                            Triple(8, "Vibrant Peach", Color(0xFFD35400)),
                            Triple(9, "Ocean Breeze", Color(0xFF1A365D)),
                            Triple(10, "Retro Chic", Color(0xFF7E4E30)),
                            Triple(11, "Lavender Mist", Color(0xFF5F4B8B)),
                            Triple(12, "Neon Noir", Color(0xFFE0115F)),
                            Triple(13, "Enterprise Blue", Color(0xFF0D3B66)),
                            Triple(14, "Matcha Tea", Color(0xFF556B2F)),
                            Triple(15, "Prestige Crimson", Color(0xFF800020)),
                            Triple(16, "Plum Velvet", Color(0xFF4A154B))
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            themesList.chunked(3).forEach { chunk ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    chunk.forEach { (id, label, color) ->
                                        val isThemeSelected = activeLayoutId == id
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (isThemeSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                    else MaterialTheme.colorScheme.background
                                                )
                                                .border(
                                                    width = if (isThemeSelected) 1.5.dp else 1.dp,
                                                    color = if (isThemeSelected) color else Color.LightGray.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .clickable { viewModel.selectInvoiceLayout(id) }
                                                .padding(horizontal = 6.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                            )
                                            Text(
                                                text = label,
                                                fontSize = 9.sp,
                                                maxLines = 1,
                                                fontWeight = if (isThemeSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isThemeSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
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
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()
    val invoice = remember(invoices, invoiceNum) { invoices.find { it.invoiceNumber == invoiceNum } }
    val initialPhone = invoice?.clientPhone ?: ""

    // Phone Country Code Formatter
    var selectedCode by remember { mutableStateOf("+92") }
    var rawPhone by remember { mutableStateOf("") }
    var showCodeDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(initialPhone) {
        if (initialPhone.isNotEmpty()) {
            val matchingPrefix = listOf("+92", "+1", "+44", "+91", "+966", "+971").find { initialPhone.startsWith(it) }
            if (matchingPrefix != null) {
                selectedCode = matchingPrefix
                rawPhone = initialPhone.substring(matchingPrefix.length)
            } else {
                selectedCode = "+92"
                rawPhone = if (initialPhone.startsWith("0")) initialPhone.substring(1) else initialPhone
            }
        }
    }

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
                    text = "Deducted original stock levels & tracked payment status. Customize details below before sending:",
                    fontSize = 11.sp,
                    color = Color.GRAY,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // MANUAL COUNTRY CODE SELECTOR CONTAINER
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "MANUAL COUNTRY CODE SELECTOR (OPTIONAL)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.wrapContentSize()) {
                                OutlinedButton(
                                    onClick = { showCodeDropdown = true },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                                    modifier = Modifier.defaultMinSize(minHeight = 1.dp, minWidth = 1.dp)
                                ) {
                                    Text(if (selectedCode.isEmpty()) "Prefix" else selectedCode, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", modifier = Modifier.size(14.dp))
                                }
                                DropdownMenu(
                                    expanded = showCodeDropdown,
                                    onDismissRequest = { showCodeDropdown = false }
                                ) {
                                    listOf(
                                        Pair("+92", "Pakistan (+92)"),
                                        Pair("+1", "USA/CAN (+1)"),
                                        Pair("+44", "UK (+44)"),
                                        Pair("+91", "India (+91)"),
                                        Pair("+966", "KSA (+966)"),
                                        Pair("+971", "UAE (+971)"),
                                        Pair("", "No prefix")
                                    ).forEach { (code, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label, fontSize = 11.sp) },
                                            onClick = {
                                                selectedCode = code
                                                showCodeDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            OutlinedTextField(
                                value = rawPhone,
                                onValueChange = { rawPhone = it },
                                placeholder = { Text("3001234567") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(48.dp),
                                textStyle = TextStyle(fontSize = 12.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons for sharing channels
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val finalPhoneNumber = selectedCode + rawPhone.replace(Regex("[^0-9]"), "")

                    // WhatsApp
                    Button(
                        onClick = { viewModel.sendWhatsAppMessage(invoiceNum, finalPhoneNumber) },
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
                        onClick = { viewModel.sendSimSms(invoiceNum, finalPhoneNumber) },
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

    val customCategories by viewModel.categories.collectAsStateWithLifecycle()
    val filterCategories = listOf("All") + customCategories

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
                            text = "${lowStock.size} inventory items require immediate restock replenishment.",
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
                var category by remember { mutableStateOf(customCategories.firstOrNull() ?: "Cosmetics") }
                var costPrice by remember { mutableStateOf("") }
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
                        Text("Add Product / Item Catalog", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
                                customCategories.forEach { cat ->
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

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = costPrice,
                                onValueChange = { costPrice = it },
                                label = { Text("Cost Price C.P. (PKR)", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = TextStyle(fontSize = 12.sp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = price,
                                onValueChange = { price = it },
                                label = { Text("Sale Price S.P. (PKR)", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = TextStyle(fontSize = 12.sp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
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
                                    val cp = costPrice.toDoubleOrNull() ?: 0.0
                                    val st = stock.toIntOrNull() ?: 0
                                    val lim = limit.toIntOrNull() ?: 5
                                    if (name.isNotBlank()) {
                                        viewModel.addStockItem(name, category, pr, cp, st, lim, sku, desc)
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
                    text = String.format(Locale.getDefault(), "PKR %.2f", item.price),
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
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                var editingName by remember { mutableStateOf(item.name) }
                var editingCategory by remember { mutableStateOf(item.category) }
                var editingPrice by remember { mutableStateOf(item.price.toString()) }
                var editingCostPrice by remember { mutableStateOf(item.costPrice.toString()) }
                var editingStock by remember { mutableStateOf(item.stockQuantity.toString()) }
                var editingLimit by remember { mutableStateOf(item.lowStockThreshold.toString()) }
                var editingSkubyVal by remember { mutableStateOf(item.sku) }
                var editingDesc by remember { mutableStateOf(item.description) }

                var showDeleteConfirm by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .heightIn(max = 560.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (showDeleteConfirm) "Confirm Deletion" else "Edit Product / Item",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (showDeleteConfirm) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LowStockAlertColor.copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, LowStockAlertColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.DeleteForever, contentDescription = "Alert", tint = LowStockAlertColor, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Are you absolutely sure you want to delete this product? This will permanently erase it from inventories.", fontSize = 11.sp, textAlign = TextAlign.Center)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { showDeleteConfirm = false }, modifier = Modifier.weight(1f)) {
                                Text("No, Cancel")
                            }
                            Button(
                                onClick = {
                                    viewModel.deleteStockItem(item)
                                    showAdjustDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LowStockAlertColor),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Yes, Delete", color = Color.WHITE)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                OutlinedTextField(
                                    value = editingName,
                                    onValueChange = { editingName = it },
                                    label = { Text("Product Title", fontSize = 11.sp) },
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = 13.sp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = editingCategory,
                                    onValueChange = { editingCategory = it },
                                    label = { Text("Category", fontSize = 11.sp) },
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = 13.sp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = editingCostPrice,
                                        onValueChange = { editingCostPrice = it },
                                        label = { Text("Cost Price C.P.", fontSize = 10.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        textStyle = TextStyle(fontSize = 12.sp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = editingPrice,
                                        onValueChange = { editingPrice = it },
                                        label = { Text("Sale Price S.P.", fontSize = 10.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        textStyle = TextStyle(fontSize = 12.sp),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            item {
                                OutlinedTextField(
                                    value = editingLimit,
                                    onValueChange = { editingLimit = it },
                                    label = { Text("Low Stock Alert Limit", fontSize = 10.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = TextStyle(fontSize = 12.sp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = editingStock,
                                        onValueChange = { editingStock = it },
                                        label = { Text("Current Stock", fontSize = 10.sp) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        textStyle = TextStyle(fontSize = 12.sp),
                                        modifier = Modifier.weight(1.2f)
                                    )
                                    Button(
                                        onClick = {
                                            val s = editingStock.toIntOrNull() ?: 0
                                            editingStock = (s - 1).coerceAtLeast(0).toString()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.size(36.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                                    ) {
                                        Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            val s = editingStock.toIntOrNull() ?: 0
                                            editingStock = (s + 1).toString()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            item {
                                OutlinedTextField(
                                    value = editingSkubyVal,
                                    onValueChange = { editingSkubyVal = it },
                                    label = { Text("SKU / barcode / code", fontSize = 11.sp) },
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = 13.sp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = editingDesc,
                                    onValueChange = { editingDesc = it },
                                    label = { Text("Short Details Description", fontSize = 11.sp) },
                                    textStyle = TextStyle(fontSize = 13.sp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showDeleteConfirm = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = LowStockAlertColor),
                                border = BorderStroke(1.dp, LowStockAlertColor.copy(alpha = 0.5f)),
                                modifier = Modifier.weight(0.9f)
                            ) {
                                Text("Delete", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { showAdjustDialog = false }, 
                                modifier = Modifier.weight(0.9f)
                            ) {
                                Text("Cancel", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    val priceVal = editingPrice.toDoubleOrNull() ?: item.price
                                    val costPriceVal = editingCostPrice.toDoubleOrNull() ?: item.costPrice
                                    val stockVal = editingStock.toIntOrNull() ?: item.stockQuantity
                                    val limitVal = editingLimit.toIntOrNull() ?: item.lowStockThreshold
                                    val finalDiff = stockVal - item.stockQuantity

                                    viewModel.updateStockItem(
                                        item.copy(
                                            name = editingName,
                                            category = editingCategory,
                                            price = priceVal,
                                            costPrice = costPriceVal,
                                            stockQuantity = stockVal,
                                            lowStockThreshold = limitVal,
                                            sku = editingSkubyVal,
                                            description = editingDesc
                                        ),
                                        manualChangeAmount = finalDiff
                                    )
                                    showAdjustDialog = false
                                },
                                modifier = Modifier.weight(1.2f).testTag("save_adjust_btn"),
                                enabled = editingName.isNotBlank() && editingPrice.toDoubleOrNull() != null && editingStock.toIntOrNull() != null
                            ) {
                                Text("Save", fontSize = 11.sp)
                            }
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
    var showEditClientDialog by remember { mutableStateOf(false) }
    var showDeleteClientConfirm by remember { mutableStateOf(false) }

    var showCancelDialog by remember { mutableStateOf(false) }
    var invoiceToCancel by remember { mutableStateOf<Invoice?>(null) }
    var cancelReasonText by remember { mutableStateOf("") }

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
                    Text("No clients registered in business database.", color = Color.GRAY)
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
                                        text = String.format(Locale.getDefault(), "PKR %.2f", totalMoneySpent),
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
                        if (client.address.isNotEmpty()) Text("Client Address: ${client.address}", fontSize = 13.sp)
                        
                        // Edit & Delete Customizer actions inside Customer CRM profile card
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = { showEditClientDialog = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Profile", modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            TextButton(
                                onClick = { showDeleteClientConfirm = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = LowStockAlertColor)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Customer", modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("BILLING HISTORY AUDIT", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.GRAY)
                    Text("Cumulative: " + String.format(Locale.getDefault(), "PKR %.2f", sumSpent), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
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
                                            text = String.format(Locale.getDefault(), "PKR %.2f", inv.totalAmount),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        // Status dropdown adjustment capability
                                        var isMenuExp by remember { mutableStateOf(false) }
                                        Box {
                                            val col = if (inv.paymentStatus.lowercase() == "paid") PaymentPaidColor else if (inv.paymentStatus.lowercase() == "unpaid") LowStockAlertColor else if (inv.paymentStatus.lowercase() == "canceled") Color.Gray else PaymentPendingColor
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
                                                HorizontalDivider()
                                                DropdownMenuItem(
                                                    text = { Text("Cancel & Reverse Stock", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
                                                    onClick = {
                                                        invoiceToCancel = inv
                                                        cancelReasonText = ""
                                                        showCancelDialog = true
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

    // Dynamic Client Editing Dialog
    if (showEditClientDialog && selectedClientDetails != null) {
        val client = selectedClientDetails!!
        Dialog(onDismissRequest = { showEditClientDialog = false }) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                var name by remember { mutableStateOf(client.name) }
                var phone by remember { mutableStateOf(client.phoneNumber) }
                var email by remember { mutableStateOf(client.email) }
                var addr by remember { mutableStateOf(client.address) }

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Edit Customer Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Customer Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email identifier") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = addr, onValueChange = { addr = it }, label = { Text("Client Address") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showEditClientDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (name.isNotBlank() && phone.isNotBlank()) {
                                    val updated = client.copy(name = name, phoneNumber = phone, email = email, address = addr)
                                    viewModel.updateClient(updated)
                                    selectedClientDetails = updated
                                    showEditClientDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = name.isNotBlank() && phone.isNotBlank()
                        ) {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }

    // Customer deletion confirmation modal dialog
    if (showDeleteClientConfirm && selectedClientDetails != null) {
        val client = selectedClientDetails!!
        Dialog(onDismissRequest = { showDeleteClientConfirm = false }) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = "Delete Check", tint = LowStockAlertColor, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Delete Customer?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Are you sure you want to delete ${client.name}? This will remove them from your active CRM records.", fontSize = 12.sp, color = Color.GRAY, textAlign = TextAlign.Center)

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showDeleteClientConfirm = false }, modifier = Modifier.weight(1f)) {
                            Text("No, Keep")
                        }
                        Button(
                            onClick = {
                                viewModel.deleteClient(client)
                                selectedClientDetails = null
                                showDeleteClientConfirm = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LowStockAlertColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Yes, Delete", color = Color.WHITE)
                        }
                    }
                }
            }
        }
    }

    // Invoice Cancellation Dialog (for stock reversals and logs with reason)
    if (showCancelDialog && invoiceToCancel != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showCancelDialog = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cancel Bill & Reverse Stocks",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Text(
                        text = "This will mark Invoice ${invoiceToCancel!!.invoiceNumber} as CANCELED, return all billed items back to stock inventory counters, and update records. Please write details of the cancellation reason below.",
                        fontSize = 11.sp,
                        color = Color.GRAY
                    )
                    
                    OutlinedTextField(
                        value = cancelReasonText,
                        onValueChange = { cancelReasonText = it },
                        label = { Text("Reason for Reversal") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Size exchange, customer return, billing error") },
                        shape = RoundedCornerShape(8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showCancelDialog = false
                                invoiceToCancel = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Go Back")
                        }
                        Button(
                            onClick = {
                                if (cancelReasonText.isNotBlank()) {
                                    viewModel.cancelOrReverseInvoice(invoiceToCancel!!, cancelReasonText)
                                    showCancelDialog = false
                                    invoiceToCancel = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1.5f),
                            enabled = cancelReasonText.isNotBlank(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Confirm Reversal", fontWeight = FontWeight.Bold)
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
    invoices: List<Invoice>,
    onLaunchGuide: () -> Unit
) {
    // Branding settings state fields
    val bName by viewModel.businessName.collectAsStateWithLifecycle()
    val bLogo by viewModel.businessLogoText.collectAsStateWithLifecycle()
    val bPhone by viewModel.businessPhone.collectAsStateWithLifecycle()
    val bAddress by viewModel.businessAddress.collectAsStateWithLifecycle()
    val bNotes by viewModel.customInvoiceNotes.collectAsStateWithLifecycle()
    val activeLayoutId by viewModel.invoiceLayoutId.collectAsStateWithLifecycle()
    val activeSmsTemplateId by viewModel.smsTemplateId.collectAsStateWithLifecycle()
    val selectedFontType by viewModel.selectedFontType.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()

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
                        Text(String.format(Locale.getDefault(), "PKR %.2f", totalRevenue), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
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
                        Text(String.format(Locale.getDefault(), "PKR %.2f", paidReceiptsSum), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = PaymentPaidColor)
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
                    Text(String.format(Locale.getDefault(), "PKR %.2f", pendingMoneyReceivable), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = LowStockAlertColor)
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
                        Triple(8, "Vibrant Peach", "Playful Peach Apricot and energetic warm Rust tones for lively shops."),
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

                    // LOGO UPLOAD COMPONENT
                    val logoUri by viewModel.businessLogoUri.collectAsStateWithLifecycle()
                    val showLogoOnInvoice by viewModel.showLogoOnInvoice.collectAsStateWithLifecycle()
                    val context = LocalContext.current
                    
                    val imagePickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.PickVisualMedia()
                    ) { uri: Uri? ->
                        if (uri != null) {
                            viewModel.uploadLogoImage(uri)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("BRAND LOGO IMAGE", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (logoUri != null) {
                                AsyncImage(
                                    model = logoUri,
                                    contentDescription = "Uploaded Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.CloudUpload,
                                    contentDescription = "No Logo",
                                    tint = Color.GRAY,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = {
                                        imagePickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                                ) {
                                    Text(if (logoUri != null) "Change Photo" else "Upload Logo", fontSize = 11.sp)
                                }
                                
                                if (logoUri != null) {
                                    OutlinedButton(
                                        onClick = { viewModel.removeLogoImage() },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Remove", fontSize = 11.sp)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Max size 1MB, square PNG/JPG recommended.", fontSize = 9.sp, color = Color.GRAY)
                        }
                    }

                    if (logoUri != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Add brand logo image to generated PDF invoices", fontSize = 11.sp)
                            Switch(
                                checked = showLogoOnInvoice,
                                onCheckedChange = { viewModel.setShowLogoOnInvoice(it) }
                            )
                        }
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 4.dp))

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

        // MANAGE INVENTORY TYPES & CATEGORIES
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth().testTag("manage_categories_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Categories Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "MANAGE INVENTORY TYPES (CATEGORIES)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Text(
                        text = "Customize item types for stock tracking and invoice filters. Add your own custom categories (e.g. Cosmetics, Electronics, Suits) or remove existing ones.",
                        fontSize = 11.sp,
                        color = Color.GRAY,
                        lineHeight = 14.sp
                    )

                    val customCategories by viewModel.categories.collectAsStateWithLifecycle()
                    var newCategoryName by remember { mutableStateOf("") }

                    // Add category section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            placeholder = { Text("E.g. Accessories") },
                            label = { Text("Add New Type") },
                            modifier = Modifier.weight(1f).testTag("new_category_input"),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (newCategoryName.trim().isNotEmpty()) {
                                    viewModel.addCategory(newCategoryName)
                                    newCategoryName = ""
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("add_category_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                        }
                    }

                    // Existing categories chips with delete buttons
                    Text("ACTIVE INVENTORY TYPES", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 4.dp))
                    
                    // Simple wrap layout of categories
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        customCategories.chunked(2).forEach { rowList ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowList.forEach { catName ->
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = catName,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = {
                                                    viewModel.removeCategory(catName)
                                                },
                                                modifier = Modifier.size(24.dp).testTag("delete_category_$catName")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete $catName",
                                                    tint = Color.Red.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                // Fill empty weights if the row has odd number of items
                                if (rowList.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // LOAD TEMPORARY DEMO STOCKS
        item {
            val context = LocalContext.current
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.prepopulateDemoStocks()
                        android.widget.Toast.makeText(context, "Successfully loaded 9 premium demo stock items across Cosmetics, Shoes, Perfumes & Clothing!", android.widget.Toast.LENGTH_LONG).show()
                    }
                    .testTag("action_load_demo_stocks_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Demo Stocks Icon",
                            tint = Color.WHITE,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "⚡ Load Temporary Demo Stocks",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap to instantly populate your database with 9 pristine high-quality stock items (Lipsticks, Heels, Mists, Frocks) for quick preview / billing checkout testing.",
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Prepopulate",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // INTERACTIVE WALKTHROUGH GUIDELINES CARD
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLaunchGuide() }
                    .testTag("action_guide_settings_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Manual Icon",
                            tint = Color.WHITE,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Interactive Guide & Walkthrough",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Learn how to configure active languages, billing workflows, stock inventory counts, and presets themes inside your business suite.",
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Launch",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // APP CUSTOMIZABLE FEATURES (Language, Fonts, Colors, Dynamic Layouts)
        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            Text("APP PERSONALIZATION & STYLISH GRAPHICS CONFIG", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            Text("Customize active languages, change app-wide elegant typography font face styles, and apply royal high-graphics theme colors.", fontSize = 10.sp, color = Color.GRAY)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    
                    // 1. CHOOSE SYSTEM LANGUAGE
                    Column {
                        Text("CHOOSE SYSTEM LANGUAGE", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("English", "Urdu", "Pashto", "Arabic", "Spanish").forEach { lang ->
                                val isLangSelected = appLanguage == lang
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isLangSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                        .clickable { viewModel.setAppLanguage(lang) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = lang,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (isLangSelected) Color.WHITE else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Divider(color = Color.LightGray.copy(alpha = 0.15f))

                    // 2. CHOOSE TYPOGRAPHY DEFAULT FONTS
                    Column {
                        Text("CHOOSE DEFAULT FONTS FACES", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Royal Serif", "Modern Chic", "Technical Mono").forEach { font ->
                                val isFontSelected = selectedFontType == font
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isFontSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                        .clickable { viewModel.setSelectedFontType(font) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = font,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isFontSelected) Color.WHITE else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Divider(color = Color.LightGray.copy(alpha = 0.15f))

                    // 3. COLOR PALETTES / SYSTEM SCHEMES
                    Column {
                        Text("CHOOSE STYLISH HIGH-GRAPHICS SCHEME", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(6.dp))

                        val themesList = listOf(
                            Triple(1, "Classic Emerald", Color(0xFF1B4D3E)),
                            Triple(2, "Vintage Rose", Color(0xFF8E4162)),
                            Triple(3, "Royal Burgundy", Color(0xFF7A2048)),
                            Triple(4, "Midnight Slate", Color(0xFF1E293B)),
                            Triple(5, "Golden Luxury", Color(0xFFD4A373)),
                            Triple(6, "Minimalist Clean", Color(0xFF424242)),
                            Triple(7, "Eco Mint", Color(0xFF2E5A44)),
                            Triple(8, "Vibrant Peach", Color(0xFFD35400)),
                            Triple(9, "Ocean Breeze", Color(0xFF1A365D)),
                            Triple(10, "Retro Chic", Color(0xFF7E4E30)),
                            Triple(11, "Lavender Mist", Color(0xFF5F4B8B)),
                            Triple(12, "Neon Noir", Color(0xFFE0115F)),
                            Triple(13, "Enterprise Blue", Color(0xFF0D3B66)),
                            Triple(14, "Matcha Tea", Color(0xFF556B2F)),
                            Triple(15, "Prestige Crimson", Color(0xFF800020)),
                            Triple(16, "Plum Velvet", Color(0xFF4A154B))
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            themesList.chunked(3).forEach { chunk ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    chunk.forEach { (id, label, color) ->
                                        val isThemeSelected = activeLayoutId == id
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (isThemeSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                    else MaterialTheme.colorScheme.background
                                                )
                                                .border(
                                                    width = if (isThemeSelected) 1.5.dp else 1.dp,
                                                    color = if (isThemeSelected) color else Color.LightGray.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .clickable { viewModel.selectInvoiceLayout(id) }
                                                .padding(horizontal = 6.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                            )
                                            Text(
                                                text = label,
                                                fontSize = 9.sp,
                                                maxLines = 1,
                                                fontWeight = if (isThemeSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isThemeSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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

        // THE ROYAL BRANDING & RECOGNITION / ABOUT SECTION WITH APK SHARING
        item {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                ),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Royal Golden Crown/Logo Icon
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Royal Crest",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "ONE BUSINESS SOLUTION",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "MADE BY MUHAMMAD HAMZA SABIR",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = "This premium bespoke digital solution is custom handcrafted and configured specifically to impress. Equips retail business owners with beautiful instant PDF thermal-style receipts layout engines, automatic stock inventory audits tracker, customized language dictionary preferences, dynamic font adjustments, and intelligent client profiling records.",
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // ACTION BUTTON: SHARE SUITE APK FILE
                    val context = LocalContext.current
                    Button(
                        onClick = {
                            try {
                                val sourceApkFile = java.io.File(context.applicationInfo.sourceDir)
                                val cacheApkFile = java.io.File(context.cacheDir, "One_Solution.apk")
                                if (!cacheApkFile.exists() || cacheApkFile.length() != sourceApkFile.length()) {
                                    sourceApkFile.inputStream().use { input ->
                                        cacheApkFile.outputStream().use { output ->
                                            input.copyTo(output)
                                        }
                                    }
                                }
                                val apkUri = androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    cacheApkFile
                                )
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/vnd.android.package-archive"
                                    putExtra(Intent.EXTRA_STREAM, apkUri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share One Solution APK via"))
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "Failed to share APK: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share App", tint = Color.WHITE)
                            Text("Share Suite APK Installer File", fontWeight = FontWeight.Bold, color = Color.WHITE)
                        }
                    }
                    
                    Text(
                        text = "Version 2.4.1 (Royal Enterprise Build) • Safe & Secure Offline Storage",
                        fontSize = 9.sp,
                        color = Color.GRAY
                    )
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsAndAboutDialog(
    viewModel: BoutiqueViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    // Collect settings states
    val activeLayoutId by viewModel.invoiceLayoutId.collectAsStateWithLifecycle()
    val selectedFontType by viewModel.selectedFontType.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    
    val bName by viewModel.businessName.collectAsStateWithLifecycle()
    val bLogo by viewModel.businessLogoText.collectAsStateWithLifecycle()
    val bPhone by viewModel.businessPhone.collectAsStateWithLifecycle()
    val bAddress by viewModel.businessAddress.collectAsStateWithLifecycle()
    val bNotes by viewModel.customInvoiceNotes.collectAsStateWithLifecycle()
    
    var editingName by remember { mutableStateOf(bName) }
    var editingPhone by remember { mutableStateOf(bPhone) }
    var editingAddress by remember { mutableStateOf(bAddress) }
    var editingNotes by remember { mutableStateOf(bNotes) }
    var editingLogo by remember { mutableStateOf(bLogo) }

    val msgMode by viewModel.messagingMode.collectAsStateWithLifecycle()
    val waUrl by viewModel.whatsappApiUrl.collectAsStateWithLifecycle()
    val waKey by viewModel.whatsappApiKey.collectAsStateWithLifecycle()
    val smsUrl by viewModel.smsApiUrl.collectAsStateWithLifecycle()
    val smsKey by viewModel.smsApiKey.collectAsStateWithLifecycle()

    var editingMsgMode by remember(msgMode) { mutableStateOf(msgMode) }
    var editingWaUrl by remember(waUrl) { mutableStateOf(waUrl) }
    var editingWaKey by remember(waKey) { mutableStateOf(waKey) }
    var editingSmsUrl by remember(smsUrl) { mutableStateOf(smsUrl) }
    var editingSmsKey by remember(smsKey) { mutableStateOf(smsKey) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Settings, About & Share",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.GRAY,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.3f))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Language Settings
                    item {
                        Column {
                            Text(
                                "CHOOSE SYSTEM LANGUAGE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("English", "Urdu", "Pashto", "Arabic", "Spanish").forEach { lang ->
                                    val isLangSelected = appLanguage == lang
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isLangSelected) MaterialTheme.colorScheme.primary 
                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            )
                                            .clickable { viewModel.setAppLanguage(lang) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = lang,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = if (isLangSelected) Color.WHITE else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Choose Fonts
                    item {
                        Column {
                            Text(
                                "CHOOSE APP TYPOGRAPHY",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Royal Serif", "Modern Chic", "Technical Mono").forEach { font ->
                                    val isFontSelected = selectedFontType == font
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isFontSelected) MaterialTheme.colorScheme.primary 
                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            )
                                            .clickable { viewModel.setSelectedFontType(font) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = font,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = if (isFontSelected) Color.WHITE else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2b. Display Style & Mode Selection
                    item {
                        Column {
                            Text(
                                "LIGHT / DARK / STANDARD DISPLAY STATE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Light", "Dark", "Standard").forEach { mode ->
                                    val isSelected = themeMode == mode
                                    val label = when (mode) {
                                        "Light" -> "Light Mode"
                                        "Dark" -> "Dark Mode"
                                        else -> "System Auto"
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary 
                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            )
                                            .clickable { viewModel.setThemeMode(mode) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = if (isSelected) Color.WHITE else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Brand Theme & Colors selection
                    item {
                        Column {
                            Text(
                                "CHOOSE STYLISH HIGH-GRAPHICS THEME",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            val themesList = listOf(
                                Triple(1, "Classic Emerald", Color(0xFF1B4D3E)),
                                Triple(2, "Vintage Rose", Color(0xFF8E4162)),
                                Triple(3, "Royal Burgundy", Color(0xFF7A2048)),
                                Triple(4, "Midnight Slate", Color(0xFF1E293B)),
                                Triple(5, "Golden Luxury", Color(0xFFD4A373)),
                                Triple(6, "Minimalist Clean", Color(0xFF424242)),
                                Triple(7, "Eco Mint", Color(0xFF2E5A44)),
                                Triple(8, "Vibrant Peach", Color(0xFFD35400)),
                                Triple(9, "Ocean Breeze", Color(0xFF1A365D)),
                                Triple(10, "Retro Chic", Color(0xFF7E4E30)),
                                Triple(11, "Lavender Mist", Color(0xFF5F4B8B)),
                                Triple(12, "Neon Noir", Color(0xFFE0115F)),
                                Triple(13, "Enterprise Blue", Color(0xFF0D3B66)),
                                Triple(14, "Matcha Tea", Color(0xFF556B2F)),
                                Triple(15, "Prestige Crimson", Color(0xFF800020)),
                                Triple(16, "Plum Velvet", Color(0xFF4A154B))
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                themesList.chunked(3).forEach { chunk ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        chunk.forEach { (id, label, color) ->
                                            val isThemeSelected = activeLayoutId == id
                                            Row(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (isThemeSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                        else MaterialTheme.colorScheme.background
                                                    )
                                                    .border(
                                                        width = if (isThemeSelected) 1.5.dp else 1.dp,
                                                        color = if (isThemeSelected) color else Color.LightGray.copy(alpha = 0.15f),
                                                        shape = RoundedCornerShape(6.dp)
                                                    )
                                                    .clickable { viewModel.selectInvoiceLayout(id) }
                                                    .padding(horizontal = 4.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(color)
                                                )
                                                Text(
                                                    text = label,
                                                    fontSize = 8.sp,
                                                    maxLines = 1,
                                                    fontWeight = if (isThemeSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isThemeSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Customizable Business Branding Form
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "EDIT STORE BRAND DETAILS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            
                            OutlinedTextField(
                                value = editingName,
                                onValueChange = { editingName = it },
                                label = { Text("Brand Name / Business Label", fontSize = 10.sp) },
                                textStyle = TextStyle(fontSize = 11.sp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = editingLogo,
                                    onValueChange = { editingLogo = it.take(2) },
                                    label = { Text("Logo Initials (2 char)", fontSize = 10.sp) },
                                    textStyle = TextStyle(fontSize = 11.sp),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = editingPhone,
                                    onValueChange = { editingPhone = it },
                                    label = { Text("Store Phone No.", fontSize = 10.sp) },
                                    textStyle = TextStyle(fontSize = 11.sp),
                                    modifier = Modifier.weight(1.5f),
                                    singleLine = true
                                )
                            }

                            OutlinedTextField(
                                value = editingAddress,
                                onValueChange = { editingAddress = it },
                                label = { Text("Physical Shop Address", fontSize = 10.sp) },
                                textStyle = TextStyle(fontSize = 11.sp),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = editingNotes,
                                onValueChange = { editingNotes = it },
                                label = { Text("Receipt Policy Note / Terms", fontSize = 10.sp) },
                                textStyle = TextStyle(fontSize = 11.sp),
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 2
                            )

                            Button(
                                onClick = {
                                    viewModel.updateBranding(editingName, editingLogo, editingPhone, editingAddress, editingNotes)
                                    android.widget.Toast.makeText(context, "Branding Saved Successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Save Branding Configs", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 4b. SMS and WhatsApp Gateway API Config Panel
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "MESSAGING & SMS OUTBOUND GATEWAYS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            
                            Text(
                                text = "Set credentials for your cloud SMS & WhatsApp gateways to dispatch messages directly from servers rather than triggering local apps.",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 12.sp
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(0 to "System default (SIM/App)", 1 to "Direct Gateway APIs").forEach { (modeVal, label) ->
                                    val isModeSelected = editingMsgMode == modeVal
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isModeSelected) MaterialTheme.colorScheme.primary 
                                                else MaterialTheme.colorScheme.surface
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isModeSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { editingMsgMode = modeVal }
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.5.sp,
                                            color = if (isModeSelected) Color.WHITE else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            if (editingMsgMode == 1) {
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "WhatsApp Integration API",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        
                                        OutlinedTextField(
                                            value = editingWaUrl,
                                            onValueChange = { editingWaUrl = it },
                                            label = { Text("WhatsApp API URL (POST or GET)", fontSize = 9.sp) },
                                            placeholder = { Text("https://api.gateway.example/whatsapp-send?to={phone}&msg={message}", fontSize = 9.sp) },
                                            textStyle = TextStyle(fontSize = 10.5.sp),
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                        
                                        OutlinedTextField(
                                            value = editingWaKey,
                                            onValueChange = { editingWaKey = it },
                                            label = { Text("WhatsApp Bearer/API Key", fontSize = 9.sp) },
                                            textStyle = TextStyle(fontSize = 10.5.sp),
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                    }
                                }
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "SIM-based SMS Network Gateway",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        
                                        OutlinedTextField(
                                            value = editingSmsUrl,
                                            onValueChange = { editingSmsUrl = it },
                                            label = { Text("SMS HTTP Gateway URL (POST or GET)", fontSize = 9.sp) },
                                            placeholder = { Text("https://api.gateway.example/sms-send?to={phone}&msg={message}", fontSize = 9.sp) },
                                            textStyle = TextStyle(fontSize = 10.5.sp),
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                        
                                        OutlinedTextField(
                                            value = editingSmsKey,
                                            onValueChange = { editingSmsKey = it },
                                            label = { Text("SMS Network Bearer/API Key", fontSize = 9.sp) },
                                            textStyle = TextStyle(fontSize = 10.5.sp),
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                    }
                                }
                                
                                Text(
                                    text = "💡 Pro Tip: Use placeholders {phone}, {message}, and {key} as part of your endpoints URL to build dynamic GET request links for legacy gateway protocols.",
                                    fontSize = 8.5.sp,
                                    lineHeight = 11.sp,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            
                            Button(
                                onClick = {
                                    viewModel.updateMessagingSettings(
                                        editingMsgMode,
                                        editingWaUrl.trim(),
                                        editingWaKey.trim(),
                                        editingSmsUrl.trim(),
                                        editingSmsKey.trim()
                                    )
                                    android.widget.Toast.makeText(context, "Messaging Gateways Saved Successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Save Gateway Credentials", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 5. About Muhammad Hamza Sabir / One Business Solution
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Royal Crest",
                                        tint = Color.WHITE,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Text(
                                    text = "ONE BUSINESS SOLUTION",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "MADE BY MUHAMMAD HAMZA SABIR",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = "Premium, highly tailored retail business management application containing high-contrast PDF layout engines, real-time stock registers, customer profiling trackers, offline-secure Room storage architectures, multiligual dictionaries support, and easy-install APK sharing mechanism.",
                                    fontSize = 9.sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }

                    // 6. Action Button: APK File Sharing Installer
                    item {
                        Button(
                            onClick = {
                                try {
                                    val sourceApkFile = java.io.File(context.applicationInfo.sourceDir)
                                    val cacheApkFile = java.io.File(context.cacheDir, "One_Solution.apk")
                                    if (!cacheApkFile.exists() || cacheApkFile.length() != sourceApkFile.length()) {
                                        sourceApkFile.inputStream().use { input ->
                                            cacheApkFile.outputStream().use { output ->
                                                input.copyTo(output)
                                            }
                                        }
                                    }
                                    val apkUri = androidx.core.content.FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        cacheApkFile
                                    )
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/vnd.android.package-archive"
                                        putExtra(Intent.EXTRA_STREAM, apkUri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Suite APK Installer File via"))
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Failed to share APK: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.WHITE, modifier = Modifier.size(16.dp))
                                Text("Share Suite APK Installer File", fontWeight = FontWeight.Bold, color = Color.WHITE, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
