package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.SourcingRequest
import com.example.data.model.Supplier
import com.example.data.model.UserProfile
import com.example.ui.components.StatusDotPill
import com.example.ui.components.formatUsd
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDeepAccessible
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSoftTint
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite

@Composable
fun AdminDashboardScreen(
    isAdmin: Boolean,
    suppliers: List<Supplier>,
    products: List<Product>,
    orders: List<Order>,
    customers: List<UserProfile>,
    sourcingRequests: List<SourcingRequest>,
    onConnectSupplier: (Supplier, String) -> Unit,
    onDisconnectSupplier: (Supplier) -> Unit,
    onUpdateSupplierToggles: (Supplier) -> Unit,
    onSyncPrices: (Supplier) -> Unit,
    onSyncStock: (Supplier, Boolean) -> Unit,
    onAddCustomSupplier: (name: String, code: String, apiKey: String, note: String) -> Unit,
    onSaveProduct: (Product) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onForwardAndSyncTracking: (Order) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Suppliers", "Products", "Orders", "Customers")

    var showAddSupplierDialog by remember { mutableStateOf(false) }
    var newSupName by remember { mutableStateOf("") }
    var newSupCode by remember { mutableStateOf("") }
    var newSupApiKey by remember { mutableStateOf("") }
    var newSupNote by remember { mutableStateOf("") }

    var showAddProductDialog by remember { mutableStateOf(false) }
    var prodTitle by remember { mutableStateOf("") }
    var prodCategory by remember { mutableStateOf("Watches") }
    var prodPrice by remember { mutableStateOf("249.00") }
    var prodStock by remember { mutableStateOf("20") }

    val totalRevenue = orders.filter { it.orderStatus != "Cancelled" }.sumOf { it.totalAmount }
    val connectedCount = suppliers.count { it.isConnected }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 800.dp)
                .fillMaxSize()
        ) {
            // Admin KPI Summary Header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(20.dp),
                color = ObsidianBlack
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ADMIN & DROPSHIPPING HUB",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldBright
                            )
                            Text(
                                text = "$connectedCount / ${suppliers.size} Suppliers Connected",
                                style = MaterialTheme.typography.titleLarge,
                                color = PureWhite
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = GoldPrimary
                        ) {
                            Text(
                                text = if (isAdmin) "OWNER CLOUD SYNC" else "PREVIEW MODE",
                                style = MaterialTheme.typography.labelMedium,
                                color = ObsidianBlack,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AdminKpiBox("Orders", orders.size.toString(), Modifier.weight(1f))
                        AdminKpiBox("Customers", customers.size.coerceAtLeast(1).toString(), Modifier.weight(1f))
                        AdminKpiBox("Products", products.size.toString(), Modifier.weight(1f))
                        AdminKpiBox("Revenue", formatUsd(totalRevenue), Modifier.weight(1.3f))
                    }
                }
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(title, style = MaterialTheme.typography.labelLarge) },
                        modifier = Modifier.testTag("admin_tab_$idx")
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // Suppliers Integration Dashboard
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Button(
                                onClick = { showAddSupplierDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = ObsidianBlack
                                ),
                                contentPadding = PaddingValues(vertical = 14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("add_new_supplier_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add New Supplier", style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        items(suppliers, key = { it.id }) { supplier ->
                            SupplierIntegrationCard(
                                supplier = supplier,
                                onConnect = { key -> onConnectSupplier(supplier, key) },
                                onDisconnect = { onDisconnectSupplier(supplier) },
                                onUpdateToggles = onUpdateSupplierToggles,
                                onSyncPrices = { onSyncPrices(supplier) },
                                onSyncStock = { simulateOut -> onSyncStock(supplier, simulateOut) }
                            )
                        }
                    }
                }
                1 -> {
                    // Manage Products
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Button(
                                onClick = { showAddProductDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ObsidianBlack,
                                    contentColor = GoldBright
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add New Store Product", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        items(products, key = { it.id }) { prod ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(prod.title, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = "${formatUsd(prod.price)} • Stock: ${prod.stock} • ${prod.supplierName}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row {
                                        OutlinedButton(
                                            onClick = {
                                                val toggledStock = if (prod.stock > 0) 0 else 25
                                                onSaveProduct(prod.copy(stock = toggledStock))
                                            }
                                        ) {
                                            Text(if (prod.stock > 0) "Set 0" else "Restock")
                                        }
                                        IconButton(onClick = { onDeleteProduct(prod.id) }) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete product")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Orders & Auto Tracking Sync + Sourcing Requests
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("Customer Orders & Fulfillment", style = MaterialTheme.typography.titleLarge)
                        }
                        if (orders.isEmpty()) {
                            item {
                                Text(
                                    text = "No customer orders recorded yet.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(orders, key = { it.id }) { order ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(order.id, style = MaterialTheme.typography.titleMedium)
                                            Text(formatUsd(order.totalAmount), style = MaterialTheme.typography.titleMedium)
                                        }
                                        Text(
                                            text = "${order.customerName} (${order.customerEmail})",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Supplier: ${order.supplierName} • Status: ${order.orderStatus} • Forward: ${order.forwardingStatus}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = GoldDeepAccessible
                                        )
                                        if (order.trackingNumber.isNotBlank()) {
                                            Text(
                                                text = "Tracking: ${order.carrier} ${order.trackingNumber}",
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        }
                                        if (order.orderStatus != "Cancelled" && order.orderStatus != "Shipped") {
                                            Button(
                                                onClick = { onForwardAndSyncTracking(order) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = ObsidianBlack,
                                                    contentColor = GoldBright
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(Icons.Default.LocalShipping, contentDescription = null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = if (order.forwardingStatus == "Queued") {
                                                        "Auto-Forward to ${order.supplierName}"
                                                    } else {
                                                        "Sync Tracking # & Email Customer"
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (sourcingRequests.isNotEmpty()) {
                            item {
                                HorizontalDivider()
                                Spacer(modifier = Modifier.padding(top = 8.dp))
                                Text("CJ Sourcing & Spocket Sample Orders", style = MaterialTheme.typography.titleLarge)
                            }
                            items(sourcingRequests, key = { it.id }) { req ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = GoldSoftTint,
                                    border = BorderStroke(1.dp, GoldPrimary)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("${req.requestType} • ${req.supplierName}", style = MaterialTheme.typography.labelLarge)
                                        Text(req.productTitle, style = MaterialTheme.typography.titleMedium)
                                        Text(req.notes, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    // Customers
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("Registered Store Customers", style = MaterialTheme.typography.titleLarge)
                        }
                        if (customers.isEmpty()) {
                            item {
                                Text(
                                    text = "Customer profiles sync here for the verified store owner.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(customers, key = { it.userId }) { cust ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(Icons.Default.People, contentDescription = null, tint = GoldDeepAccessible)
                                        Column {
                                            Text(cust.displayName, style = MaterialTheme.typography.titleMedium)
                                            Text(cust.email, style = MaterialTheme.typography.bodyMedium)
                                            if (cust.shippingAddress.isNotBlank()) {
                                                Text(
                                                    text = cust.shippingAddress,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

    if (showAddSupplierDialog) {
        AlertDialog(
            onDismissRequest = { showAddSupplierDialog = false },
            title = { Text("Add New Dropshipping Supplier") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newSupName,
                        onValueChange = { newSupName = it },
                        label = { Text("Supplier Name (e.g., Printful)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newSupCode,
                        onValueChange = { newSupCode = it },
                        label = { Text("Short Code (e.g., PRINTFUL)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newSupApiKey,
                        onValueChange = { newSupApiKey = it },
                        label = { Text("Paste API Key (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newSupNote,
                        onValueChange = { newSupNote = it },
                        label = { Text("Warehouse / Specialty Note") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddCustomSupplier(newSupName, newSupCode, newSupApiKey, newSupNote)
                        newSupName = ""
                        newSupCode = ""
                        newSupApiKey = ""
                        newSupNote = ""
                        showAddSupplierDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack, contentColor = GoldBright)
                ) {
                    Text("Add Supplier")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSupplierDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddProductDialog) {
        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text("Add Store Product") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = prodTitle,
                        onValueChange = { prodTitle = it },
                        label = { Text("Product Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = prodCategory,
                        onValueChange = { prodCategory = it },
                        label = { Text("Category (Watches / Audio / Footwear)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = prodPrice,
                        onValueChange = { prodPrice = it },
                        label = { Text("Price (USD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = prodStock,
                        onValueChange = { prodStock = it },
                        label = { Text("Initial Stock") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (prodTitle.isNotBlank()) {
                            val imageKey = when {
                                prodCategory.contains("Audio", ignoreCase = true) -> "audio"
                                prodCategory.contains("Foot", ignoreCase = true) -> "sneaker"
                                else -> "watch"
                            }
                            onSaveProduct(
                                Product(
                                    title = prodTitle.trim(),
                                    description = "Curated flagship release added via Aurum Admin Panel.",
                                    category = prodCategory.ifBlank { "Watches" },
                                    price = prodPrice.toDoubleOrNull() ?: 199.0,
                                    compareAtPrice = (prodPrice.toDoubleOrNull() ?: 199.0) + 50.0,
                                    stock = prodStock.toIntOrNull() ?: 20,
                                    imageKey = imageKey,
                                    supplierId = "zendrop",
                                    supplierName = "Zendrop",
                                    supplierTag = "US Warehouse • Fast Shipping 4-7 days"
                                )
                            )
                            prodTitle = ""
                            showAddProductDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack, contentColor = GoldBright)
                ) {
                    Text("Save Product")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AdminKpiBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                color = GoldBright
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = PureWhite
            )
        }
    }
}

@Composable
private fun SupplierIntegrationCard(
    supplier: Supplier,
    onConnect: (String) -> Unit,
    onDisconnect: () -> Unit,
    onUpdateToggles: (Supplier) -> Unit,
    onSyncPrices: () -> Unit,
    onSyncStock: (Boolean) -> Unit
) {
    var apiKeyInput by remember(supplier.id) { mutableStateOf("") }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("supplier_card_${supplier.id}"),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(supplier.name, style = MaterialTheme.typography.titleLarge)
                    if (supplier.specialNote.isNotBlank()) {
                        Text(
                            text = supplier.specialNote,
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldDeepAccessible
                        )
                    }
                }
                StatusDotPill(isConnected = supplier.isConnected)
            }

            if (supplier.isConnected && supplier.apiKeyMasked.isNotBlank()) {
                Text(
                    text = "Active Key: ${supplier.apiKeyMasked} • Imported Items: ${supplier.importedProductCount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Connect by API Key
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("Paste ${supplier.name} API Key") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("supplier_key_input_${supplier.id}")
                )
                Button(
                    onClick = {
                        if (supplier.isConnected && apiKeyInput.isBlank()) {
                            onDisconnect()
                        } else {
                            onConnect(apiKeyInput)
                            apiKeyInput = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ObsidianBlack,
                        contentColor = GoldBright
                    ),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("supplier_connect_btn_${supplier.id}")
                ) {
                    Text(if (supplier.isConnected && apiKeyInput.isBlank()) "Disconnect" else "Connect")
                }
            }

            HorizontalDivider()

            // 5 Automation Pipeline Controls
            SupplierSwitchRow("Auto Product Import", supplier.autoImport) {
                onUpdateToggles(supplier.copy(autoImport = it))
            }
            SupplierSwitchRow("Auto Price Sync", supplier.autoPriceSync) {
                onUpdateToggles(supplier.copy(autoPriceSync = it))
            }
            SupplierSwitchRow("Auto Stock Sync (Out of Stock Guard)", supplier.autoStockSync) {
                onUpdateToggles(supplier.copy(autoStockSync = it))
            }
            SupplierSwitchRow("Auto Order Forwarding", supplier.autoOrderForwarding) {
                onUpdateToggles(supplier.copy(autoOrderForwarding = it))
            }
            SupplierSwitchRow("Tracking Number Auto Update", supplier.autoTrackingSync) {
                onUpdateToggles(supplier.copy(autoTrackingSync = it))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSyncPrices,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync Prices")
                }
                OutlinedButton(
                    onClick = { onSyncStock(false) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync Stock")
                }
            }
        }
    }
}

@Composable
private fun SupplierSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
