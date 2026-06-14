package com.example.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.Invoice
import com.example.data.InvoiceItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    // 12 Premium Invoice Themes for the App
    data class InvoiceTheme(
        val primaryColorHex: String,
        val secondaryColorHex: String,
        val bannerBgColorHex: String,
        val pageBgColorHex: String,
        val isPageBgDark: Boolean,
        val isHeaderDark: Boolean,
        val typefaceType: Typeface,
        val accentStyle: String,
        val tagline: String
    )

    fun getThemeById(layoutId: Int): InvoiceTheme {
        return when (layoutId) {
            1 -> InvoiceTheme(
                primaryColorHex = "#1B4D3E", // Classic Emerald Forest
                secondaryColorHex = "#4C7B6B",
                bannerBgColorHex = "#EBF2EE", // Soft mint
                pageBgColorHex = "#FFFFFF",
                isPageBgDark = false,
                isHeaderDark = false,
                typefaceType = Typeface.create(Typeface.SERIF, Typeface.NORMAL),
                accentStyle = "Classic",
                tagline = "Classic Emerald - Premium Styling Heritage"
            )
            2 -> InvoiceTheme(
                primaryColorHex = "#8E4162", // Vintage Soft Rose
                secondaryColorHex = "#C38D9E",
                bannerBgColorHex = "#FDF6F8", // Blush cream
                pageBgColorHex = "#FFFFFF",
                isPageBgDark = false,
                isHeaderDark = false,
                typefaceType = Typeface.create(Typeface.SERIF, Typeface.NORMAL),
                accentStyle = "Vintage",
                tagline = "Vintage Rose - Handmade Craft & Charm Aesthetics"
            )
            3 -> InvoiceTheme(
                primaryColorHex = "#7A2048", // Royal Velvet Burgundy
                secondaryColorHex = "#B58A30", // Royal Gold
                bannerBgColorHex = "#F9F5F7",
                pageBgColorHex = "#FFFFFF",
                isPageBgDark = false,
                isHeaderDark = false,
                typefaceType = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL),
                accentStyle = "Royal",
                tagline = "Royal Burgundy - Velvet Selection & Haute Curation"
            )
            4 -> InvoiceTheme(
                primaryColorHex = "#1E293B", // Midnight Slate
                secondaryColorHex = "#64748B",
                bannerBgColorHex = "#F1F5F9",
                pageBgColorHex = "#FFFFFF",
                isPageBgDark = false,
                isHeaderDark = false,
                typefaceType = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL),
                accentStyle = "Midnight",
                tagline = "Midnight Slate - Sharp Professional Minimalism"
            )
            5 -> InvoiceTheme(
                primaryColorHex = "#111111", // Golden Luxury (Onyx)
                secondaryColorHex = "#C5A059", // Brass gold
                bannerBgColorHex = "#FAF6F0", // Warm Champagne
                pageBgColorHex = "#FFFFFF",
                isPageBgDark = false,
                isHeaderDark = false,
                typefaceType = Typeface.create(Typeface.SERIF, Typeface.NORMAL),
                accentStyle = "Luxury",
                tagline = "Golden Luxury - Bespoke Couture & High Aesthetics"
            )
            6 -> InvoiceTheme(
                primaryColorHex = "#000000", // Minimalist stark grid
                secondaryColorHex = "#555555",
                bannerBgColorHex = "#F2F2F2",
                pageBgColorHex = "#FFFFFF",
                isPageBgDark = false,
                isHeaderDark = false,
                typefaceType = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL),
                accentStyle = "Minimalist",
                tagline = "Minimalist Clean - stark structural simple contrast"
            )
            7 -> InvoiceTheme(
                primaryColorHex = "#2E5A44", // Botanical Sage Mint
                secondaryColorHex = "#4CA873",
                bannerBgColorHex = "#EAF7EE",
                pageBgColorHex = "#FFFFFF",
                isPageBgDark = false,
                isHeaderDark = false,
                typefaceType = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL),
                accentStyle = "Botanical",
                tagline = "Eco Mint - Botanical Organic Linen Wear"
            )
            8 -> InvoiceTheme(
                primaryColorHex = "#D35400", // Vibrant Peach / Rust
                secondaryColorHex = "#E67E22",
                bannerBgColorHex = "#FDF2E9",
                pageBgColorHex = "#FFFFFF",
                isPageBgDark = false,
                isHeaderDark = false,
                typefaceType = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL),
                accentStyle = "Peach",
                tagline = "Vibrant Peach - Playful Joyful Everyday Accents"
            )
            9 -> InvoiceTheme(
                primaryColorHex = "#1A365D", // Ocean Breeze Navy
                secondaryColorHex = "#319795",
                bannerBgColorHex = "#EBF8FF",
                pageBgColorHex = "#FFFFFF",
                isPageBgDark = false,
                isHeaderDark = false,
                typefaceType = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL),
                accentStyle = "Ocean",
                tagline = "Ocean Breeze - Coastal Textures & Summer Leisurewear"
            )
            10 -> InvoiceTheme(
                primaryColorHex = "#7E4E30", // Retro Terracotta
                secondaryColorHex = "#D4A373",
                bannerBgColorHex = "#FAF0E6",
                pageBgColorHex = "#FFFFFF",
                isPageBgDark = false,
                isHeaderDark = false,
                typefaceType = Typeface.create(Typeface.SERIF, Typeface.NORMAL),
                accentStyle = "Retro",
                tagline = "Retro Chic - Terracotta Warmth & Timeless Heritage"
            )
            11 -> InvoiceTheme(
                primaryColorHex = "#5F4B8B", // Lavender Mist
                secondaryColorHex = "#A29BFE",
                bannerBgColorHex = "#F3EFFF",
                pageBgColorHex = "#FFFFFF",
                isPageBgDark = false,
                isHeaderDark = false,
                typefaceType = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL),
                accentStyle = "Lavender",
                tagline = "Lavender Mist - Ethereal Wear, Scents & Mist Accessories"
            )
            12 -> InvoiceTheme(
                primaryColorHex = "#E0115F", // Cyber Neon Pink
                secondaryColorHex = "#00F5FF", // Neon Cyan
                bannerBgColorHex = "#1D1E22", // Slate Black Header
                pageBgColorHex = "#111215", // Cyber Charcoal
                isPageBgDark = true,
                isHeaderDark = true,
                typefaceType = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL),
                accentStyle = "Neon",
                tagline = "Neon Noir - Avant-Garde Streetwear & Glamour Optics"
            )
            else -> InvoiceTheme(
                primaryColorHex = "#7A2048",
                secondaryColorHex = "#B58A30",
                bannerBgColorHex = "#F9F5F7",
                pageBgColorHex = "#FFFFFF",
                isPageBgDark = false,
                isHeaderDark = false,
                typefaceType = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL),
                accentStyle = "Royal",
                tagline = "Boutique Retail - Premium Selection & Styling"
            )
        }
    }

    fun generateInvoicePdf(
        context: Context,
        invoice: Invoice,
        items: List<InvoiceItem>,
        layoutId: Int = 3
    ): File? {
        val theme = getThemeById(layoutId)

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size (595 x 842 points)
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val textPaint = Paint().apply {
            color = if (theme.isPageBgDark) Color.WHITE else Color.BLACK
            isAntiAlias = true
        }

        // Draw page background
        paint.color = Color.parseColor(theme.pageBgColorHex)
        canvas.drawRect(0f, 0f, 595f, 842f, paint)

        // Draw top banner background accent
        paint.color = Color.parseColor(theme.bannerBgColorHex)
        canvas.drawRect(0f, 0f, 595f, 120f, paint)

        // Draw Branding Header Area / Logo square background
        paint.color = Color.parseColor(theme.primaryColorHex)
        canvas.drawRect(40f, 30f, 90f, 80f, paint)

        // Draw Logo text
        textPaint.apply {
            color = Color.WHITE
            textSize = 20f
            typeface = Typeface.create(theme.typefaceType, Typeface.BOLD)
        }
        val logoText = invoice.businessLogoText.take(2).uppercase()
        canvas.drawText(logoText, 48f, 63f, textPaint)

        // Draw Brand Name
        textPaint.apply {
            color = Color.parseColor(theme.primaryColorHex)
            textSize = 24f
            typeface = Typeface.create(theme.typefaceType, Typeface.BOLD)
        }
        canvas.drawText(invoice.businessName, 105f, 55f, textPaint)

        // Draw Subtitle / Tagline based on active theme
        textPaint.apply {
            color = if (theme.isPageBgDark) Color.LTGRAY else Color.DKGRAY
            textSize = 9f
            typeface = Typeface.create(theme.typefaceType, Typeface.NORMAL)
        }
        val subLine = theme.tagline
        canvas.drawText(subLine, 105f, 72f, textPaint)

        // Draw custom business contact details
        textPaint.apply {
            color = if (theme.isPageBgDark) Color.WHITE else Color.BLACK
            textSize = 9f
            typeface = Typeface.create(theme.typefaceType, Typeface.NORMAL)
        }
        val addressLine = invoice.businessAddress.ifEmpty { "101 Luxury Mall, Fashion Lane" }
        val phoneLine = "Phone: " + invoice.businessPhone.ifEmpty { "+1 555-BOUTIQUE" }
        canvas.drawText(addressLine, 400f, 50f, textPaint)
        canvas.drawText(phoneLine, 400f, 65f, textPaint)

        // Draw Invoice Meta Box Line separator with custom style
        paint.color = Color.parseColor(theme.secondaryColorHex)
        paint.strokeWidth = if (theme.accentStyle == "Classic" || theme.accentStyle == "Royal") 2.5f else 1.2f
        canvas.drawLine(40f, 140f, 555f, 140f, paint)

        if (theme.accentStyle == "Luxury" || theme.accentStyle == "Royal") {
            // Draw luxury double golden accent design element
            paint.color = Color.parseColor(theme.secondaryColorHex)
            canvas.drawRect(40f, 144f, 100f, 146f, paint)
        }

        // Setup text color for labels
        val normalTextColor = if (theme.isPageBgDark) Color.WHITE else Color.BLACK
        val secondaryTextColor = if (theme.isPageBgDark) Color.LTGRAY else Color.GRAY

        textPaint.apply {
            textSize = 15f
            color = Color.parseColor(theme.primaryColorHex)
            typeface = Typeface.create(theme.typefaceType, Typeface.BOLD)
        }
        canvas.drawText("INVOICE", 40f, 170f, textPaint)

        textPaint.apply {
            textSize = 11f
            color = normalTextColor
            typeface = Typeface.create(theme.typefaceType, Typeface.BOLD)
        }
        canvas.drawText("Bill To Customer:", 40f, 200f, textPaint)

        textPaint.apply {
            textSize = 10f
            typeface = Typeface.create(theme.typefaceType, Typeface.NORMAL)
        }
        canvas.drawText("Name: " + invoice.clientName, 40f, 218f, textPaint)
        canvas.drawText("Phone: " + invoice.clientPhone, 40f, 233f, textPaint)

        // Draw invoice specifications right-aligned
        textPaint.apply {
            textSize = 10f
            color = normalTextColor
            typeface = Typeface.create(theme.typefaceType, Typeface.NORMAL)
        }
        val invNoText = "Invoice Number: " + invoice.invoiceNumber
        val invDateText = "Billing Date: " + dateFormat.format(Date(invoice.invoiceDate))
        val invStatusText = "Payment Status: " + invoice.paymentStatus.uppercase()
        val invMethodText = "Payment Mode: " + invoice.paymentMethod

        canvas.drawText(invNoText, 350f, 170f, textPaint)
        canvas.drawText(invDateText, 350f, 185f, textPaint)
        canvas.drawText(invStatusText, 350f, 200f, textPaint)
        canvas.drawText(invMethodText, 350f, 215f, textPaint)

        // Draw Sales Table Headers Bar
        paint.color = Color.parseColor(theme.bannerBgColorHex)
        canvas.drawRect(40f, 260f, 555f, 285f, paint)

        textPaint.apply {
            textSize = 10f
            color = Color.parseColor(theme.primaryColorHex)
            typeface = Typeface.create(theme.typefaceType, Typeface.BOLD)
        }
        canvas.drawText("Item Name", 50f, 277f, textPaint)
        canvas.drawText("Category", 230f, 277f, textPaint)
        canvas.drawText("Unit Price", 360f, 277f, textPaint)
        canvas.drawText("Qty", 442f, 277f, textPaint)
        canvas.drawText("Total ($)", 495f, 277f, textPaint)

        // Draw Table Row Items
        var currentY = 310f
        textPaint.apply {
            color = normalTextColor
            typeface = Typeface.create(theme.typefaceType, Typeface.NORMAL)
        }

        for (item in items) {
            // Draw item line separator
            paint.color = Color.parseColor(if (theme.isPageBgDark) "#2F2F3F" else "#EEE9EC")
            canvas.drawLine(40f, currentY + 8f, 555f, currentY + 8f, paint)

            val maxNameLength = 26
            val truncatedName = if (item.itemName.length > maxNameLength) {
                item.itemName.substring(0, maxNameLength) + "..."
            } else {
                item.itemName
            }

            canvas.drawText(truncatedName, 50f, currentY, textPaint)
            canvas.drawText(item.category, 230f, currentY, textPaint)

            val unitPriceStr = String.format(Locale.US, "$%.2f", item.unitPrice)
            canvas.drawText(unitPriceStr, 360f, currentY, textPaint)
            canvas.drawText(item.quantity.toString(), 442f, currentY, textPaint)

            val totalStr = String.format(Locale.US, "$%.2f", item.totalPrice)
            canvas.drawText(totalStr, 495f, currentY, textPaint)

            currentY += 30f
            if (currentY > 700f) break // page overflow protection
        }

        // Draw Summary Totals Box
        paint.color = Color.parseColor(theme.bannerBgColorHex)
        canvas.drawRect(330f, currentY + 10f, 555f, currentY + 115f, paint)

        textPaint.apply {
            textSize = 10f
            color = normalTextColor
            typeface = Typeface.create(theme.typefaceType, Typeface.NORMAL)
        }
        canvas.drawText("Subtotal:", 350f, currentY + 32f, textPaint)
        canvas.drawText("Discount:", 350f, currentY + 52f, textPaint)
        canvas.drawText("Sales Tax (GST):", 350f, currentY + 72f, textPaint)

        textPaint.apply {
            typeface = Typeface.create(theme.typefaceType, Typeface.BOLD)
            color = Color.parseColor(theme.primaryColorHex)
            textSize = 11f
        }
        canvas.drawText("Grand Total:", 350f, currentY + 98f, textPaint)

        // Draw values right-aligned inside the totals box
        textPaint.apply {
            typeface = Typeface.create(theme.typefaceType, Typeface.NORMAL)
            color = normalTextColor
            textSize = 10f
        }
        val subtotalStr = String.format(Locale.US, "$%.2f", invoice.subtotal)
        val discountStr = String.format(Locale.US, "$%.2f", invoice.discount)
        val taxStr = String.format(Locale.US, "$%.2f", invoice.tax)
        val grandTotalStr = String.format(Locale.US, "$%.2f", invoice.totalAmount)

        canvas.drawText(subtotalStr, 480f, currentY + 32f, textPaint)
        canvas.drawText("-$" + discountStr, 480f, currentY + 52f, textPaint)
        canvas.drawText("+$" + taxStr, 480f, currentY + 72f, textPaint)

        textPaint.apply {
            typeface = Typeface.create(theme.typefaceType, Typeface.BOLD)
            color = Color.parseColor(theme.primaryColorHex)
            textSize = 11f
        }
        canvas.drawText(grandTotalStr, 480f, currentY + 98f, textPaint)

        // Draw Terms Notes / Brand Footer with theme style
        textPaint.apply {
            typeface = Typeface.create(theme.typefaceType, Typeface.BOLD)
            textSize = 9f
            color = Color.parseColor(theme.primaryColorHex)
        }
        val noteHeading = "Receipt Policy Notes & Terms:"
        canvas.drawText(noteHeading, 40f, currentY + 30f, textPaint)

        textPaint.apply {
            typeface = Typeface.create(theme.typefaceType, Typeface.ITALIC)
            textSize = 8f
            color = secondaryTextColor
        }
        val customNote = invoice.notes.ifEmpty { "Thank you for supporting our boutique. Exchange within 7 days." }
        val finalNotes = if (customNote.length > 55) customNote.take(53) + "..." else customNote
        canvas.drawText(finalNotes, 40f, currentY + 45f, textPaint)
        canvas.drawText("Standard 8% Sales Tax (GST) binds layout configurations safely.", 40f, currentY + 58f, textPaint)

        // Bottom Page footer
        paint.color = Color.parseColor(theme.primaryColorHex)
        canvas.drawRect(0f, 810f, 595f, 842f, paint)

        textPaint.apply {
            color = Color.WHITE
            textSize = 10f
            typeface = Typeface.create(theme.typefaceType, Typeface.BOLD)
        }
        val footerText = "Thank you for shopping at " + invoice.businessName
        canvas.drawText(footerText, 160f, 830f, textPaint)

        pdfDocument.finishPage(page)

        // Write file
        val publicDir = context.getExternalFilesDir("Documents")
        if (publicDir != null && !publicDir.exists()) {
            publicDir.mkdirs()
        }
        val invoiceFile = File(publicDir, "BoutiqueInvoice_${invoice.invoiceNumber}.pdf")
        return try {
            val fos = FileOutputStream(invoiceFile)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()
            invoiceFile
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    fun generateReportPdf(
        context: Context,
        reportTitle: String,
        totalSales: Double,
        paidInvoicesCount: Int,
        unpaidInvoicesCount: Int,
        totalInvoicesCount: Int,
        categorySplits: Map<String, Double>,
        invoices: List<Invoice>
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val textPaint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }

        // Title Header
        paint.color = Color.parseColor("#7A2048")
        canvas.drawRect(0f, 0f, 595f, 100f, paint)

        textPaint.apply {
            color = Color.WHITE
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("MONTHLY SALES REPORT", 40f, 50f, textPaint)

        textPaint.apply {
            color = Color.parseColor("#EBDCD5")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }
        canvas.drawText(reportTitle, 40f, 75f, textPaint)

        // Draw Metrics Summary Panel
        paint.color = Color.parseColor("#FAF6F8")
        canvas.drawRect(40f, 120f, 555f, 210f, paint)

        // Grid split for KPI Cards
        paint.color = Color.parseColor("#7A2048")
        paint.strokeWidth = 2f
        canvas.drawLine(200f, 130f, 200f, 200f, paint)
        canvas.drawLine(380f, 130f, 380f, 200f, paint)

        textPaint.apply {
            color = Color.DKGRAY
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        canvas.drawText("TOTAL DIRECT REVENUE", 50f, 145f, textPaint)
        canvas.drawText("PAID BILLS VOLUME", 215f, 145f, textPaint)
        canvas.drawText("PENDING RECEIVABLES", 395f, 145f, textPaint)

        textPaint.apply {
            color = Color.parseColor("#7A2048")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(String.format(Locale.US, "$%.2f", totalSales), 50f, 175f, textPaint)
        canvas.drawText("$paidInvoicesCount / $totalInvoicesCount Bills", 215f, 175f, textPaint)
        canvas.drawText("$unpaidInvoicesCount Unpaid Bills", 395f, 175f, textPaint)

        // Draw Category Breakdown
        textPaint.apply {
            color = Color.BLACK
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("Boutique Departmental Breakdown", 40f, 240f, textPaint)

        paint.color = Color.parseColor("#7A2048")
        paint.strokeWidth = 1f
        canvas.drawLine(40f, 248f, 555f, 248f, paint)

        var splitY = 275f
        textPaint.apply {
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val categories = listOf("Cosmetics", "Shoes & Sandals", "Perfumes", "Children's Clothing")
        for (cat in categories) {
            val totalCatSale = categorySplits[cat] ?: 0.0
            
            // Draw small styled bar indicator
            paint.color = Color.parseColor("#EDE6EA")
            canvas.drawRect(200f, splitY - 8f, 450f, splitY + 2f, paint)
            
            val maxRange = (categorySplits.values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
            val barPercentage = (totalCatSale / maxRange).toFloat()
            val drawWidth = 250f * barPercentage
            paint.color = Color.parseColor("#7A2048")
            canvas.drawRect(200f, splitY - 8f, 200f + drawWidth, splitY + 2f, paint)

            textPaint.color = Color.BLACK
            canvas.drawText(cat, 50f, splitY, textPaint)
            
            textPaint.apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.parseColor("#7A2048")
            }
            canvas.drawText(String.format(Locale.US, "$%.2f", totalCatSale), 470f, splitY, textPaint)
            textPaint.apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                color = Color.BLACK
            }

            splitY += 22f
        }

        // Draw Recent Invoices Table
        textPaint.apply {
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("Audit Log of Key Invoices Listed in Range", 40f, 390f, textPaint)

        paint.color = Color.parseColor("#EDE6EA")
        canvas.drawRect(40f, 405f, 555f, 428f, paint)

        textPaint.apply {
            textSize = 9f
            color = Color.parseColor("#7A2048")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("Invoice No.", 45f, 420f, textPaint)
        canvas.drawText("Client Name", 150f, 420f, textPaint)
        canvas.drawText("Date", 280f, 420f, textPaint)
        canvas.drawText("Status", 390f, 420f, textPaint)
        canvas.drawText("Amount ($)", 490f, 420f, textPaint)

        var itemY = 445f
        textPaint.apply {
            textSize = 9f
            color = Color.BLACK
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val limitInvoices = invoices.take(10) // fit in single page
        for (inv in limitInvoices) {
            paint.color = Color.parseColor("#F4F2F3")
            canvas.drawLine(40f, itemY + 6f, 555f, itemY + 6f, paint)

            canvas.drawText(inv.invoiceNumber, 45f, itemY, textPaint)
            val clientTruncated = if (inv.clientName.length > 18) inv.clientName.substring(0, 16) + ".." else inv.clientName
            canvas.drawText(clientTruncated, 150f, itemY, textPaint)
            
            val formattedDate = SimpleDateFormat("dd MMM, yy", Locale.getDefault()).format(Date(inv.invoiceDate))
            canvas.drawText(formattedDate, 280f, itemY, textPaint)
            
            // Render beautiful rounded badge for status
            val badgeColor = when (inv.paymentStatus.lowercase()) {
                "paid" -> Color.parseColor("#4BB543")
                "unpaid" -> Color.parseColor("#D9534F")
                else -> Color.parseColor("#F0AD4E")
            }
            paint.color = badgeColor
            canvas.drawRect(390f, itemY - 8f, 440f, itemY + 2f, paint)

            textPaint.color = Color.WHITE
            canvas.drawText(inv.paymentStatus.uppercase(), 395f, itemY - 1f, textPaint)

            textPaint.color = Color.BLACK
            canvas.drawText(String.format(Locale.US, "$%.2f", inv.totalAmount), 490f, itemY, textPaint)

            itemY += 24f
            if (itemY > 780f) break
        }

        // Draw Footer
        paint.color = Color.parseColor("#7A2048")
        canvas.drawRect(0f, 810f, 595f, 842f, paint)

        textPaint.apply {
            color = Color.WHITE
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val genTimeText = "Report generated automatically on " + SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        canvas.drawText(genTimeText, 140f, 830f, textPaint)

        pdfDocument.finishPage(page)

        val publicDir = context.getExternalFilesDir("Documents")
        if (publicDir != null && !publicDir.exists()) {
            publicDir.mkdirs()
        }
        val reportFile = File(publicDir, "BoutiqueSalesReport.pdf")
        return try {
            val fos = FileOutputStream(reportFile)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()
            reportFile
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}
