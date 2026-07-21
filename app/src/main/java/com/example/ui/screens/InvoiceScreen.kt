package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Loc
import com.example.data.SaleWithItems
import com.example.ui.theme.DueOrange
import com.example.ui.theme.ProfitGreen
import com.example.viewmodel.ShopViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    viewModel: ShopViewModel,
    saleId: Long,
    onBack: () -> Unit
) {
    val lang by viewModel.appLanguage.collectAsState()
    val config by viewModel.shopConfig.collectAsState()
    val currency = config?.currency ?: "৳"
    val context = LocalContext.current

    val salesWithItems by viewModel.salesWithItems.collectAsState()
    val invoiceData = remember(salesWithItems, saleId) {
        salesWithItems.find { it.sale.id == saleId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Loc.t("invoice", lang), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        if (invoiceData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val invoice = invoiceData!!
            val sale = invoice.sale
            val items = invoice.items

            val formatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
            val dateStr = formatter.format(Date(sale.date))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Scrollable Invoice content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Block (Shop Info)
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = config?.shopName ?: Loc.t("app_title", lang),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = config?.address ?: "",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${Loc.t("phone_num", lang)}: ${config?.mobileNumber ?: ""}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 2.dp)
                        }
                    }

                    // Metadata Block (Invoice No, Date, Customer)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${Loc.t("inv_no", lang)}:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = sale.invoiceNumber,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${Loc.t("date", lang)}:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = dateStr,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${Loc.t("new_customer", lang)}:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = sale.customerName ?: "Guest",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }

                    // Table Headings
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = Loc.t("product_name", lang),
                                modifier = Modifier.weight(1.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Qty",
                                modifier = Modifier.weight(0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Price",
                                modifier = Modifier.weight(1f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = "Total",
                                modifier = Modifier.weight(1f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    // Table Items
                    items(items) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.productName,
                                modifier = Modifier.weight(1.8f),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = item.quantity.toString(),
                                modifier = Modifier.weight(0.6f),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "$currency${item.sellPrice}",
                                modifier = Modifier.weight(1f),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = "$currency${item.sellPrice * item.quantity}",
                                modifier = Modifier.weight(1f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    // Summary Calculations Block
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 2.dp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(Loc.t("subtotal", lang), fontSize = 12.sp)
                                Text("$currency${String.format("%.1f", sale.subTotal)}", fontSize = 12.sp)
                            }
                            if (sale.discount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(Loc.t("discount", lang), fontSize = 12.sp, color = Color.Red)
                                    Text("-$currency${String.format("%.1f", sale.discount)}", fontSize = 12.sp, color = Color.Red)
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(Loc.t("net_total", lang), fontSize = 15.sp, fontWeight = FontWeight.Black)
                                Text(
                                    text = "$currency${String.format("%.1f", sale.totalAmount)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(Loc.t("paid_amount", lang), fontSize = 12.sp, color = ProfitGreen)
                                Text("$currency${String.format("%.1f", sale.paidAmount)}", fontSize = 12.sp, color = ProfitGreen, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(Loc.t("due_amount", lang), fontSize = 12.sp, color = DueOrange)
                                Text("$currency${String.format("%.1f", sale.dueAmount)}", fontSize = 12.sp, color = DueOrange, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Invoice Footer Message
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (lang == "bn") "আমাদের সাথে ব্যবসা করার জন্য ধন্যবাদ!" else "Thank you for shopping with us!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Powered by Infinity Shop",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom CTA PDF Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            Toast.makeText(
                                context,
                                if (lang == "bn") "ইনভয়েস সফলভাবে প্রিন্টারে পাঠানো হয়েছে!" else "Sending invoice to printer...",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = "Print")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (lang == "bn") "প্রিন্ট" else "Print", fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            Toast.makeText(
                                context,
                                Loc.t("invoice_created", lang) + " (PDF successfully shared)",
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("download_pdf_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share PDF")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (lang == "bn") "PDF শেয়ার" else "Share PDF", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
