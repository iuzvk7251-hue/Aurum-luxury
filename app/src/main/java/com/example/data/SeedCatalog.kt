package com.example.data

import com.example.data.model.Product
import com.example.data.model.ProductReview
import com.example.data.model.Supplier

object SeedCatalog {

    val defaultSuppliers: List<Supplier> = listOf(
        Supplier(
            id = "cj_dropshipping",
            name = "CJ Dropshipping",
            code = "CJ",
            apiKeyMasked = "",
            isConnected = false,
            autoImport = true,
            autoPriceSync = true,
            autoStockSync = true,
            autoOrderForwarding = true,
            autoTrackingSync = true,
            specialNote = "Print on Demand + Custom Sourcing Requests",
            importedProductCount = 0,
            isCustom = false
        ),
        Supplier(
            id = "zendrop",
            name = "Zendrop",
            code = "ZENDROP",
            apiKeyMasked = "",
            isConnected = false,
            autoImport = true,
            autoPriceSync = true,
            autoStockSync = true,
            autoOrderForwarding = true,
            autoTrackingSync = true,
            specialNote = "US Warehouse • Fast Shipping 4-7 days",
            importedProductCount = 0,
            isCustom = false
        ),
        Supplier(
            id = "spocket",
            name = "Spocket",
            code = "SPOCKET",
            apiKeyMasked = "",
            isConnected = false,
            autoImport = true,
            autoPriceSync = true,
            autoStockSync = true,
            autoOrderForwarding = true,
            autoTrackingSync = true,
            specialNote = "US / EU Supplier • Premium Quality • Sample Order",
            importedProductCount = 0,
            isCustom = false
        ),
        Supplier(
            id = "aliexpress_dsers",
            name = "AliExpress via DSers",
            code = "DSERS",
            apiKeyMasked = "",
            isConnected = false,
            autoImport = true,
            autoPriceSync = true,
            autoStockSync = true,
            autoOrderForwarding = true,
            autoTrackingSync = true,
            specialNote = "Automated Bulk Mapping & Order Dispatch",
            importedProductCount = 0,
            isCustom = false
        ),
        Supplier(
            id = "alibaba",
            name = "Alibaba",
            code = "ALIBABA",
            apiKeyMasked = "",
            isConnected = false,
            autoImport = true,
            autoPriceSync = true,
            autoStockSync = true,
            autoOrderForwarding = true,
            autoTrackingSync = true,
            specialNote = "Direct Manufacturer Wholesale & Private Label",
            importedProductCount = 0,
            isCustom = false
        )
    )

