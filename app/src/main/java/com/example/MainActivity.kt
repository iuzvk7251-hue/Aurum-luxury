package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.auth.signOutUser
import com.example.data.repository.StoreRepository
import com.example.ui.components.AurumTopBar
import com.example.ui.screens.AccountOrdersWishlistScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CartAndCheckoutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InfoPagesScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ShopAndSearchScreen
import com.example.ui.theme.AurumLuxeTheme
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ObsidianBlack
import com.example.ui.viewmodel.StoreViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object StoreRoutes {
    const val HOME = "home"
    const val SHOP = "shop/{category}"
    const val PRODUCT = "product/{productId}"
    const val CART = "cart"
    const val ACCOUNT = "account/{tab}"
    const val ADMIN = "admin"
    const val INFO = "info/{pageKey}"

    fun shopRoute(category: String = "All") = "shop/$category"
    fun productRoute(productId: String) = "product/$productId"
    fun accountRoute(tab: Int = 0) = "account/$tab"
    fun infoRoute(pageKey: String) = "info/$pageKey"
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AurumLuxeTheme {
                AppRootNavigation()
            }
        }
    }
}

@Composable
fun AppRootNavigation() {
    val auth = Firebase.auth
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        key(user.uid) {
            AuthenticatedStoreShell(
                currentUserId = user.uid,
                userEmail = user.email ?: "member@aurumluxe.com",
                userDisplayName = user.displayName ?: "Aurum Member"
            )
        }
    }
}

