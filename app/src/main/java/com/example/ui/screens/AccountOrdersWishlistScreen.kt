package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.UserProfile
import com.example.data.model.WishlistItem
import com.example.ui.components.EmptyStateBox
import com.example.ui.components.ProductPhoto
import com.example.ui.components.formatUsd
import com.example.ui.theme.ConnectedGreen
import com.example.ui.theme.ConnectedGreenBg
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDeepAccessible
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSoftTint
import com.example.ui.theme.ObsidianBlack

@Composable
fun AccountOrdersWishlistScreen(
    userProfile: UserProfile?,
    userEmail: String,
    userName: String,
    isAdmin: Boolean,
    orders: List<Order>,
    wishlist: List<WishlistItem>,
    products: List<Product>,
    initialTab: Int = 0,
    onSaveProfile: (name: String, phone: String, address: String) -> Unit,
    onCancelOrder: (String) -> Unit,
    onProductClick: (String) -> Unit,
    onRemoveWishlist: (Product) -> Unit,
    onOpenAdminPanel: () -> Unit,
    onOpenInfoPage: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    val tabs = listOf("My Orders", "Wishlist", "My Account")

    var nameInput by remember(userProfile, userName) {
        mutableStateOf(userProfile?.displayName?.ifBlank { userName } ?: userName)
    }
    var phoneInput by remember(userProfile) { mutableStateOf(userProfile?.phone ?: "") }
    var addressInput by remember(userProfile) { mutableStateOf(userProfile?.shippingAddress ?: "") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        modifier = Modifier.testTag("account_tab_$idx")
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // My Orders & Live Order Tracking
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (orders.isEmpty()) {
                            item {
                                EmptyStateBox(
                                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                    title = "No Orders Yet",
                                    subtitle = "Once you place an order, real-time supplier forwarding and carrier tracking will appear here."
                                )
                            }
                        } else {
                            items(orders, key = { it.id }) { order ->
                                OrderTrackingCard(
                                    order = order,
                                    onCancel = { onCancelOrder(order.id) }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Wishlist
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (wishlist.isEmpty()) {
                            item {
                                EmptyStateBox(
                                    icon = Icons.Default.Favorite,
                                    title = "Your Wishlist Is Empty",
                                    subtitle = "Tap the heart icon on any watch, audio gear, or sneaker to save it here."
                                )
                            }
                        } else {
                            items(wishlist, key = { it.productId }) { item ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onProductClick(item.productId) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        ProductPhoto(
                                            imageKey = item.imageKey,
                                            contentDescription = item.title,
                                            modifier = Modifier
                                                .size(72.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                                            Text(
                                                text = formatUsd(item.price),
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = GoldDeepAccessible
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                val matched = products.firstOrNull { it.id == item.productId }
                                                    ?: Product(id = item.productId, title = item.title, price = item.price)
                                                onRemoveWishlist(matched)
                                            },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Text("Remove")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    // My Account
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                color = ObsidianBlack
                            ) {
                                Row(
                                    modifier = Modifier.padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = GoldBright,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Column {
                                        Text(
                                            text = nameInput.ifBlank { "Aurum Member" },
                                            style = MaterialTheme.typography.titleLarge,
                                            color = GoldBright
                                        )
                                        Text(
                                            text = userEmail,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Text(
                                            text = if (isAdmin) "Verified Store Owner (Admin)" else "Verified Customer Account",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = GoldPrimary
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Button(
                                onClick = onOpenAdminPanel,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = ObsidianBlack
                                ),
                                contentPadding = PaddingValues(vertical = 16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("account_open_admin_button")
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open Dropshipping Admin Panel", style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("Profile & Saved Shipping", style = MaterialTheme.typography.titleLarge)
                                    OutlinedTextField(
                                        value = nameInput,
                                        onValueChange = { nameInput = it },
                                        label = { Text("Full Name") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = phoneInput,
                                        onValueChange = { phoneInput = it },
                                        label = { Text("Phone / WhatsApp") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = addressInput,
                                        onValueChange = { addressInput = it },
                                        label = { Text("Default US / Global Shipping Address") },
                                        minLines = 2,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Button(
                                        onClick = { onSaveProfile(nameInput, phoneInput, addressInput) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ObsidianBlack,
                                            contentColor = GoldBright
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .minimumInteractiveComponentSize()
                                    ) {
                                        Text("Save Profile", style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                        }

                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("Store Help & Policies", style = MaterialTheme.typography.titleMedium)
                                    listOf(
                                        "contact" to "Contact Us (Form, Email, WhatsApp)",
                                        "faq" to "FAQ",
                                        "shipping" to "Shipping Policy",
                                        "returns" to "Return & Refund Policy",
                                        "privacy" to "Privacy Policy",
                                        "terms" to "Terms & Conditions",
                                        "about" to "About Us"
                                    ).forEach { (key, label) ->
                                        OutlinedButton(
                                            onClick = { onOpenInfoPage(key) },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(label)
                                        }
                                    }
                                    HorizontalDivider()
                                    OutlinedButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://accounts.google.com/signin/recovery"))
                                            runCatching { context.startActivity(intent) }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Password & Security Recovery")
                                    }
                                    OutlinedButton(
                                        onClick = onSignOut,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("sign_out_button")
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Sign Out")
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

@Composable
fun OrderTrackingCard(
    order: Order,
    onCancel: () -> Unit
) {
    val progress = when (order.orderStatus) {
        "Pending" -> 0.25f
        "Forwarded to Supplier" -> 0.55f
        "Shipped" -> 0.80f
        "Delivered" -> 1.0f
        else -> 0.0f
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.id,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (order.orderStatus == "Shipped" || order.orderStatus == "Delivered") {
                        ConnectedGreenBg
                    } else GoldSoftTint
                ) {
                    Text(
                        text = order.orderStatus,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (order.orderStatus == "Shipped" || order.orderStatus == "Delivered") {
                            ConnectedGreen
                        } else ObsidianBlack,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Text(order.itemsSummary, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "Total: ${formatUsd(order.totalAmount)} • ${order.paymentMethod} (${order.paymentStatus})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (order.orderStatus != "Cancelled") {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = GoldPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Placed", style = MaterialTheme.typography.labelMedium)
                    Text("Supplier", style = MaterialTheme.typography.labelMedium)
                    Text("Shipped", style = MaterialTheme.typography.labelMedium)
                    Text("Delivered", style = MaterialTheme.typography.labelMedium)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = null,
                    tint = GoldDeepAccessible,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Fulfillment: ${order.supplierName} (${order.forwardingStatus})",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (order.trackingNumber.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ObsidianBlack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = GoldBright)
                        Text(
                            text = "${order.carrier.ifBlank { "Express" }} Tracking: ${order.trackingNumber}",
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldBright
                        )
                    }
                }
            }

            if (order.orderStatus == "Pending") {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text("Cancel Order")
                }
            }
        }
    }
}
