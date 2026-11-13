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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.ProductReview
import com.example.ui.components.ProductPhoto
import com.example.ui.components.TrustBadgesSection
import com.example.ui.components.formatUsd
import com.example.ui.theme.ConnectedGreen
import com.example.ui.theme.ConnectedGreenBg
import com.example.ui.theme.DisconnectedRed
import com.example.ui.theme.DisconnectedRedBg
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDeepAccessible
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSoftTint
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite

@Composable
fun ProductDetailScreen(
    product: Product,
    reviews: List<ProductReview>,
    isWishlisted: Boolean,
    onToggleWishlist: () -> Unit,
    onAddToCart: (variant: String, podNote: String) -> Unit,
    onBuyNow: (variant: String, podNote: String) -> Unit,
    onSubmitReview: (rating: Int, comment: String) -> Unit,
    onSubmitSourcingRequest: (supplierId: String, supplierName: String, requestType: String, notes: String) -> Unit
) {
    val variants = listOf("Obsidian / Gold", "Pure White / Gold", "Stealth Matte Black")
    var selectedVariant by remember { mutableStateOf(variants.first()) }
    var podCustomText by remember { mutableStateOf("") }

    var showSourcingDialog by remember { mutableStateOf(false) }
    var sourcingNotes by remember { mutableStateOf("") }
    var sourcingRequestType by remember { mutableStateOf("Sourcing Request") }

    var newReviewRating by remember { mutableIntStateOf(5) }
    var newReviewComment by remember { mutableStateOf("") }

    val inStock = product.stock > 0
    val productReviews = reviews.filter { it.productId == product.id }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Big Product Image
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    ProductPhoto(
                        imageKey = product.imageKey,
                        contentDescription = product.title,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp),
                        shape = RoundedCornerShape(50),
                        color = PureWhite.copy(alpha = 0.94f),
                        shadowElevation = 3.dp
                    ) {
                        IconButton(
                            onClick = onToggleWishlist,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("detail_wishlist_button")
                        ) {
                            Icon(
                                imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Toggle Wishlist",
                                tint = if (isWishlisted) DisconnectedRed else ObsidianBlack
                            )
                        }
                    }
                }
            }

            // Title, Price, Stock Status, Supplier Specials
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (inStock) ConnectedGreenBg else DisconnectedRedBg
                        ) {
                            Text(
                                text = if (inStock) "In Stock (${product.stock} ready to ship)" else "Out of Stock",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (inStock) ConnectedGreen else DisconnectedRed,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = GoldSoftTint
                        ) {
                            Text(
                                text = product.supplierName,
                                style = MaterialTheme.typography.labelMedium,
                                color = ObsidianBlack,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Special Tags (Zendrop US Warehouse / Spocket US-EU / CJ POD)
                    if (product.supplierTag.isNotBlank() || product.supplierId == "zendrop" || product.supplierId == "spocket") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = ObsidianBlack
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = GoldBright
                                )
                                Column {
                                    Text(
                                        text = when (product.supplierId) {
                                            "zendrop" -> "US Warehouse • Fast Shipping 4-7 Days"
                                            "spocket" -> "US / EU Supplier • Premium Quality Verified"
                                            "cj_dropshipping" -> "CJ Dropshipping • Print on Demand & Sourcing"
                                            else -> product.supplierTag
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        color = GoldBright
                                    )
                                    Text(
                                        text = "Est. Delivery: ${product.shippingEstimate}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PureWhite.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = product.title,
                        style = MaterialTheme.typography.displaySmall
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = formatUsd(product.price),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        if (product.compareAtPrice > product.price) {
                            Text(
                                text = formatUsd(product.compareAtPrice),
                                style = MaterialTheme.typography.titleMedium,
                                textDecoration = TextDecoration.LineThrough,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = GoldPrimary)
                        Text(
                            text = "${product.rating} / 5.0 • ${product.reviewCount + productReviews.size} Customer Reviews",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Finish Variant Selection
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select Edition Finish",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        variants.forEach { variant ->
                            FilterChip(
                                selected = selectedVariant == variant,
                                onClick = { selectedVariant = variant },
                                label = {
                                    Text(
                                        text = variant,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ObsidianBlack,
                                    selectedLabelColor = GoldBright
                                )
                            )
                        }
                    }
                }
            }

            // CJ Dropshipping Special: Print on Demand + Sourcing Request
            if (product.supportsPrintOnDemand || product.supplierId == "cj_dropshipping") {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.5.dp, GoldPrimary)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Brush, contentDescription = null, tint = GoldDeepAccessible)
                                Text(
                                    text = "CJ Print on Demand Personalization",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            OutlinedTextField(
                                value = podCustomText,
                                onValueChange = { podCustomText = it },
                                label = { Text("Custom Monogram / Print Text (Optional)") },
                                placeholder = { Text("e.g., J.D. 24K GOLD") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("cj_pod_input")
                            )
                            OutlinedButton(
                                onClick = {
                                    sourcingRequestType = "Sourcing Request"
                                    sourcingNotes = "Custom sourcing quote requested for ${product.title}"
                                    showSourcingDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("cj_sourcing_request_button")
                            ) {
                                Icon(Icons.Default.RequestQuote, contentDescription = null, tint = GoldDeepAccessible)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("CJ Sourcing Request", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            // Spocket Special: Sample Order Button
            if (product.supportsSampleOrder || product.supplierId == "spocket") {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = GoldSoftTint,
                        border = BorderStroke(1.dp, GoldPrimary)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Spocket US / EU Supplier • Premium Quality",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Inspect craftsmanship directly before bulk ordering with a fast US/EU Sample Order.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            OutlinedButton(
                                onClick = {
                                    sourcingRequestType = "Sample Order"
                                    sourcingNotes = "Sample order for ${product.title} ($selectedVariant)"
                                    showSourcingDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("spocket_sample_order_button")
                            ) {
                                Icon(Icons.Default.Inventory2, contentDescription = null, tint = ObsidianBlack)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Order Sample Unit", style = MaterialTheme.typography.labelLarge, color = ObsidianBlack)
                            }
                        }
                    }
                }
            }

            // Primary Big CTA Buttons: Add to Cart & Buy Now
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { onAddToCart(selectedVariant, podCustomText.trim()) },
                        enabled = inStock,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ObsidianBlack,
                            contentColor = GoldBright
                        ),
                        contentPadding = PaddingValues(vertical = 18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("detail_add_to_cart_button")
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (inStock) "Add to Bag — ${formatUsd(product.price)}" else "Out of Stock",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp)
                        )
                    }

                    Button(
                        onClick = { onBuyNow(selectedVariant, podCustomText.trim()) },
                        enabled = inStock,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = ObsidianBlack
                        ),
                        contentPadding = PaddingValues(vertical = 18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("detail_buy_now_button")
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (inStock) "Buy Now • Express Checkout" else "Currently Unavailable",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp)
                        )
                    }
                }
            }

            item { TrustBadgesSection() }

            // Customer Reviews & Submit Review
            item {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Customer Reviews",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            if (productReviews.isEmpty()) {
                item {
                    Text(
                        text = "Be the first to review ${product.title}.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(productReviews, key = { it.id }) { rev ->
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                repeat(rev.rating) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = GoldDeepAccessible,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(rev.authorName, style = MaterialTheme.typography.labelMedium)
                            }
                            Text(rev.comment, style = MaterialTheme.typography.bodyLarge)
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
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Write a Verified Review", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = { newReviewRating = star },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "$star stars",
                                        tint = if (star <= newReviewRating) GoldPrimary else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = newReviewComment,
                            onValueChange = { newReviewComment = it },
                            label = { Text("Share your experience") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                if (newReviewComment.isNotBlank()) {
                                    onSubmitReview(newReviewRating, newReviewComment)
                                    newReviewComment = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ObsidianBlack,
                                contentColor = GoldBright
                            ),
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Text("Publish Review", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }

    if (showSourcingDialog) {
        AlertDialog(
            onDismissRequest = { showSourcingDialog = false },
            title = { Text("$sourcingRequestType — ${product.supplierName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Product: ${product.title}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    OutlinedTextField(
                        value = sourcingNotes,
                        onValueChange = { sourcingNotes = it },
                        label = { Text("Requirements / Delivery Notes") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSubmitSourcingRequest(
                            product.supplierId,
                            product.supplierName,
                            sourcingRequestType,
                            sourcingNotes
                        )
                        showSourcingDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ObsidianBlack,
                        contentColor = GoldBright
                    )
                ) {
                    Text("Submit Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSourcingDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