    val flagshipProducts: List<Product> = listOf(
        Product(
            id = "prod_aurum_watch",
            title = "Aurum Apex Titanium Watch",
            description = "Precision-milled Grade 5 matte black titanium case with brushed 24K gold bezel, sapphire crystal glass, 14-day battery life, and biometric heart-rate tracking. Built for US luxury performance.",
            category = "Watches",
            price = 349.00,
            compareAtPrice = 449.00,
            stock = 24,
            imageKey = "watch",
            rating = 4.9,
            reviewCount = 342,
            supplierId = "zendrop",
            supplierName = "Zendrop",
            supplierTag = "US Warehouse • Fast Shipping 4-7 days",
            shippingEstimate = "4-7 Days (US Warehouse)",
            isBestSeller = true,
            supportsPrintOnDemand = false,
            supportsSampleOrder = false
        ),
        Product(
            id = "prod_aurum_audio",
            title = "Aurum Studio Pro Headphones",
            description = "Flagship acoustic engineering with adaptive noise cancellation, 45-hour spatial audio playback, memory-foam lambskin earcups, and champagne gold anodized aluminum hinges.",
            category = "Audio",
            price = 289.00,
            compareAtPrice = 379.00,
            stock = 18,
            imageKey = "audio",
            rating = 4.9,
            reviewCount = 219,
            supplierId = "spocket",
            supplierName = "Spocket",
            supplierTag = "US / EU Supplier • Premium Quality",
            shippingEstimate = "3-5 Days (US / EU Express)",
            isBestSeller = true,
            supportsPrintOnDemand = false,
            supportsSampleOrder = true
        ),
        Product(
            id = "prod_aurum_sneaker",
            title = "Vanguard Carbon Runner",
            description = "Full-grain Italian white leather paired with obsidian ballistic mesh and 24K metallic gold heel stabilizer. Engineered carbon-plate sole for effortless all-day American street comfort.",
            category = "Footwear",
            price = 215.00,
            compareAtPrice = 265.00,
            stock = 32,
            imageKey = "sneaker",
            rating = 4.8,
            reviewCount = 415,
            supplierId = "cj_dropshipping",
            supplierName = "CJ Dropshipping",
            supplierTag = "Print on Demand • Custom Monogram",
            shippingEstimate = "5-8 Business Days",
            isBestSeller = true,
            supportsPrintOnDemand = true,
            supportsSampleOrder = false
        ),
        Product(
            id = "prod_zendrop_chronograph",
            title = "Fifth Ave Gold Chrono",
            description = "Minimalist architectural dress timepiece featuring brushed gold indices, anti-reflective mineral glass, and quick-release black fluoroelastomer strap.",
            category = "Watches",
            price = 195.00,
            compareAtPrice = 249.00,
            stock = 14,
            imageKey = "watch",
            rating = 4.8,
            reviewCount = 128,
            supplierId = "zendrop",
            supplierName = "Zendrop",
            supplierTag = "US Warehouse • Fast Shipping 4-7 days",
            shippingEstimate = "4-7 Days (US Warehouse)",
            isBestSeller = false,
            supportsPrintOnDemand = false,
            supportsSampleOrder = false
        ),
        Product(
            id = "prod_spocket_earstudio",
            title = "Monaco Acoustic Wireless Buds",
            description = "European-tuned lossless wireless audio with brushed gold charging vault, IPX5 sweat resistance, and crystal-clear dual-beamforming microphones.",
            category = "Audio",
            price = 159.00,
            compareAtPrice = 199.00,
            stock = 9,
            imageKey = "audio",
            rating = 4.7,
            reviewCount = 94,
            supplierId = "spocket",
            supplierName = "Spocket",
            supplierTag = "US / EU Supplier • Premium Quality",
            shippingEstimate = "3-6 Days (US / EU)",
            isBestSeller = false,
            supportsPrintOnDemand = false,
            supportsSampleOrder = true
        ),
        Product(
            id = "prod_cj_custom_trainer",
            title = "Sovereign Custom Street Low",
            description = "Hand-finished court sneaker with optional Print-on-Demand custom heel monogramming, gold lace aglets, and ultra-soft CloudFoam insole.",
            category = "Footwear",
            price = 179.00,
            compareAtPrice = 220.00,
            stock = 0,
            imageKey = "sneaker",
            rating = 4.8,
            reviewCount = 76,
            supplierId = "cj_dropshipping",
            supplierName = "CJ Dropshipping",
            supplierTag = "Print on Demand • Sourcing Ready",
            shippingEstimate = "6-9 Business Days",
            isBestSeller = false,
            supportsPrintOnDemand = true,
            supportsSampleOrder = false
        )
    )

