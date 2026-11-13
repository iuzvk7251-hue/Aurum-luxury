package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SeedCatalog
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.ProductReview
import com.example.data.model.SourcingRequest
import com.example.data.model.Supplier
import com.example.data.model.SupportTicket
import com.example.data.model.UserProfile
import com.example.data.model.WishlistItem
import com.example.data.repository.StoreRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OrderNotificationSummary(
    val orderId: String,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String,
    val totalAmount: Double,
    val paymentMethod: String,
    val supplierName: String,
    val autoForwarded: Boolean,
    val adminEmail: String = "iuzvk7251@gmail.com",
    val adminWhatsApp: String = "+18005550199"
)

class StoreViewModel(
    private val repository: StoreRepository,
    val currentUserId: String,
    val userEmail: String,
    val userDisplayName: String
) : ViewModel() {

    val isAdmin: Boolean = userEmail.equals("iuzvk7251@gmail.com", ignoreCase = true)

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _lastOrderNotification = MutableStateFlow<OrderNotificationSummary?>(null)
    val lastOrderNotification: StateFlow<OrderNotificationSummary?> = _lastOrderNotification.asStateFlow()

    private val _localPreviewProducts = MutableStateFlow(SeedCatalog.flagshipProducts)
    private val _localPreviewSuppliers = MutableStateFlow(SeedCatalog.defaultSuppliers)

    val userProfile: StateFlow<UserProfile?> = repository.observeUserProfile(currentUserId)
        .catch { error ->
            unwrapAndLog("userProfile", error)
            emit(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val products: StateFlow<List<Product>> = combine(
        repository.observeProducts().catch { error ->
            unwrapAndLog("products", error)
            emit(emptyList())
        },
        _localPreviewProducts
    ) { remote, localFallback ->
        if (remote.isNotEmpty()) {
            remote.sortedByDescending { it.isBestSeller }
        } else {
            localFallback
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SeedCatalog.flagshipProducts)

    val cartItems: StateFlow<List<CartItem>> = repository.observeCart(currentUserId)
        .catch { error ->
            unwrapAndLog("cart", error)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val wishlistItems: StateFlow<List<WishlistItem>> = repository.observeWishlist(currentUserId)
        .catch { error ->
            unwrapAndLog("wishlist", error)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val userOrders: StateFlow<List<Order>> = repository.observeUserOrders(currentUserId)
        .map { list -> list.sortedByDescending { it.createdAt?.seconds ?: Long.MAX_VALUE } }
        .catch { error ->
            unwrapAndLog("userOrders", error)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val allOrders: StateFlow<List<Order>> = if (isAdmin) {
        repository.observeAllOrders()
            .map { list -> list.sortedByDescending { it.createdAt?.seconds ?: Long.MAX_VALUE } }
            .catch { error ->
                unwrapAndLog("allOrders", error)
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())
    } else {
        userOrders
    }

    val suppliers: StateFlow<List<Supplier>> = if (isAdmin) {
        combine(
            repository.observeSuppliers().catch { error ->
                unwrapAndLog("suppliers", error)
                emit(emptyList())
            },
            _localPreviewSuppliers
        ) { remote, local ->
            if (remote.isNotEmpty()) remote else local
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SeedCatalog.defaultSuppliers)
    } else {
        _localPreviewSuppliers.asStateFlow()
    }

    val customers: StateFlow<List<UserProfile>> = if (isAdmin) {
        repository.observeAllCustomers()
            .catch { error ->
                unwrapAndLog("customers", error)
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())
    } else {
        MutableStateFlow(emptyList<UserProfile>()).asStateFlow()
    }

    val sourcingRequests: StateFlow<List<SourcingRequest>> = repository.observeSourcingRequests(currentUserId, isAdmin)
        .catch { error ->
            unwrapAndLog("sourcingRequests", error)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val reviews: StateFlow<List<ProductReview>> = repository.observeReviews()
        .map { remote ->
            if (remote.isEmpty()) SeedCatalog.flagshipReviews
            else (remote + SeedCatalog.flagshipReviews).distinctBy { it.id }
        }
        .catch { error ->
            unwrapAndLog("reviews", error)
            emit(SeedCatalog.flagshipReviews)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SeedCatalog.flagshipReviews)

    init {
        viewModelScope.launch {
            repository.ensureUserProfile(
                userId = currentUserId,
                displayName = userDisplayName,
                email = userEmail
            )
            if (isAdmin) {
                seedInitialAdminCatalogIfNeeded()
            }
        }
    }

    private suspend fun seedInitialAdminCatalogIfNeeded() {
        // Seed flagship products and default 5 suppliers once into Firestore for the store owner
        try {
            val currentSuppliers = suppliers.value
            if (currentSuppliers.all { it.createdAt == null }) {
                SeedCatalog.defaultSuppliers.forEach { repository.upsertSupplier(it) }
            }
            val currentProducts = products.value
            if (currentProducts.all { it.createdAt == null }) {
                SeedCatalog.flagshipProducts.forEach { repository.upsertProduct(it) }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Initial admin seed skipped or deferred: ${e.message}")
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun dismissOrderNotification() {
        _lastOrderNotification.value = null
    }

    fun updateProfile(displayName: String, phone: String, shippingAddress: String) {
        viewModelScope.launch {
            val res = repository.updateUserProfile(currentUserId, displayName, phone, shippingAddress)
            _toastMessage.value = if (res.isSuccess) "Account profile saved" else "Could not update profile"
        }
    }

    fun addToCart(
        product: Product,
        quantity: Int = 1,
        selectedVariant: String = "Obsidian / Gold",
        printOnDemandNote: String = ""
    ) {
        if (product.stock <= 0) {
            _toastMessage.value = "${product.title} is currently Out of Stock"
            return
        }
        viewModelScope.launch {
            val item = CartItem(
                userId = currentUserId,
                productId = product.id,
                title = product.title,
                price = product.price,
                quantity = quantity,
                imageKey = product.imageKey,
                selectedVariant = selectedVariant,
                printOnDemandNote = printOnDemandNote,
                supplierId = product.supplierId,
                supplierName = product.supplierName
            )
            val res = repository.addToCart(currentUserId, item)
            _toastMessage.value = if (res.isSuccess) "Added ${product.title} to Bag" else "Failed to add to Bag"
        }
    }

    fun updateCartQuantity(cartItemId: String, newQty: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(currentUserId, cartItemId, newQty)
        }
    }

    fun toggleWishlist(product: Product) {
        viewModelScope.launch {
            val res = repository.toggleWishlist(currentUserId, product)
            res.onSuccess { added ->
                _toastMessage.value = if (added) "Saved to Wishlist" else "Removed from Wishlist"
            }
        }
    }

    fun placeCheckoutOrder(
        customerName: String,
        customerEmail: String,
        customerPhone: String,
        shippingAddress: String,
        paymentMethod: String,
        onComplete: () -> Unit
    ) {
        val items = cartItems.value
        if (items.isEmpty()) {
            _toastMessage.value = "Your shopping bag is empty"
            return
        }
        val subtotal = items.sumOf { it.price * it.quantity }
        val shippingCost = if (subtotal >= 150.0) 0.0 else 12.0
        val total = subtotal + shippingCost
        val summary = items.joinToString(", ") {
            val pod = if (it.printOnDemandNote.isNotBlank()) " [POD: ${it.printOnDemandNote}]" else ""
            "${it.quantity}x ${it.title} (${it.selectedVariant})$pod"
        }
        val primarySupplier = items.first()
        val matchedSupplier = suppliers.value.firstOrNull { it.id == primarySupplier.supplierId }
        val autoForwardActive = matchedSupplier?.isConnected == true && matchedSupplier.autoOrderForwarding

        val paymentStatus = if (paymentMethod == "Cash on Delivery") "Pending COD" else "Paid"

        viewModelScope.launch {
            val order = Order(
                userId = currentUserId,
                customerName = customerName,
                customerEmail = customerEmail,
                customerPhone = customerPhone,
                shippingAddress = shippingAddress,
                itemsSummary = summary,
                itemCount = items.sumOf { it.quantity },
                subtotal = subtotal,
                shippingCost = shippingCost,
                totalAmount = total,
                paymentMethod = paymentMethod,
                paymentStatus = paymentStatus,
                orderStatus = "Pending",
                supplierId = primarySupplier.supplierId,
                supplierName = primarySupplier.supplierName,
                forwardingStatus = "Queued",
                notificationSent = true
            )
            val res = repository.placeOrder(order)
            res.onSuccess { orderId ->
                repository.clearCart(currentUserId)
                // If admin & supplier has Auto Order Forwarding active, automatically forward to supplier
                if (isAdmin && autoForwardActive) {
                    repository.adminUpdateOrderFulfillment(
                        orderId = orderId,
                        orderStatus = "Forwarded to Supplier",
                        forwardingStatus = "Forwarded",
                        trackingNumber = "",
                        carrier = if (primarySupplier.supplierId == "zendrop") "USPS Priority" else "UPS Express"
                    )
                }
                _lastOrderNotification.value = OrderNotificationSummary(
                    orderId = orderId,
                    customerName = customerName,
                    customerEmail = customerEmail,
                    customerPhone = customerPhone,
                    totalAmount = total,
                    paymentMethod = paymentMethod,
                    supplierName = primarySupplier.supplierName,
                    autoForwarded = autoForwardActive
                )
                onComplete()
            }.onFailure {
                _toastMessage.value = "Order could not be placed: ${it.localizedMessage}"
            }
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            val res = repository.cancelOrder(orderId)
            _toastMessage.value = if (res.isSuccess) "Order $orderId cancelled" else "Cannot cancel order"
        }
    }

    fun submitReview(productId: String, rating: Int, comment: String) {
        if (comment.isBlank()) return
        viewModelScope.launch {
            val res = repository.submitReview(
                ProductReview(
                    productId = productId,
                    userId = currentUserId,
                    authorName = userDisplayName.ifBlank { "Verified Shopper" },
                    rating = rating,
                    comment = comment.trim()
                )
            )
            _toastMessage.value = if (res.isSuccess) "Review published" else "Could not submit review"
        }
    }

    fun submitSourcingOrSampleRequest(
        supplierId: String,
        supplierName: String,
        requestType: String,
        productTitle: String,
        notes: String
    ) {
        viewModelScope.launch {
            val res = repository.submitSourcingRequest(
                SourcingRequest(
                    userId = currentUserId,
                    supplierId = supplierId,
                    supplierName = supplierName,
                    requestType = requestType,
                    productTitle = productTitle,
                    notes = notes
                )
            )
            _toastMessage.value = if (res.isSuccess) {
                "$requestType sent to $supplierName"
            } else {
                "Failed to submit $requestType"
            }
        }
    }

    fun submitSupportTicket(name: String, email: String, subject: String, message: String) {
        if (subject.isBlank() || message.isBlank()) {
            _toastMessage.value = "Please enter subject and message"
            return
        }
        viewModelScope.launch {
            val res = repository.submitSupportTicket(
                SupportTicket(
                    userId = currentUserId,
                    name = name,
                    email = email,
                    subject = subject,
                    message = message
                )
            )
            _toastMessage.value = if (res.isSuccess) {
                "Support request received. We'll reply via email & WhatsApp."
            } else {
                "Could not send message"
            }
        }
    }

    // --- Dropshipping Admin Operations ---
    fun connectSupplierWithApiKey(supplier: Supplier, apiKey: String) {
        val cleanKey = apiKey.trim()
        if (cleanKey.length < 6) {
            _toastMessage.value = "Please paste a valid API Key (6+ characters)"
            return
        }
        val masked = "${supplier.code.lowercase()}_****${cleanKey.takeLast(4)}"
        val imported = SeedCatalog.importedProductsForSupplier(supplier.id, supplier.name)
        val updated = supplier.copy(
            apiKeyMasked = masked,
            isConnected = true,
            importedProductCount = imported.size
        )

        viewModelScope.launch {
            if (isAdmin) {
                repository.upsertSupplier(updated)
                if (updated.autoImport) {
                    imported.forEach { repository.upsertProduct(it) }
                }
            }
            _localPreviewSuppliers.value = _localPreviewSuppliers.value.map {
                if (it.id == supplier.id) updated else it
            }
            if (updated.autoImport) {
                val existingIds = _localPreviewProducts.value.map { it.id }.toSet()
                val toAdd = imported.filterNot { it.id in existingIds }
                _localPreviewProducts.value = toAdd + _localPreviewProducts.value
            }
            _toastMessage.value = "${supplier.name} Connected • ${imported.size} product(s) auto-imported"
        }
    }

    fun disconnectSupplier(supplier: Supplier) {
        val updated = supplier.copy(isConnected = false, apiKeyMasked = "")
        viewModelScope.launch {
            if (isAdmin) repository.upsertSupplier(updated)
            _localPreviewSuppliers.value = _localPreviewSuppliers.value.map {
                if (it.id == supplier.id) updated else it
            }
            _toastMessage.value = "${supplier.name} Disconnected"
        }
    }

    fun updateSupplierAutomations(updated: Supplier) {
        viewModelScope.launch {
            if (isAdmin) repository.upsertSupplier(updated)
            _localPreviewSuppliers.value = _localPreviewSuppliers.value.map {
                if (it.id == updated.id) updated else it
            }
        }
    }

    fun syncSupplierPrices(supplier: Supplier) {
        if (!supplier.isConnected) {
            _toastMessage.value = "Connect ${supplier.name} API key first"
            return
        }
        viewModelScope.launch {
            val matching = products.value.filter { it.supplierId == supplier.id }
            matching.forEach { prod ->
                val adjustedPrice = (prod.price * 0.96).let { kotlin.math.round(it * 100) / 100.0 }
                val updated = prod.copy(price = adjustedPrice)
                if (isAdmin) repository.upsertProduct(updated)
            }
            _localPreviewProducts.value = _localPreviewProducts.value.map { prod ->
                if (prod.supplierId == supplier.id) {
                    prod.copy(price = kotlin.math.round(prod.price * 0.96 * 100) / 100.0)
                } else prod
            }
            _toastMessage.value = "Auto Price Sync completed for ${supplier.name} (${matching.size} products)"
        }
    }

    fun syncSupplierStock(supplier: Supplier, simulateOutOfStock: Boolean) {
        if (!supplier.isConnected) {
            _toastMessage.value = "Connect ${supplier.name} API key first"
            return
        }
        viewModelScope.launch {
            val matching = products.value.filter { it.supplierId == supplier.id }
            matching.forEachIndexed { idx, prod ->
                val newStock = if (simulateOutOfStock && idx == 0) 0 else if (prod.stock == 0) 18 else prod.stock
                val updated = prod.copy(stock = newStock)
                if (isAdmin) repository.upsertProduct(updated)
            }
            _localPreviewProducts.value = _localPreviewProducts.value.mapIndexed { idx, prod ->
                if (prod.supplierId == supplier.id) {
                    val newStock = if (simulateOutOfStock && idx == 0) 0 else if (prod.stock == 0) 18 else prod.stock
                    prod.copy(stock = newStock)
                } else prod
            }
            _toastMessage.value = if (simulateOutOfStock) {
                "Stock Sync: ${supplier.name} item marked Out of Stock"
            } else {
                "Auto Stock Sync refreshed for ${supplier.name}"
            }
        }
    }

    fun addNewCustomSupplier(name: String, code: String, apiKey: String, specialNote: String) {
        if (name.isBlank() || code.isBlank()) {
            _toastMessage.value = "Supplier name and code are required"
            return
        }
        val cleanId = name.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_').ifBlank { "custom_sup" }
        val connected = apiKey.trim().length >= 6
        val masked = if (connected) "${code.lowercase()}_****${apiKey.trim().takeLast(4)}" else ""
        val newSupplier = Supplier(
            id = cleanId,
            name = name.trim(),
            code = code.trim().uppercase(),
            apiKeyMasked = masked,
            isConnected = connected,
            autoImport = true,
            autoPriceSync = true,
            autoStockSync = true,
            autoOrderForwarding = true,
            autoTrackingSync = true,
            specialNote = specialNote.ifBlank { "Custom Dropshipping Partner" },
            importedProductCount = if (connected) 1 else 0,
            isCustom = true
        )
        viewModelScope.launch {
            if (isAdmin) {
                repository.upsertSupplier(newSupplier)
                if (connected) {
                    SeedCatalog.importedProductsForSupplier(cleanId, newSupplier.name).forEach {
                        repository.upsertProduct(it)
                    }
                }
            }
            _localPreviewSuppliers.value = _localPreviewSuppliers.value + newSupplier
            if (connected) {
                _localPreviewProducts.value = SeedCatalog.importedProductsForSupplier(cleanId, newSupplier.name) + _localPreviewProducts.value
            }
            _toastMessage.value = "Added supplier ${newSupplier.name}"
        }
    }

    fun adminSaveProduct(product: Product) {
        viewModelScope.launch {
            if (isAdmin) {
                val res = repository.upsertProduct(product)
                _toastMessage.value = if (res.isSuccess) "Product saved to store" else "Could not save product"
            } else {
                val exists = _localPreviewProducts.value.any { it.id == product.id }
                _localPreviewProducts.value = if (exists) {
                    _localPreviewProducts.value.map { if (it.id == product.id) product else it }
                } else {
                    listOf(product.copy(id = "prod_${System.currentTimeMillis()}")) + _localPreviewProducts.value
                }
                _toastMessage.value = "Product updated (Admin preview)"
            }
        }
    }

    fun adminDeleteProduct(productId: String) {
        viewModelScope.launch {
            if (isAdmin) repository.deleteProduct(productId)
            _localPreviewProducts.value = _localPreviewProducts.value.filterNot { it.id == productId }
            _toastMessage.value = "Product removed"
        }
    }

    fun adminForwardAndGenerateTracking(order: Order) {
        viewModelScope.launch {
            val isAlreadyForwarded = order.forwardingStatus == "Forwarded" || order.orderStatus == "Forwarded to Supplier"
            val nextOrderStatus = if (!isAlreadyForwarded) "Forwarded to Supplier" else "Shipped"
            val nextForwardStatus = if (!isAlreadyForwarded) "Forwarded" else "Dispatched"
            val carrier = when (order.supplierId) {
                "zendrop" -> "USPS Priority"
                "spocket" -> "DHL Express"
                else -> "UPS Worldwide"
            }
            val tracking = if (!isAlreadyForwarded) "" else "1Z999AA10${(1000000..9999999).random()}"

            if (isAdmin) {
                val res = repository.adminUpdateOrderFulfillment(
                    orderId = order.id,
                    orderStatus = nextOrderStatus,
                    forwardingStatus = nextForwardStatus,
                    trackingNumber = tracking,
                    carrier = carrier
                )
                _toastMessage.value = if (res.isSuccess) {
                    if (tracking.isNotEmpty()) {
                        "Tracking $tracking synced & emailed to ${order.customerEmail}"
                    } else {
                        "Order forwarded to ${order.supplierName}"
                    }
                } else {
                    "Fulfillment update failed"
                }
            }
        }
    }

    private fun unwrapAndLog(source: String, error: Throwable) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, "Stream error ($source): ${rootError.message}", rootError)
    }

    private companion object {
        const val TAG = "StoreVM"
    }
}
