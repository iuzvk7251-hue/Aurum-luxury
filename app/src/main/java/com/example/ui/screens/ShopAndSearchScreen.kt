package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.components.EmptyStateBox
import com.example.ui.components.ProductShowcaseCard
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldDeepAccessible
import com.example.ui.theme.ObsidianBlack

@Composable
fun ShopAndSearchScreen(
    products: List<Product>,
    wishlistIds: Set<String>,
    initialCategory: String = "All",
    onProductClick: (String) -> Unit,
    onToggleWishlist: (Product) -> Unit,
    onQuickAdd: (Product) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember(initialCategory) { mutableStateOf(initialCategory) }
    var selectedSupplierFilter by remember { mutableStateOf("All Suppliers") }

    val categories = listOf("All", "Watches", "Audio", "Footwear", "Accessories")
    val supplierFilters = listOf("All Suppliers", "US Warehouse", "US / EU Supplier", "Print on Demand")

    val filteredProducts = remember(products, searchQuery, selectedCategory, selectedSupplierFilter) {
        products.filter { p ->
            val matchesCat = selectedCategory == "All" || p.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                p.title.contains(searchQuery, ignoreCase = true) ||
                p.description.contains(searchQuery, ignoreCase = true) ||
                p.supplierName.contains(searchQuery, ignoreCase = true)
            val matchesSupplierTag = when (selectedSupplierFilter) {
                "US Warehouse" -> p.supplierTag.contains("US Warehouse", ignoreCase = true) || p.supplierId == "zendrop"
                "US / EU Supplier" -> p.supplierTag.contains("US / EU", ignoreCase = true) || p.supplierId == "spocket"
                "Print on Demand" -> p.supportsPrintOnDemand || p.supplierId == "cj_dropshipping"
                else -> true
            }
            matchesCat && matchesSearch && matchesSupplierTag
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Shop All Products",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search by product, brand, or supplier...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ObsidianBlack,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shop_search_input")
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            val selected = selectedCategory == cat
                            FilterChip(
                                selected = selected,
                                onClick = { selectedCategory = cat },
                                label = {
                                    Text(
                                        text = cat,
                                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ObsidianBlack,
                                    selectedLabelColor = GoldBright
                                )
                            )
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(supplierFilters) { filter ->
                            val selected = selectedSupplierFilter == filter
                            FilterChip(
                                selected = selected,
                                onClick = { selectedSupplierFilter = filter },
                                label = {
                                    Text(
                                        text = filter,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldDeepAccessible,
                                    selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                                )
                            )
                        }
                    }

                    Text(
                        text = "${filteredProducts.size} Products Available",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (filteredProducts.isEmpty()) {
                item {
                    EmptyStateBox(
                        icon = Icons.Default.ShoppingBag,
                        title = "No Matching Products",
                        subtitle = "Try clearing your search or switching category filters.",
                        actionLabel = "Reset Filters",
                        onAction = {
                            searchQuery = ""
                            selectedCategory = "All"
                            selectedSupplierFilter = "All Suppliers"
                        }
                    )
                }
            } else {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductShowcaseCard(
                        product = product,
                        isWishlisted = product.id in wishlistIds,
                        onCardClick = { onProductClick(product.id) },
                        onToggleWishlist = { onToggleWishlist(product) },
                        onQuickAdd = { onQuickAdd(product) }
                    )
                }
            }
        }
    }
}
