package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Client::class,
        StockItem::class,
        Invoice::class,
        InvoiceItem::class,
        InventoryLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BoutiqueDatabase : RoomDatabase() {

    abstract fun clientDao(): ClientDao
    abstract fun stockItemDao(): StockItemDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun invoiceItemDao(): InvoiceItemDao
    abstract fun inventoryLogDao(): InventoryLogDao

    companion object {
        @Volatile
        private var INSTANCE: BoutiqueDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): BoutiqueDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BoutiqueDatabase::class.java,
                    "boutique_database"
                )
                    .addCallback(BoutiqueDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class BoutiqueDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        suspend fun populateInitialData(database: BoutiqueDatabase) {
            val clientDao = database.clientDao()
            val stockItemDao = database.stockItemDao()
            val logDao = database.inventoryLogDao()

            // Prepopulate Clients
            val c1 = Client(name = "Sophia Reynolds", phoneNumber = "+1555019283", email = "sophia@example.com", address = "742 Evergreen Terrace")
            val c2 = Client(name = "Amara Adebayo", phoneNumber = "+447700900077", email = "amara@example.com", address = "12 Baker St")
            val c3 = Client(name = "Isabella Martinez", phoneNumber = "+1555024832", email = "isabella@example.com", address = "101 Ocean Drive")
            
            clientDao.insertClient(c1)
            clientDao.insertClient(c2)
            clientDao.insertClient(c3)

            // Prepopulate Stock Items (Cosmetics, Shoes & Sandals, Perfumes, Children's Clothing)
            val items = listOf(
                // Cosmetics
                StockItem(name = "Silk Touch Hydrating Lipstick", category = "Cosmetics", price = 22.00, stockQuantity = 15, lowStockThreshold = 4, description = "Long-lasting velvety hydrating lipstick in crimson blush."),
                StockItem(name = "Matte Foundation Rose Ivory", category = "Cosmetics", price = 35.00, stockQuantity = 3, lowStockThreshold = 5, description = "Flawless coverage matte foundation with SPF 15."),
                StockItem(name = "Glaze Pearl Liquid Eyeshadow", category = "Cosmetics", price = 26.00, stockQuantity = 8, lowStockThreshold = 2, description = "Highly pigmented rose gold metallic shimmering eyeshadow."),
                
                // Shoes & Sandals
                StockItem(name = "Stella Champagne Mesh Heels", category = "Shoes & Sandals", price = 115.00, stockQuantity = 12, lowStockThreshold = 3, description = "Gorgeous 3-inch champagne stiletto heels with rhinestone straps."),
                StockItem(name = "Isla Pearl White Bridal Heel", category = "Shoes & Sandals", price = 130.00, stockQuantity = 4, lowStockThreshold = 5, description = "Silk closed-toe court pump heels adorned with fresh-water pearls."),
                StockItem(name = "Suede Meadow Slide Sandals", category = "Shoes & Sandals", price = 65.00, stockQuantity = 1, lowStockThreshold = 3, description = "Comfortable soft premium lavender suede slides with memory insoles."),
                
                // Perfumes
                StockItem(name = "Nectar Bloom Eau de Parfum", category = "Perfumes", price = 95.00, stockQuantity = 20, lowStockThreshold = 5, description = "Delightful feminine scent featuring sweet peony and white musk notes."),
                StockItem(name = "Royal Oud Luxury Perfume Oil", category = "Perfumes", price = 150.00, stockQuantity = 2, lowStockThreshold = 3, description = "Deep mystical Unisex Cambodian Oud mixed with damask rose extracts."),
                
                // Children's Clothing
                StockItem(name = "Floral Ruffle Cotton Kids Dress", category = "Children's Clothing", price = 48.00, stockQuantity = 10, lowStockThreshold = 3, description = "100% fine cotton vintage-styled rose floral print dress for ages 2-6."),
                StockItem(name = "Linen Suspenders Infant Set", category = "Children's Clothing", price = 52.00, stockQuantity = 2, lowStockThreshold = 4, description = "Charming beige organic linen shorts and white collared shirt set.")
            )

            for (item in items) {
                val id = stockItemDao.insertStockItem(item)
                logDao.insertLog(
                    InventoryLog(
                        itemId = id.toInt(),
                        itemName = item.name,
                        category = item.category,
                        actionType = "STOCK_INITIALIZED",
                        quantityChanged = item.stockQuantity,
                        reorderQuantity = item.stockQuantity,
                        details = "Initial setup of boutique stock: ${item.name} (${item.stockQuantity} units)"
                    )
                )
            }
        }
    }
}
