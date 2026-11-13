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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.SecurityBadgeBanner
import com.example.ui.components.TrustBadgesSection
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSoftTint
import com.example.ui.theme.ObsidianBlack

@Composable
fun InfoPagesScreen(
    pageKey: String,
    defaultName: String,
    defaultEmail: String,
    onSubmitContactForm: (name: String, email: String, subject: String, message: String) -> Unit
) {
    val context = LocalContext.current
    var contactName by remember(defaultName) { mutableStateOf(defaultName) }
    var contactEmail by remember(defaultEmail) { mutableStateOf(defaultEmail) }
    var contactSubject by remember { mutableStateOf("") }
    var contactMessage by remember { mutableStateOf("") }

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
            when (pageKey) {
                "about" -> {
                    item {
                        InfoHeaderCard(
                            eyebrow = "OUR HERITAGE",
                            title = "About Aurum Luxe",
                            body = "Headquartered in New York City, Aurum Luxe pairs American industrial design with vetted US, EU, and global fulfillment partners. Every timepiece, acoustic headphone, and sneaker is crafted for effortless daily luxury and high-contrast usability."
                        )
                    }
                    item { TrustBadgesSection() }
                }
                "contact" -> {
                    item {
                        InfoHeaderCard(
                            eyebrow = "24/7 CONCIERGE",
                            title = "Contact Us",
                            body = "Reach our US customer care specialists directly via Email, WhatsApp, or the instant support form below."
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:concierge@aurumluxe.com"))
                                    runCatching { context.startActivity(intent) }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ObsidianBlack,
                                    contentColor = GoldBright
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Email Us")
                            }
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/18005550199"))
                                    runCatching { context.startActivity(intent) }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = ObsidianBlack
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("contact_whatsapp_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("WhatsApp")
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
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Send a Direct Message", style = MaterialTheme.typography.titleLarge)
                                OutlinedTextField(
                                    value = contactName,
                                    onValueChange = { contactName = it },
                                    label = { Text("Your Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = contactEmail,
                                    onValueChange = { contactEmail = it },
                                    label = { Text("Your Email") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = contactSubject,
                                    onValueChange = { contactSubject = it },
                                    label = { Text("Subject") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = contactMessage,
                                    onValueChange = { contactMessage = it },
                                    label = { Text("How can we help?") },
                                    minLines = 3,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(
                                    onClick = {
                                        onSubmitContactForm(contactName, contactEmail, contactSubject, contactMessage)
                                        contactSubject = ""
                                        contactMessage = ""
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ObsidianBlack,
                                        contentColor = GoldBright
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .minimumInteractiveComponentSize()
                                        .testTag("contact_submit_button")
                                ) {
                                    Text("Submit Inquiry", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
                "faq" -> {
                    item {
                        InfoHeaderCard(
                            eyebrow = "HELP CENTER",
                            title = "Frequently Asked Questions",
                            body = "Quick answers about US warehouse delivery, Print-on-Demand customization, payments, and returns."
                        )
                    }
                    val faqs = listOf(
                        "How fast is shipping?" to "US Warehouse items (Zendrop & Spocket US) deliver in 4-7 business days. International express takes 6-10 business days with full tracking.",
                        "Which payment methods are accepted?" to "We accept Visa, MasterCard, PayPal, and Cash on Delivery (COD) with SSL 256-bit encryption.",
                        "How do I track my order?" to "Open Account -> My Orders to watch real-time supplier forwarding and carrier tracking numbers.",
                        "Can I order custom monogrammed items?" to "Yes! Products fulfilled by CJ Dropshipping include Print-on-Demand personalization directly on the product page."
                    )
                    items(faqs) { (q, a) ->
                        FaqAccordionItem(question = q, answer = a)
                    }
                }
                "shipping" -> {
                    item {
                        InfoHeaderCard(
                            eyebrow = "LOGISTICS",
                            title = "Shipping Policy",
                            body = "• Free Express US Shipping on all orders $150+ ($12 flat rate under $150).\n• Zendrop US Warehouse orders arrive in 4-7 business days.\n• Spocket US/EU Supplier orders arrive in 3-6 business days.\n• Orders are automatically forwarded to verified fulfillment centers with live carrier tracking emailed and sent via WhatsApp as soon as dispatched."
                        )
                    }
                }
                "returns" -> {
                    item {
                        InfoHeaderCard(
                            eyebrow = "PEACE OF MIND",
                            title = "Return & Refund Policy",
                            body = "• 30-Day Hassle-Free Returns on all unworn/unused items in original packaging.\n• Defective or damaged deliveries receive an immediate free replacement or 100% refund.\n• Card and PayPal refunds process within 3-5 business days; COD refunds are issued via bank transfer or store credit."
                        )
                    }
                }
                "privacy" -> {
                    item {
                        InfoHeaderCard(
                            eyebrow = "DATA PROTECTION",
                            title = "Privacy Policy",
                            body = "• All personal data (name, email, phone, address) is protected by Zero-Trust Firebase Security Rules and SSL 256-bit encryption.\n• We never sell customer PII to third parties.\n• Shipping details are shared strictly with the assigned fulfillment supplier to dispatch your order."
                        )
                    }
                }
                else -> {
                    item {
                        InfoHeaderCard(
                            eyebrow = "LEGAL",
                            title = "Terms & Conditions",
                            body = "• By placing an order on Aurum Luxe, you agree to provide accurate delivery information and comply with applicable local import regulations.\n• Prices and stock sync in real time with our partner network; out-of-stock items cannot be purchased until restocked.\n• Custom Print-on-Demand orders begin production immediately upon checkout."
                        )
                    }
                }
            }
            item { SecurityBadgeBanner() }
        }
    }
}

@Composable
private fun InfoHeaderCard(
    eyebrow: String,
    title: String,
    body: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(shape = RoundedCornerShape(50), color = GoldSoftTint) {
                Text(
                    text = eyebrow,
                    style = MaterialTheme.typography.labelMedium,
                    color = ObsidianBlack,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Text(title, style = MaterialTheme.typography.displaySmall)
            Text(body, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun FaqAccordionItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle answer"
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.padding(top = 8.dp))
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