@Composable
fun AuthenticatedStoreShell(
    currentUserId: String,
    userEmail: String,
    userDisplayName: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: StoreViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                StoreViewModel(
                    repository = StoreRepository(db),
                    currentUserId = currentUserId,
                    userEmail = userEmail,
                    userDisplayName = userDisplayName
                )
            }
        }
    )

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: StoreRoutes.HOME

    val products by viewModel.products.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val wishlistItems by viewModel.wishlistItems.collectAsStateWithLifecycle()
    val userOrders by viewModel.userOrders.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val sourcingRequests by viewModel.sourcingRequests.collectAsStateWithLifecycle()
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val lastNotification by viewModel.lastOrderNotification.collectAsStateWithLifecycle()

    val wishlistIds = remember(wishlistItems) { wishlistItems.map { it.productId }.toSet() }
    val cartCount = remember(cartItems) { cartItems.sumOf { it.quantity } }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            snackbarHostState.showSnackbar(toastMessage!!)
            viewModel.clearToast()
        }
    }

    val topTitle = when {
        currentRoute == StoreRoutes.HOME -> "AURUM LUXE"
        currentRoute.startsWith("shop") -> "Shop Collection"
        currentRoute.startsWith("product") -> "Product Details"
        currentRoute == StoreRoutes.CART -> "Bag & Checkout"
        currentRoute.startsWith("account") -> "Orders & Account"
        currentRoute == StoreRoutes.ADMIN -> "Dropshipping Admin"
        currentRoute.startsWith("info") -> "Aurum Concierge"
        else -> "AURUM LUXE"
    }

    val showBack = currentRoute.startsWith("product") || currentRoute.startsWith("info")

    val navItems = listOf(
        Triple("Home", StoreRoutes.HOME, Icons.Filled.Home to Icons.Outlined.Home),
        Triple("Shop", StoreRoutes.shopRoute("All"), Icons.Filled.Storefront to Icons.Outlined.Storefront),
        Triple("Bag", StoreRoutes.CART, Icons.Filled.ShoppingBag to Icons.Outlined.ShoppingBag),
        Triple("Account", StoreRoutes.accountRoute(0), Icons.Filled.Person to Icons.Outlined.Person),
        Triple("Admin", StoreRoutes.ADMIN, Icons.Filled.AdminPanelSettings to Icons.Outlined.AdminPanelSettings)
    )

    fun isNavSelected(routePattern: String): Boolean {
        return when {
            routePattern == StoreRoutes.HOME -> currentRoute == StoreRoutes.HOME
            routePattern.startsWith("shop") -> currentRoute.startsWith("shop")
            routePattern == StoreRoutes.CART -> currentRoute == StoreRoutes.CART
            routePattern.startsWith("account") -> currentRoute.startsWith("account")
            routePattern == StoreRoutes.ADMIN -> currentRoute == StoreRoutes.ADMIN
            else -> false
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 700.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                AurumTopBar(
                    title = topTitle,
                    showBack = showBack,
                    cartCount = cartCount,
                    onBackClick = { navController.popBackStack() },
                    onSearchClick = { navController.navigate(StoreRoutes.shopRoute("All")) },
                    onCartClick = { navController.navigate(StoreRoutes.CART) }
                )
            },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        modifier = Modifier.navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        navItems.forEach { (label, targetRoute, icons) ->
                            val selected = isNavSelected(targetRoute)
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(targetRoute) {
                                        launchSingleTop = true
                                    }
                                },
                                icon = {
                                    if (label == "Bag" && cartCount > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge(containerColor = GoldPrimary, contentColor = ObsidianBlack) {
                                                    Text(cartCount.toString())
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (selected) icons.first else icons.second,
                                                contentDescription = label
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = if (selected) icons.first else icons.second,
                                            contentDescription = label
                                        )
                                    }
                                },
                                label = { Text(label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = GoldBright,
                                    indicatorColor = ObsidianBlack,
                                    selectedTextColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.testTag("nav_tab_${label.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        navItems.forEach { (label, targetRoute, icons) ->
                            val selected = isNavSelected(targetRoute)
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(targetRoute) {
                                        launchSingleTop = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) icons.first else icons.second,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    NavHost(
                        navController = navController,
                        startDestination = StoreRoutes.HOME
                    ) {
                        composable(StoreRoutes.HOME) {
                            HomeScreen(
                                products = products,
                                reviews = reviews,
                                wishlistIds = wishlistIds,
                                onShopAllClick = { cat -> navController.navigate(StoreRoutes.shopRoute(cat)) },
                                onSearchClick = { navController.navigate(StoreRoutes.shopRoute("All")) },
                                onProductClick = { id -> navController.navigate(StoreRoutes.productRoute(id)) },
                                onToggleWishlist = { viewModel.toggleWishlist(it) },
                                onQuickAdd = { viewModel.addToCart(it) },
                                onInfoPageClick = { key -> navController.navigate(StoreRoutes.infoRoute(key)) }
                            )
                        }

                        composable(
                            route = StoreRoutes.SHOP,
                            arguments = listOf(navArgument("category") { type = NavType.StringType })
                        ) { entry ->
                            val cat = entry.arguments?.getString("category") ?: "All"
                            ShopAndSearchScreen(
                                products = products,
                                wishlistIds = wishlistIds,
                                initialCategory = cat,
                                onProductClick = { id -> navController.navigate(StoreRoutes.productRoute(id)) },
                                onToggleWishlist = { viewModel.toggleWishlist(it) },
                                onQuickAdd = { viewModel.addToCart(it) }
                            )
                        }

                        composable(
                            route = StoreRoutes.PRODUCT,
                            arguments = listOf(navArgument("productId") { type = NavType.StringType })
                        ) { entry ->
                            val prodId = entry.arguments?.getString("productId") ?: ""
                            val product = products.firstOrNull { it.id == prodId } ?: products.first()
                            ProductDetailScreen(
                                product = product,
                                reviews = reviews,
                                isWishlisted = product.id in wishlistIds,
                                onToggleWishlist = { viewModel.toggleWishlist(product) },
                                onAddToCart = { variant, podNote ->
                                    viewModel.addToCart(product, 1, variant, podNote)
                                },
                                onBuyNow = { variant, podNote ->
                                    viewModel.addToCart(product, 1, variant, podNote)
                                    navController.navigate(StoreRoutes.CART)
                                },
                                onSubmitReview = { rating, comment ->
                                    viewModel.submitReview(product.id, rating, comment)
                                },
                                onSubmitSourcingRequest = { supId, supName, reqType, notes ->
                                    viewModel.submitSourcingOrSampleRequest(
                                        supplierId = supId,
                                        supplierName = supName,
                                        requestType = reqType,
                                        productTitle = product.title,
                                        notes = notes
                                    )
                                }
                            )
                        }

                        composable(StoreRoutes.CART) {
                            CartAndCheckoutScreen(
                                cartItems = cartItems,
                                userProfile = userProfile,
                                defaultName = userDisplayName,
                                defaultEmail = userEmail,
                                lastOrderNotification = lastNotification,
                                onUpdateQty = { id, qty -> viewModel.updateCartQuantity(id, qty) },
                                onPlaceOrder = { name, email, phone, address, method ->
                                    viewModel.placeCheckoutOrder(
                                        customerName = name,
                                        customerEmail = email,
                                        customerPhone = phone,
                                        shippingAddress = address,
                                        paymentMethod = method,
                                        onComplete = {}
                                    )
                                },
                                onDismissNotification = { viewModel.dismissOrderNotification() },
                                onBrowseShop = { navController.navigate(StoreRoutes.shopRoute("All")) },
                                onViewOrders = { navController.navigate(StoreRoutes.accountRoute(0)) }
                            )
                        }

                        composable(
                            route = StoreRoutes.ACCOUNT,
                            arguments = listOf(navArgument("tab") { type = NavType.IntType })
                        ) { entry ->
                            val initialTab = entry.arguments?.getInt("tab") ?: 0
                            AccountOrdersWishlistScreen(
                                userProfile = userProfile,
                                userEmail = userEmail,
                                userName = userDisplayName,
                                isAdmin = viewModel.isAdmin,
                                orders = userOrders,
                                wishlist = wishlistItems,
                                products = products,
                                initialTab = initialTab,
                                onSaveProfile = { n, p, a -> viewModel.updateProfile(n, p, a) },
                                onCancelOrder = { id -> viewModel.cancelOrder(id) },
                                onProductClick = { id -> navController.navigate(StoreRoutes.productRoute(id)) },
                                onRemoveWishlist = { prod -> viewModel.toggleWishlist(prod) },
                                onOpenAdminPanel = { navController.navigate(StoreRoutes.ADMIN) },
                                onOpenInfoPage = { key -> navController.navigate(StoreRoutes.infoRoute(key)) },
                                onSignOut = { signOutUser(credentialManager, scope) }
                            )
                        }

                        composable(StoreRoutes.ADMIN) {
                            AdminDashboardScreen(
                                isAdmin = viewModel.isAdmin,
                                suppliers = suppliers,
                                products = products,
                                orders = allOrders,
                                customers = customers,
                                sourcingRequests = sourcingRequests,
                                onConnectSupplier = { sup, key -> viewModel.connectSupplierWithApiKey(sup, key) },
                                onDisconnectSupplier = { sup -> viewModel.disconnectSupplier(sup) },
                                onUpdateSupplierToggles = { sup -> viewModel.updateSupplierAutomations(sup) },
                                onSyncPrices = { sup -> viewModel.syncSupplierPrices(sup) },
                                onSyncStock = { sup, simOut -> viewModel.syncSupplierStock(sup, simOut) },
                                onAddCustomSupplier = { n, c, k, note -> viewModel.addNewCustomSupplier(n, c, k, note) },
                                onSaveProduct = { prod -> viewModel.adminSaveProduct(prod) },
                                onDeleteProduct = { id -> viewModel.adminDeleteProduct(id) },
                                onForwardAndSyncTracking = { order -> viewModel.adminForwardAndGenerateTracking(order) }
                            )
                        }

                        composable(
                            route = StoreRoutes.INFO,
                            arguments = listOf(navArgument("pageKey") { type = NavType.StringType })
                        ) { entry ->
                            val key = entry.arguments?.getString("pageKey") ?: "about"
                            InfoPagesScreen(
                                pageKey = key,
                                defaultName = userDisplayName,
                                defaultEmail = userEmail,
                                onSubmitContactForm = { n, e, s, m ->
                                    viewModel.submitSupportTicket(n, e, s, m)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
