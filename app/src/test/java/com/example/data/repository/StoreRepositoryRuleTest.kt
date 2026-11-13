package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.example.data.model.Supplier
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class StoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun userProfileAndCart_ownerAccess_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = StoreRepository(firestore)

        val profileRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.ensureUserProfile(uid, "Alice Sterling", ALICE_EMAIL)
        }
        assertTrue(profileRes.isSuccess)

        val cartRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.addToCart(
                uid,
                CartItem(
                    productId = "prod_aurum_watch",
                    title = "Aurum Apex Titanium Watch",
                    price = 349.0,
                    quantity = 1,
                    imageKey = "watch",
                    selectedVariant = "Obsidian / Gold",
                    supplierId = "zendrop",
                    supplierName = "Zendrop"
                )
            )
        }
        assertTrue(cartRes.isSuccess)

        val cartItems = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeCart(uid).first { it.isNotEmpty() }
        }
        assertEquals(1, cartItems.size)
    }

    @Test
    fun placeAndObserveOrder_ownerAccess_succeeds_crossUserDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repo = StoreRepository(firestore)
        val uniqueOrderId = "ORD-${UUID.randomUUID().toString().take(8)}"

        val orderRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.placeOrder(
                Order(
                    id = uniqueOrderId,
                    userId = aliceUid,
                    customerName = "Alice Sterling",
                    customerEmail = ALICE_EMAIL,
                    customerPhone = "+1 555 0101",
                    shippingAddress = "725 Fifth Ave, New York, NY",
                    itemsSummary = "1x Aurum Apex Titanium Watch",
                    itemCount = 1,
                    subtotal = 349.0,
                    shippingCost = 0.0,
                    totalAmount = 349.0,
                    paymentMethod = "Card (Visa / MasterCard)",
                    paymentStatus = "Paid",
                    supplierId = "zendrop",
                    supplierName = "Zendrop"
                )
            )
        }
        assertTrue(orderRes.isSuccess)

        val orders = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeUserOrders(aliceUid).first { list -> list.any { it.id == uniqueOrderId } }
        }
        assertTrue(orders.any { it.id == uniqueOrderId })

        // Cross-user read attempt by Bob must fail with PERMISSION_DENIED
        signInTestUser(BOB_EMAIL)
        val bobRes = withTimeout(DEFAULT_TIMEOUT_MS) { repo.getOrderById(uniqueOrderId) }
        assertTrue(bobRes.isFailure)
        val fsEx = bobRes.exceptionOrNull()?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }

    @Test
    fun observeUserOrders_unauthenticated_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = StoreRepository(firestore)
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeUserOrders("alice_uid").first()
            }
            fail("Expected PERMISSION_DENIED when unauthenticated")
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
        }
    }

    @Test
    fun supplierWrite_nonAdmin_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repo = StoreRepository(firestore)
        val res = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.upsertSupplier(
                Supplier(
                    id = "zendrop",
                    name = "Zendrop",
                    code = "ZENDROP",
                    isConnected = true
                )
            )
        }
        assertTrue(res.isFailure)
        val fsEx = res.exceptionOrNull()?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@test.com"
        const val BOB_EMAIL = "bob@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
