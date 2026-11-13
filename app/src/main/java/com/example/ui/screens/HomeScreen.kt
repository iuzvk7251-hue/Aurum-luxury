package com.example.ui.screens

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Product
import com.example.data.model.ProductReview
import com.example.ui.components.ProductShowcaseCard
import com.example.ui.components.SecurityBadgeBanner
import com.example.ui.components.TrustBadgesSection
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDeepAccessible
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSoftTint
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite

@Composable
fun HomeScreen(
    products: List<Product>,
    reviews: List<ProductReview>,
    wishlistIds: Set<String>,
    onShopAllClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onToggleWishlist: (Product) -> Unit,
    onQuickAdd: (Product) -> Unit,
    onInfoPageClick: (String) -> Unit
) {
    val bestSellers = products.filter { it.isBestSeller }.ifEmpty { products.take(3) }
    val categories = listOf("All", "Watches", "Audio", "Footwear", "Accessories")

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // Search Quick Entry Bar
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onSearchClick)
                        .testTag("home_search_bar"),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = GoldDeepAccessible
                        )
                        Text(
                            text = "Search watches, studio audio, sneakers...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Big Hero Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(270.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .testTag("hero_banner")
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_flagship),
                        contentDescription = "Aurum Flagship US Collection",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        ObsidianBlack.copy(alpha = 0.88f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = GoldPrimary
                        ) {
                            Text(
                                text = "NEW ARRIVAL • US FLAGSHIP SERIES",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                                color = ObsidianBlack,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                        Text(
                            text = "ENGINEERED IN OBSIDIAN & 24K GOLD",
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 24.sp),
                            color = PureWhite
                        )
                        Text(
                            text = "Free Express US Shipping on orders $150+",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Button(
                            onClick = { onShopAllClick("All") },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = ObsidianBlack
                            ),
                            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("hero_shop_now_button")
                        ) {
                            Text(
                                text = "Shop Collection",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Categories Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Shop by Category",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = cat == "All",
                                onClick = { onShopAllClick(cat) },
                                label = {
                                    Text(
                                        text = cat,
                                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ObsidianBlack,
                                    selectedLabelColor = GoldBright,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = cat == "All",
                                    borderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.testTag("category_chip_$cat")
                            )
                        }
                    }
                }
            }

            // Best Selling Products
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "BEST SELLERS",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldDeepAccessible
                        )
                        Text(
                            text = "Most Wanted Right Now",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    TextButton(
                        onClick = { onShopAllClick("All") },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldDeepAccessible
                        )
                    }
                }
            }

            items(bestSellers, key = { it.id }) { product ->
                ProductShowcaseCard(
                    product = product,
                    isWishlisted = product.id in wishlistIds,
                    onCardClick = { onProductClick(product.id) },
                    onToggleWishlist = { onToggleWishlist(product) },
                    onQuickAdd = { onQuickAdd(product) }
                )
            }

            // Trust Badges
            item {
                TrustBadgesSection()
            }

            // Customer Reviews
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "VERIFIED US CUSTOMER REVIEWS",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldDeepAccessible
                    )
                    Text(
                        text = "Rated 4.9 / 5.0 Across 1,400+ Orders",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(reviews.take(5), key = { it.id }) { rev ->
                            Surface(
                                modifier = Modifier.width(280.dp),
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        repeat(rev.rating) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = GoldPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "\"${rev.comment}\"",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Verified buyer",
                                            tint = GoldDeepAccessible,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = rev.authorName,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Store Directory & Legal / Policy Pages
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = ObsidianBlack
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "AURUM LUXE CONCIERGE & POLICIES",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldBright
                        )
                        Text(
                            text = "Customer Care, Guarantees & Legal",
                            style = MaterialTheme.typography.titleLarge,
                            color = PureWhite
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                        val infoLinks = listOf(
                            "about" to "About Us",
                            "contact" to "Contact Us (Email & WhatsApp)",
                            "faq" to "Frequently Asked Questions (FAQ)",
                            "shipping" to "Shipping Policy",
                            "returns" to "Return & Refund Policy",
                            "privacy" to "Privacy Policy",
                            "terms" to "Terms & Conditions"
                        )
                        infoLinks.forEach { (key, label) ->
                            OutlinedButton(
                                onClick = { onInfoPageClick(key) },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("footer_link_$key")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                                        color = PureWhite
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = GoldBright,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                SecurityBadgeBanner()
            }
        }
    }
}
