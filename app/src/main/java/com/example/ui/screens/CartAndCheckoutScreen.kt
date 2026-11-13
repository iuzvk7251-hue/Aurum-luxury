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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItem
import com.example.data.model.UserProfile
import com.example.ui.components.EmptyStateBox
import com.example.ui.components.ProductPhoto
import com.example.ui.components.SecurityBadgeBanner
import com.example.ui.components.formatUsd
import com.example.ui.theme.ConnectedGreen
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDeepAccessible
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSoftTint
import com.example.ui.theme.ObsidianBlack
import com.example.ui.viewmodel.OrderNotificationSummary

@Composable
fun CartAndCheckoutScreen(
    cartItems: List<CartItem>,
    userProfile: UserProfile?,
    defaultName: String,
    defaultEmail: String,
    lastOrderNotification: OrderNotificationSummary?,
    onUpdateQty: (String, Int) -> Unit,
    onPlaceOrder: (name: String, email: String, phone: String, address: String, paymentMethod: String) -> Unit,
    onDismissNotification: () -> Unit,
    onBrowseShop: () -> Unit,
    onViewOrders: () -> Unit
) {
    val context = LocalContext.current
    var customerName by remember(userProfile, defaultName) {
        mutableStateOf(userProfile?.displayName?.ifBlank { defaultName } ?: defaultName)
    }
    var customerEmail by remember(userProfile, defaultEmail) {
        mutableStateOf(userProfile?.email?.ifBlank { defaultEmail } ?: defaultEmail)
    }
    var customerPhone by remember(userProfile) {
        mutableStateOf(userProfile?.phone ?: "+1 ")
    }
    var shippingAddress by remember(userProfile) {
        mutableStateOf(userProfile?.shippingAddress?.ifBlank { "" } ?: "")
    }

    val paymentMethods = listOf(
        "Card (Visa / MasterCard)",
        "PayPal",
        "Cash on Delivery"
    )
    var selectedPayment by remember { mutableStateOf(paymentMethods.first()) }
    var cardLast4Input by remember { mutableStateOf("4242") }
    var formError by remember { mutableStateOf<String?>(null) }

    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val shipping = if (subtotal >= 150.0 || cartItems.isEmpty()) 0.0 else 12.0
    val total = subtotal + shipping

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Shopping Bag & Checkout",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            if (cartItems.isEmpty()) {
                item {
                    EmptyStateBox(
                        icon = Icons.Default.ShoppingBag,
                        title = "Your Bag Is Empty",
                        subtitle = "Explore our US flagship titanium watches, acoustic audio, and custom sneakers.",
                        actionLabel = "Start Shopping",
                        onAction = onBrowseShop
                    )
                }
            } else {
                items(cartItems, key = { it.id }) { item ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductPhoto(
                                imageKey = item.imageKey,
                                contentDescription = item.title,
                                modifier = Modifier
                                    .size(84.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(item.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "${item.selectedVariant} • ${item.supplierName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (item.printOnDemandNote.isNotBlank()) {
                                    Text(
                                        text = "POD Print: ${item.printOnDemandNote}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = GoldDeepAccessible
                                    )
                                }
                                Text(
                                    text = formatUsd(item.price * item.quantity),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    IconButton(
                                        onClick = { onUpdateQty(item.id, item.quantity - 1) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            imageVector = if (item.quantity <= 1) Icons.Default.DeleteOutline else Icons.Default.Remove,
                                            contentDescription = "Decrease quantity"
                                        )
                                    }
                                    Text(
                                        text = item.quantity.toString(),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    IconButton(
                                        onClick = { onUpdateQty(item.id, item.quantity + 1) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase quantity")
                                    }
                                }
                            }
                        }
                    }
                }

                // Order Totals Summary
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = GoldSoftTint,
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal", style = MaterialTheme.typography.bodyLarge)
                                Text(formatUsd(subtotal), style = MaterialTheme.typography.bodyLarge)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("US Express Shipping", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = if (shipping == 0.0) "FREE" else formatUsd(shipping),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (shipping == 0.0) ConnectedGreen else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            HorizontalDivider()
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total", style = MaterialTheme.typography.titleLarge)
                                Text(
                                    text = formatUsd(total),
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }
                    }
                }

                // Delivery Details
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
                            Text("Shipping & Notification Details", style = MaterialTheme.typography.titleLarge)
                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { customerName = it },
                                label = { Text("Full Name") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("checkout_name_input")
                            )
                            OutlinedTextField(
                                value = customerEmail,
                                onValueChange = { customerEmail = it },
                                label = { Text("Email (for Receipt & Tracking)") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("checkout_email_input")
                            )
                            OutlinedTextField(
                                value = customerPhone,
                                onValueChange = { customerPhone = it },
                                label = { Text("WhatsApp / Mobile Number") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("checkout_phone_input")
                            )
                            OutlinedTextField(
                                value = shippingAddress,
                                onValueChange = { shippingAddress = it },
                                label = { Text("Street Address, City, State, ZIP") },
                                placeholder = { Text("725 Fifth Ave, New York, NY 10022") },
                                minLines = 2,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("checkout_address_input")
                            )
                        }
                    }
                }

                // Payment Methods
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
                            Text("Select Payment Method", style = MaterialTheme.typography.titleLarge)
                            paymentMethods.forEach { method ->
                                val selected = selectedPayment == method
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedPayment = method },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) GoldSoftTint else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        1.5.dp,
                                        if (selected) ObsidianBlack else MaterialTheme.colorScheme.outline
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        RadioButton(
                                            selected = selected,
                                            onClick = { selectedPayment = method }
                                        )
                                        Icon(
                                            imageVector = when (method) {
                                                "Card (Visa / MasterCard)" -> Icons.Default.CreditCard
                                                "PayPal" -> Icons.Default.Payments
                                                else -> Icons.Default.LocalShipping
                                            },
                                            contentDescription = null,
                                            tint = ObsidianBlack
                                        )
                                        Text(
                                            text = method,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                }
                            }

                            if (selectedPayment == "Card (Visa / MasterCard)") {
                                OutlinedTextField(
                                    value = cardLast4Input,
                                    onValueChange = { if (it.length <= 19) cardLast4Input = it },
                                    label = { Text("Visa / MasterCard Ending Digits (Tokenized SSL)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (formError != null) {
                                Text(
                                    text = formError ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Button(
                                onClick = {
                                    if (customerName.isBlank() || customerEmail.isBlank() || shippingAddress.isBlank()) {
                                        formError = "Please complete your name, email, and shipping address."
                                    } else {
                                        formError = null
                                        onPlaceOrder(
                                            customerName.trim(),
                                            customerEmail.trim(),
                                            customerPhone.trim(),
                                            shippingAddress.trim(),
                                            selectedPayment
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ObsidianBlack,
                                    contentColor = GoldBright
                                ),
                                contentPadding = PaddingValues(vertical = 18.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("place_order_button")
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Complete Order — ${formatUsd(total)}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp)
                                )
                            }
                        }
                    }
                }

                item { SecurityBadgeBanner() }
            }
        }
    }

    if (lastOrderNotification != null) {
        val summaryText = "Aurum Luxe Order Confirmed (${lastOrderNotification.orderId})\n" +
            "Customer: ${lastOrderNotification.customerName} (${lastOrderNotification.customerEmail})\n" +
            "Total: ${formatUsd(lastOrderNotification.totalAmount)} via ${lastOrderNotification.paymentMethod}\n" +
            "Fulfillment: ${lastOrderNotification.supplierName} " +
            if (lastOrderNotification.autoForwarded) "(Auto-Forwarded)" else "(Queued for Dispatch)"

        AlertDialog(
            onDismissRequest = onDismissNotification,
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Order Confirmed",
                    tint = ConnectedGreen,
                    modifier = Modifier.size(44.dp)
                )
            },
            title = {
                Text("Order Confirmed!", style = MaterialTheme.typography.headlineMedium)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(summaryText, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "Dispatch email & WhatsApp notifications are ready for both Customer & Store Admin:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = {
                            val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${lastOrderNotification.customerEmail},${lastOrderNotification.adminEmail}")
                                putExtra(Intent.EXTRA_SUBJECT, "Order ${lastOrderNotification.orderId} Confirmed — Aurum Luxe")
                                putExtra(Intent.EXTRA_TEXT, summaryText)
                            }
                            runCatching { context.startActivity(mailIntent) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Email Notification")
                    }
                    OutlinedButton(
                        onClick = {
                            val encoded = Uri.encode(summaryText)
                            val waIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://wa.me/${lastOrderNotification.adminWhatsApp.replace("+", "")}?text=$encoded")
                            )
                            runCatching { context.startActivity(waIntent) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Send WhatsApp Alert (Admin & Customer)")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDismissNotification()
                        onViewOrders()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ObsidianBlack,
                        contentColor = GoldBright
                    )
                ) {
                    Text("Track My Order")
                }
            }
        )
    }
}