    fun importedProductsForSupplier(supplierId: String, supplierName: String): List<Product> {
        return when (supplierId) {
            "cj_dropshipping" -> listOf(
                Product(
                    id = "imp_cj_pod_1",
                    title = "CJ Custom Monogram Court Sneaker",
                    description = "Imported via CJ Dropshipping. Full Print-on-Demand personalization with gold foil custom text on heel tab.",
                    category = "Footwear",
                    price = 165.00,
                    compareAtPrice = 210.00,
                    stock = 40,
                    imageKey = "sneaker",
                    rating = 4.8,
                    reviewCount = 64,
                    supplierId = supplierId,
                    supplierName = supplierName,
                    supplierTag = "Print on Demand • Sourcing Available",
                    shippingEstimate = "5-8 Days",
                    isBestSeller = true,
                    supportsPrintOnDemand = true,
                    supportsSampleOrder = false
                )
            )
            "zendrop" -> listOf(
                Product(
                    id = "imp_zd_us_1",
                    title = "Zendrop US-Stock Titan Smartwatch",
                    description = "Automatically imported from Zendrop US East/West Coast fulfillment centers for guaranteed 4-7 day domestic delivery.",
                    category = "Watches",
                    price = 269.00,
                    compareAtPrice = 340.00,
                    stock = 35,
                    imageKey = "watch",
                    rating = 4.9,
                    reviewCount = 112,
                    supplierId = supplierId,
                    supplierName = supplierName,
                    supplierTag = "US Warehouse • Fast Shipping 4-7 days",
                    shippingEstimate = "4-7 Days (US Warehouse)",
                    isBestSeller = true,
                    supportsPrintOnDemand = false,
                    supportsSampleOrder = false
                )
            )
            "spocket" -> listOf(
                Product(
                    id = "imp_sp_eu_1",
                    title = "Spocket Artisan Studio Headphones",
                    description = "Curated US/EU boutique audio craftsmanship. Eligible for instant merchant Sample Orders prior to scale.",
                    category = "Audio",
                    price = 249.00,
                    compareAtPrice = 310.00,
                    stock = 22,
                    imageKey = "audio",
                    rating = 4.9,
                    reviewCount = 87,
                    supplierId = supplierId,
                    supplierName = supplierName,
                    supplierTag = "US / EU Supplier • Premium Quality",
                    shippingEstimate = "3-5 Days (US / EU)",
                    isBestSeller = true,
                    supportsPrintOnDemand = false,
                    supportsSampleOrder = true
                )
            )
            "aliexpress_dsers" -> listOf(
                Product(
                    id = "imp_ds_1",
                    title = "DSers Stealth Pro Fitness Chrono",
                    description = "Synced via AliExpress DSers API with real-time stock & dynamic price protection.",
                    category = "Watches",
                    price = 139.00,
                    compareAtPrice = 189.00,
                    stock = 55,
                    imageKey = "watch",
                    rating = 4.7,
                    reviewCount = 204,
                    supplierId = supplierId,
                    supplierName = supplierName,
                    supplierTag = "DSers Auto-Synced",
                    shippingEstimate = "7-12 Business Days",
                    isBestSeller = false,
                    supportsPrintOnDemand = false,
                    supportsSampleOrder = false
                )
            )
            "alibaba" -> listOf(
                Product(
                    id = "imp_ali_1",
                    title = "Alibaba Executive Gold Series Headset",
                    description = "Factory-direct wholesale studio audio hardware imported from verified Alibaba Gold Supplier.",
                    category = "Audio",
                    price = 185.00,
                    compareAtPrice = 260.00,
                    stock = 80,
                    imageKey = "audio",
                    rating = 4.8,
                    reviewCount = 150,
                    supplierId = supplierId,
                    supplierName = supplierName,
                    supplierTag = "Factory Direct Wholesale",
                    shippingEstimate = "6-10 Business Days",
                    isBestSeller = false,
                    supportsPrintOnDemand = false,
                    supportsSampleOrder = true
                )
            )
            else -> listOf(
                Product(
                    id = "imp_${supplierId}_1",
                    title = "$supplierName Signature Edition",
                    description = "Auto-imported product from $supplierName with live price and inventory synchronization.",
                    category = "Accessories",
                    price = 149.00,
                    compareAtPrice = 199.00,
                    stock = 30,
                    imageKey = "hero",
                    rating = 4.8,
                    reviewCount = 19,
                    supplierId = supplierId,
                    supplierName = supplierName,
                    supplierTag = "Verified Partner Feed",
                    shippingEstimate = "5-8 Business Days",
                    isBestSeller = false,
                    supportsPrintOnDemand = false,
                    supportsSampleOrder = false
                )
            )
        }
    }

    val flagshipReviews: List<ProductReview> = listOf(
        ProductReview(
            id = "rev_1",
            productId = "prod_aurum_watch",
            userId = "seed_user_1",
            authorName = "Marcus Sterling — New York, NY",
            rating = 5,
            comment = "Build quality rivals $900 flagship watches. Large crisp screen and arrived from the US warehouse in 4 days."
        ),
        ProductReview(
            id = "rev_2",
            productId = "prod_aurum_audio",
            userId = "seed_user_2",
            authorName = "Elena Vance — Los Angeles, CA",
            rating = 5,
            comment = "Pure obsidian and champagne gold finish looks stunning. Noise cancellation on flights is incredible."
        ),
        ProductReview(
            id = "rev_3",
            productId = "prod_aurum_sneaker",
            userId = "seed_user_3",
            authorName = "Devon Brooks — Chicago, IL",
            rating = 5,
            comment = "Comfort is unreal and the custom gold heel monogram from CJ Print-on-Demand came out razor sharp."
        )
    )
}
