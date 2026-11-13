package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.ProductReview
import com.example.data.model.SourcingRequest
import com.example.data.model.Supplier
import com.example.data.model.SupportTicket
import com.example.data.model.UserProfile
import com.example.data.model.WishlistItem
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StoreRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before accessing Firestore.")
    }

    private fun <T> Flow<T>.handleStreamError(op: OperationType, path: String, expectedUid: String? = null): Flow<T> =
        catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val toLog = firestoreError ?: (error as? Exception)
            if (toLog != null && (expectedUid == null || Firebase.auth.currentUser?.uid == expectedUid)) {
                handleFirestoreError(toLog, op, path)
            }
            throw error
        }

    // --- User Profile ---
    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val docRef = db.collection("users").document(userId)
        emitAll(
            docRef.snapshots()
                .map { snap ->
                    if (snap.exists()) {
                        snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    } else null
                }
                .handleStreamError(OperationType.GET, docRef.path, userId)
        )
    }

    suspend fun ensureUserProfile(userId: String, displayName: String, email: String): Result<Unit> = runCatching {
        val docRef = db.collection("users").document(userId)
        try {
            val snap = docRef.get().await()
            if (!snap.exists()) {
                val payload = mapOf(
                    "userId" to userId,
                    "displayName" to displayName.ifBlank { "Aurum Member" }.take(100),
                    "email" to email.ifBlank { "member@aurumluxe.com" }.take(200),
                    "phone" to "",
                    "shippingAddress" to "",
                    "role" to "customer",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun updateUserProfile(
        userId: String,
        displayName: String,
        phone: String,
        shippingAddress: String
    ): Result<Unit> = runCatching {
        val docRef = db.collection("users").document(userId)
        try {
            docRef.update(
                mapOf(
                    "displayName" to displayName.ifBlank { "Aurum Member" }.take(100),
                    "phone" to phone.take(40),
                    "shippingAddress" to shippingAddress.take(300),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    fun observeAllCustomers(): Flow<List<UserProfile>> = flow {
        val path = "users"
        emitAll(
            db.collection(path).snapshots()
                .map { snap -> snap.toObjects(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleStreamError(OperationType.LIST, path)
        )
    }

    // --- Cart ---
    fun observeCart(userId: String): Flow<List<CartItem>> = flow {
        val path = "users/$userId/cart"
        emitAll(
            db.collection("users").document(userId).collection("cart")
                .snapshots()
                .map { snap -> snap.toObjects(CartItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleStreamError(OperationType.LIST, path, userId)
        )
    }

    suspend fun addToCart(userId: String, item: CartItem): Result<Unit> = runCatching {
        val sanitizedKey = "${item.productId}_${item.selectedVariant.hashCode().toUInt()}".replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(80)
        val docRef = db.collection("users").document(userId).collection("cart").document(sanitizedKey)
        try {
            val existing = docRef.get().await()
            if (existing.exists()) {
                val currentQty = (existing.getLong("quantity") ?: 1L).toInt()
                val newQty = (currentQty + item.quantity).coerceIn(1, 99)
                docRef.update(
                    mapOf(
                        "quantity" to newQty,
                        "selectedVariant" to item.selectedVariant.take(80),
                        "printOnDemandNote" to item.printOnDemandNote.take(240),
                        "price" to item.price,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val payload = mapOf(
                    "userId" to userId,
                    "productId" to item.productId,
                    "title" to item.title.take(160),
                    "price" to item.price,
                    "quantity" to item.quantity.coerceIn(1, 99),
                    "imageKey" to item.imageKey.ifBlank { "watch" },
                    "selectedVariant" to item.selectedVariant.take(80),
                    "printOnDemandNote" to item.printOnDemandNote.take(240),
                    "supplierId" to item.supplierId.take(64),
                    "supplierName" to item.supplierName.take(100),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun updateCartQuantity(userId: String, cartItemId: String, newQuantity: Int): Result<Unit> = runCatching {
        val docRef = db.collection("users").document(userId).collection("cart").document(cartItemId)
        try {
            if (newQuantity <= 0) {
                docRef.delete().await()
            } else {
                docRef.update(
                    mapOf(
                        "quantity" to newQuantity.coerceIn(1, 99),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun clearCart(userId: String): Result<Unit> = runCatching {
        val colRef = db.collection("users").document(userId).collection("cart")
        try {
            val snap = colRef.get().await()
            if (!snap.isEmpty) {
                val batch = db.batch()
                snap.documents.forEach { batch.delete(it.reference) }
                batch.commit().await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, colRef.path)
            throw e
        }
    }

    // --- Wishlist ---
    fun observeWishlist(userId: String): Flow<List<WishlistItem>> = flow {
        val path = "users/$userId/wishlist"
        emitAll(
            db.collection("users").document(userId).collection("wishlist")
                .snapshots()
                .map { snap -> snap.toObjects(WishlistItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleStreamError(OperationType.LIST, path, userId)
        )
    }

    suspend fun toggleWishlist(userId: String, product: Product): Result<Boolean> = runCatching {
        val docRef = db.collection("users").document(userId).collection("wishlist").document(product.id)
        try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                docRef.delete().await()
                false
            } else {
                val payload = mapOf(
                    "userId" to userId,
                    "productId" to product.id,
                    "title" to product.title.take(160),
                    "price" to product.price,
                    "imageKey" to product.imageKey.ifBlank { "watch" },
                    "supplierName" to product.supplierName.take(100),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
                true
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // --- Products ---
    fun observeProducts(): Flow<List<Product>> = flow {
        val path = "products"
        emitAll(
            db.collection(path).snapshots()
                .map { snap -> snap.toObjects(Product::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleStreamError(OperationType.LIST, path)
        )
    }

    suspend fun upsertProduct(product: Product): Result<Unit> = runCatching {
        val docId = product.id.ifBlank { "prod_${UUID.randomUUID().toString().replace("-", "").take(12)}" }
        val docRef = db.collection("products").document(docId)
        try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "title" to product.title.take(160),
                        "description" to product.description.take(2000),
                        "category" to product.category.take(60),
                        "price" to product.price,
                        "compareAtPrice" to product.compareAtPrice,
                        "stock" to product.stock.coerceAtLeast(0),
                        "imageKey" to product.imageKey.ifBlank { "watch" },
                        "rating" to product.rating.coerceIn(0.0, 5.0),
                        "reviewCount" to product.reviewCount.coerceAtLeast(0),
                        "supplierId" to product.supplierId.take(64),
                        "supplierName" to product.supplierName.take(100),
                        "supplierTag" to product.supplierTag.take(100),
                        "shippingEstimate" to product.shippingEstimate.take(80),
                        "isBestSeller" to product.isBestSeller,
                        "supportsPrintOnDemand" to product.supportsPrintOnDemand,
                        "supportsSampleOrder" to product.supportsSampleOrder,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "title" to product.title.take(160),
                        "description" to product.description.take(2000),
                        "category" to product.category.take(60),
                        "price" to product.price,
                        "compareAtPrice" to product.compareAtPrice,
                        "stock" to product.stock.coerceAtLeast(0),
                        "imageKey" to product.imageKey.ifBlank { "watch" },
                        "rating" to product.rating.coerceIn(0.0, 5.0),
                        "reviewCount" to product.reviewCount.coerceAtLeast(0),
                        "supplierId" to product.supplierId.take(64),
                        "supplierName" to product.supplierName.take(100),
                        "supplierTag" to product.supplierTag.take(100),
                        "shippingEstimate" to product.shippingEstimate.take(80),
                        "isBestSeller" to product.isBestSeller,
                        "supportsPrintOnDemand" to product.supportsPrintOnDemand,
                        "supportsSampleOrder" to product.supportsSampleOrder,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> = runCatching {
        val docRef = db.collection("products").document(productId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Orders ---
    fun observeUserOrders(userId: String): Flow<List<Order>> = flow {
        val path = "orders"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snap -> snap.toObjects(Order::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleStreamError(OperationType.LIST, path, userId)
        )
    }

    fun observeAllOrders(): Flow<List<Order>> = flow {
        val path = "orders"
        emitAll(
            db.collection(path).snapshots()
                .map { snap -> snap.toObjects(Order::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleStreamError(OperationType.LIST, path)
        )
    }

    suspend fun getOrderById(orderId: String): Result<Order?> = runCatching {
        val docRef = db.collection("orders").document(orderId)
        try {
            docRef.get().await().toObject(Order::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun placeOrder(order: Order): Result<String> = runCatching {
        val uid = requireUserId()
        val orderId = order.id.ifBlank { "ORD-${UUID.randomUUID().toString().replace("-", "").take(8).uppercase()}" }
        val docRef = db.collection("orders").document(orderId)
        val payload = mapOf(
            "userId" to uid,
            "customerName" to order.customerName.ifBlank { "Valued Customer" }.take(120),
            "customerEmail" to order.customerEmail.ifBlank { "customer@aurumluxe.com" }.take(200),
            "customerPhone" to order.customerPhone.take(40),
            "shippingAddress" to order.shippingAddress.ifBlank { "USA Express Delivery" }.take(350),
            "itemsSummary" to order.itemsSummary.take(1000),
            "itemCount" to order.itemCount.coerceAtLeast(1),
            "subtotal" to order.subtotal,
            "shippingCost" to order.shippingCost,
            "totalAmount" to order.totalAmount,
            "paymentMethod" to order.paymentMethod,
            "paymentStatus" to order.paymentStatus,
            "orderStatus" to "Pending",
            "supplierId" to order.supplierId.ifBlank { "zendrop" },
            "supplierName" to order.supplierName.ifBlank { "Zendrop" },
            "forwardingStatus" to "Queued",
            "trackingNumber" to "",
            "carrier" to "",
            "notificationSent" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            orderId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun cancelOrder(orderId: String): Result<Unit> = runCatching {
        val docRef = db.collection("orders").document(orderId)
        try {
            docRef.update(
                mapOf(
                    "orderStatus" to "Cancelled",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun adminUpdateOrderFulfillment(
        orderId: String,
        orderStatus: String,
        forwardingStatus: String,
        trackingNumber: String,
        carrier: String
    ): Result<Unit> = runCatching {
        val docRef = db.collection("orders").document(orderId)
        try {
            docRef.update(
                mapOf(
                    "orderStatus" to orderStatus,
                    "forwardingStatus" to forwardingStatus,
                    "trackingNumber" to trackingNumber.take(80),
                    "carrier" to carrier.take(80),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- Suppliers ---
    fun observeSuppliers(): Flow<List<Supplier>> = flow {
        val path = "suppliers"
        emitAll(
            db.collection(path).snapshots()
                .map { snap -> snap.toObjects(Supplier::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleStreamError(OperationType.LIST, path)
        )
    }

    suspend fun upsertSupplier(supplier: Supplier): Result<Unit> = runCatching {
        val docId = supplier.id.ifBlank { "sup_${UUID.randomUUID().toString().replace("-", "").take(10)}" }
        val docRef = db.collection("suppliers").document(docId)
        try {
            val snap = docRef.get().await()
            if (snap.exists()) {
                docRef.update(
                    mapOf(
                        "name" to supplier.name.take(100),
                        "code" to supplier.code.take(50),
                        "apiKeyMasked" to supplier.apiKeyMasked.take(100),
                        "isConnected" to supplier.isConnected,
                        "autoImport" to supplier.autoImport,
                        "autoPriceSync" to supplier.autoPriceSync,
                        "autoStockSync" to supplier.autoStockSync,
                        "autoOrderForwarding" to supplier.autoOrderForwarding,
                        "autoTrackingSync" to supplier.autoTrackingSync,
                        "specialNote" to supplier.specialNote.take(200),
                        "importedProductCount" to supplier.importedProductCount.coerceAtLeast(0),
                        "isCustom" to supplier.isCustom,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "name" to supplier.name.take(100),
                        "code" to supplier.code.take(50),
                        "apiKeyMasked" to supplier.apiKeyMasked.take(100),
                        "isConnected" to supplier.isConnected,
                        "autoImport" to supplier.autoImport,
                        "autoPriceSync" to supplier.autoPriceSync,
                        "autoStockSync" to supplier.autoStockSync,
                        "autoOrderForwarding" to supplier.autoOrderForwarding,
                        "autoTrackingSync" to supplier.autoTrackingSync,
                        "specialNote" to supplier.specialNote.take(200),
                        "importedProductCount" to supplier.importedProductCount.coerceAtLeast(0),
                        "isCustom" to supplier.isCustom,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    // --- Sourcing Requests & Sample Orders ---
    fun observeSourcingRequests(userId: String, isAdmin: Boolean): Flow<List<SourcingRequest>> = flow {
        val path = "sourcing_requests"
        val query = if (isAdmin) {
            db.collection(path)
        } else {
            db.collection(path).whereEqualTo("userId", userId)
        }
        emitAll(
            query.snapshots()
                .map { snap -> snap.toObjects(SourcingRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleStreamError(OperationType.LIST, path, if (isAdmin) null else userId)
        )
    }

    suspend fun submitSourcingRequest(request: SourcingRequest): Result<String> = runCatching {
        val uid = requireUserId()
        val reqId = "REQ-${UUID.randomUUID().toString().replace("-", "").take(8).uppercase()}"
        val docRef = db.collection("sourcing_requests").document(reqId)
        try {
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "supplierId" to request.supplierId.take(64),
                    "supplierName" to request.supplierName.take(100),
                    "requestType" to request.requestType,
                    "productTitle" to request.productTitle.take(160),
                    "notes" to request.notes.take(500),
                    "status" to "Submitted",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            reqId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // --- Support Tickets ---
    suspend fun submitSupportTicket(ticket: SupportTicket): Result<String> = runCatching {
        val uid = requireUserId()
        val id = "TKT-${UUID.randomUUID().toString().replace("-", "").take(8).uppercase()}"
        val docRef = db.collection("support_tickets").document(id)
        try {
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "name" to ticket.name.take(120),
                    "email" to ticket.email.take(200),
                    "subject" to ticket.subject.take(160),
                    "message" to ticket.message.take(2000),
                    "status" to "Open",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // --- Reviews ---
    fun observeReviews(): Flow<List<ProductReview>> = flow {
        val path = "reviews"
        emitAll(
            db.collection(path).snapshots()
                .map { snap -> snap.toObjects(ProductReview::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .handleStreamError(OperationType.LIST, path)
        )
    }

    suspend fun submitReview(review: ProductReview): Result<Unit> = runCatching {
        val uid = requireUserId()
        val id = "REV-${UUID.randomUUID().toString().replace("-", "").take(8).uppercase()}"
        val docRef = db.collection("reviews").document(id)
        try {
            docRef.set(
                mapOf(
                    "productId" to review.productId,
                    "userId" to uid,
                    "authorName" to review.authorName.ifBlank { "Verified Shopper" }.take(100),
                    "rating" to review.rating.coerceIn(1, 5),
                    "comment" to review.comment.take(1000),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }
}
