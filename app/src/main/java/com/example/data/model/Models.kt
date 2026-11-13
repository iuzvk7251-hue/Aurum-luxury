package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val phone: String = "",
    val shippingAddress: String = "",
    val role: String = "customer",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Product(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "Watches",
    val price: Double = 0.0,
    val compareAtPrice: Double = 0.0,
    val stock: Int = 0,
    val imageKey: String = "watch",
    val rating: Double = 4.9,
    val reviewCount: Int = 0,
    val supplierId: String = "zendrop",
    val supplierName: String = "Zendrop",
    val supplierTag: String = "",
    val shippingEstimate: String = "4-7 Business Days",
    val isBestSeller: Boolean = false,
    val supportsPrintOnDemand: Boolean = false,
    val supportsSampleOrder: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CartItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val title: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val imageKey: String = "watch",
    val selectedVariant: String = "Obsidian / Gold",
    val printOnDemandNote: String = "",
    val supplierId: String = "zendrop",
    val supplierName: String = "Zendrop",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class WishlistItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val title: String = "",
    val price: Double = 0.0,
    val imageKey: String = "watch",
    val supplierName: String = "Zendrop",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Order(
    @DocumentId val id: String = "",
    val userId: String = "",
    val customerName: String = "",
    val customerEmail: String = "",
    val customerPhone: String = "",
    val shippingAddress: String = "",
    val itemsSummary: String = "",
    val itemCount: Int = 1,
    val subtotal: Double = 0.0,
    val shippingCost: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "Card (Visa / MasterCard)",
    val paymentStatus: String = "Paid",
    val orderStatus: String = "Pending",
    val supplierId: String = "zendrop",
    val supplierName: String = "Zendrop",
    val forwardingStatus: String = "Queued",
    val trackingNumber: String = "",
    val carrier: String = "",
    val notificationSent: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Supplier(
    @DocumentId val id: String = "",
    val name: String = "",
    val code: String = "",
    val apiKeyMasked: String = "",
    val isConnected: Boolean = false,
    val autoImport: Boolean = true,
    val autoPriceSync: Boolean = true,
    val autoStockSync: Boolean = true,
    val autoOrderForwarding: Boolean = true,
    val autoTrackingSync: Boolean = true,
    val specialNote: String = "",
    val importedProductCount: Int = 0,
    val isCustom: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SourcingRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val supplierId: String = "",
    val supplierName: String = "",
    val requestType: String = "Sourcing Request",
    val productTitle: String = "",
    val notes: String = "",
    val status: String = "Submitted",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SupportTicket(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val subject: String = "",
    val message: String = "",
    val status: String = "Open",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ProductReview(
    @DocumentId val id: String = "",
    val productId: String = "",
    val userId: String = "",
    val authorName: String = "",
    val rating: Int = 5,
    val comment: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
