package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Product
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
import com.example.ui.theme.SoftStoneBorder
import java.util.Locale

fun drawableResForImageKey(imageKey: String): Int {
    return when (imageKey.lowercase()) {
        "watch" -> R.drawable.img_product_watch
        "audio" -> R.drawable.img_product_audio
        "sneaker" -> R.drawable.img_product_sneaker
        "hero" -> R.drawable.img_hero_flagship
        else -> R.drawable.img_product_watch
    }
}

fun formatUsd(amount: Double): String {
    return String.format(Locale.US, "$%.2f", amount)
}

@Composable
fun ProductPhoto(
    imageKey: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = drawableResForImageKey(imageKey)),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AurumTopBar(
    title: String,
    showBack: Boolean = false,
    cartCount: Int = 0,
    onBackClick: () -> Unit = {},
    onSearchClick: (() -> Unit)? = null,
    onCartClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 20.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            if (showBack) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("top_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back"
                    )
                }
            }
        },
        actions = {
            if (onSearchClick != null) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("top_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search products"
                    )
                }
            }
            if (onCartClick != null) {
                IconButton(
                    onClick = onCartClick,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("top_cart_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge(
                                    containerColor = GoldPrimary,
                                    contentColor = ObsidianBlack
                                ) {
                                    Text(
                                        text = cartCount.toString(),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Shopping bag"
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun TrustBadgesSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "THE AURUM PROMISE",
            style = MaterialTheme.typography.labelMedium,
            color = GoldDeepAccessible
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TrustBadgeCard(
                icon = Icons.Default.Lock,
                title = "SSL Secure",
                subtitle = "256-Bit Encrypted",
                modifier = Modifier.weight(1f)
            )
            TrustBadgeCard(
                icon = Icons.Default.LocalShipping,
                title = "Fast Shipping",
                subtitle = "4-7 Day US Hubs",
                modifier = Modifier.weight(1f)
            )
            TrustBadgeCard(
                icon = Icons.Default.Autorenew,
                title = "Easy Returns",
                subtitle = "30-Day Guarantee",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TrustBadgeCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ObsidianBlack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = GoldBright,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ProductShowcaseCard(
    product: Product,
    isWishlisted: Boolean,
    onCardClick: () -> Unit,
    onToggleWishlist: () -> Unit,
    onQuickAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inStock = product.stock > 0
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
            ) {
                ProductPhoto(
                    imageKey = product.imageKey,
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize()
                )
                // Stock / Best Seller Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (inStock) ObsidianBlack.copy(alpha = 0.88f) else DisconnectedRed
                    ) {
                        Text(
                            text = if (inStock) {
                                if (product.isBestSeller) "BEST SELLER" else "IN STOCK (${product.stock})"
                            } else {
                                "OUT OF STOCK"
                            },
                            color = if (inStock) GoldBright else PureWhite,
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = PureWhite.copy(alpha = 0.94f),
                        shadowElevation = 2.dp
                    ) {
                        IconButton(
                            onClick = onToggleWishlist,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("wishlist_toggle_${product.id}")
                        ) {
                            Icon(
                                imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isWishlisted) "Remove from wishlist" else "Save to wishlist",
                                tint = if (isWishlisted) DisconnectedRed else ObsidianBlack
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (product.supplierTag.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GoldSoftTint,
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = product.supplierTag,
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                            color = ObsidianBlack,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${product.rating} (${product.reviewCount} reviews)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formatUsd(product.price),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (product.compareAtPrice > product.price) {
                            Text(
                                text = formatUsd(product.compareAtPrice),
                                style = MaterialTheme.typography.bodyMedium,
                                textDecoration = TextDecoration.LineThrough,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = onQuickAdd,
                        enabled = inStock,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ObsidianBlack,
                            contentColor = GoldBright,
                            disabledContainerColor = SoftStoneBorder,
                            disabledContentColor = ObsidianBlack.copy(alpha = 0.5f)
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("quick_add_${product.id}")
                    ) {
                        Text(
                            text = if (inStock) "Add to Bag" else "Sold Out",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusDotPill(
    isConnected: Boolean,
    connectedText: String = "Connected",
    disconnectedText: String = "Disconnected"
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (isConnected) ConnectedGreenBg else DisconnectedRedBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isConnected) ConnectedGreen else DisconnectedRed)
            )
            Text(
                text = if (isConnected) connectedText else disconnectedText,
                style = MaterialTheme.typography.labelMedium,
                color = if (isConnected) ConnectedGreen else DisconnectedRed
            )
        }
    }
}

@Composable
fun EmptyStateBox(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(GoldSoftTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = GoldDeepAccessible,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (actionLabel != null && onAction != null) {
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ObsidianBlack,
                        contentColor = GoldBright
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text(text = actionLabel, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun SecurityBadgeBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ObsidianBlack
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = "SSL Secure",
                tint = GoldBright,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "SSL 256-Bit Encrypted • Spam Protected • Verified US Store",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
            )
        }
    }
}
