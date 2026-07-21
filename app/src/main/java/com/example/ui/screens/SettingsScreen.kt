package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Loc
import com.example.ui.theme.DueOrange
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen
import com.example.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ShopViewModel,
    onResetCompleted: () -> Unit
) {
    val lang by viewModel.appLanguage.collectAsState()
    val config by viewModel.shopConfig.collectAsState()
    val isPremium = config?.isPremium == true
    val isDark by viewModel.isDarkTheme.collectAsState()
    val context = LocalContext.current

    var showTaxDialog by remember { mutableStateOf(false) }
    var taxInput by remember { mutableStateOf(config?.taxRate?.toString() ?: "0.0") }

    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf(config?.pinLock ?: "") }

    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Premium Upgrade Hero Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isPremium) ProfitGreen.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, if (isPremium) ProfitGreen else MaterialTheme.colorScheme.primary)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPremium) ProfitGreen
                            else MaterialTheme.colorScheme.primary
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "Premium",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isPremium) Loc.t("premium_active", lang) else Loc.t("premium_upgrade", lang),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isPremium) ProfitGreen else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = Loc.t("premium_sub", lang),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Demo upgrade trigger
                    Button(
                        onClick = {
                            viewModel.togglePremium()
                            Toast.makeText(
                                context,
                                if (!isPremium) "PRO Unlocked successfully!" else "Downgraded to Standard",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPremium) ExpenseRed else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("toggle_premium_btn")
                    ) {
                        Text(
                            text = if (isPremium) "Deactivate Demo" else "Demo Upgrade (FREE)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Base Configurations
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (lang == "bn") "সাধারণ কনফিগারেশন" else "General Configs",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Theme switch
                SettingsRow(
                    icon = Icons.Default.Palette,
                    title = Loc.t("theme", lang),
                    trailing = {
                        Switch(
                            checked = isDark,
                            onCheckedChange = { viewModel.isDarkTheme.value = it },
                            modifier = Modifier.testTag("theme_switch")
                        )
                    }
                )

                Divider()

                // Language selection
                SettingsRow(
                    icon = Icons.Default.Translate,
                    title = Loc.t("language", lang),
                    subtitle = if (lang == "bn") "বাংলা (Bengal)" else "English",
                    onClick = {
                        val target = if (lang == "bn") "en" else "bn"
                        viewModel.setLanguage(target)
                    }
                )

                Divider()

                // Custom Tax rate
                SettingsRow(
                    icon = Icons.Default.Percent,
                    title = Loc.t("tax", lang),
                    subtitle = "${config?.taxRate ?: 0.0}%",
                    onClick = {
                        taxInput = config?.taxRate?.toString() ?: "0.0"
                        showTaxDialog = true
                    }
                )

                Divider()

                // Passcode security update
                val hasPin = !config?.pinLock.isNullOrBlank()
                SettingsRow(
                    icon = Icons.Default.Lock,
                    title = if (lang == "bn") "পিন কোড পরিবর্তন" else "Security PIN lock",
                    subtitle = if (hasPin) "Active (সক্রিয়)" else "Disabled (নিষ্ক্রিয়)",
                    onClick = {
                        pinInput = config?.pinLock ?: ""
                        showPinDialog = true
                    }
                )
            }
        }

        // 3. Backup & Recovery Operations
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = Loc.t("backup", lang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = Loc.t("backup_desc", lang),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.backupData(context) { success, path ->
                                if (success) {
                                    Toast.makeText(context, Loc.t("backup_success", lang), Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Backup failed: $path", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("backup_data_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Loc.t("backup_btn", lang), fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.restoreData(context) { success, msg ->
                                if (success) {
                                    Toast.makeText(context, Loc.t("restore_success", lang), Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Restore failed: $msg", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("restore_data_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Loc.t("restore_btn", lang), fontSize = 12.sp)
                    }
                }
            }
        }

        // 4. Emergency Database Reset
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsRow(
                    icon = Icons.Default.DeleteForever,
                    title = if (lang == "bn") "দোকান ডাটাবেজ রিসেট" else "Reset Shop Database",
                    subtitle = if (lang == "bn") "সমস্ত বিক্রয়, পণ্য এবং হিসাব সম্পূর্ণ ডিলিট করুন" else "Delete all products, sales, and onboards permanently",
                    onClick = { showResetDialog = true },
                    titleColor = ExpenseRed,
                    iconColor = ExpenseRed
                )
            }
        }

        // 5. About Developer & Company (Bento Style Section)
        val uriHandler = LocalUriHandler.current

        Text(
            text = if (lang == "bn") "ডেভেলপার ও কোম্পানি পরিচিতি" else "Developer & Company",
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Bento Card 1: About Developer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White
            ),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(Color(0xFF4F46E5), Color(0xFF6366F1))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Prince AR Abdur Rahman",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                        Text(
                            text = if (lang == "bn") "স্বাধীন অ্যাপ ডেভেলপার" else "Independent App Developer",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Independent App Developer passionate about building modern Android applications, productivity tools, AI-powered experiences, media players, educational apps, and next-generation digital products.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Contacts & Socials Header
                Text(
                    text = if (lang == "bn") "যোগাযোগ ও সোশ্যাল মিডিয়া" else "Contact & Socials",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // WhatsApp Grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { uriHandler.openUri("https://wa.me/8801707424006") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                contentColor = if (isDark) Color(0xFF34D399) else Color(0xFF059669)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(40.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp 1", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { uriHandler.openUri("https://wa.me/8801796951709") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                contentColor = if (isDark) Color(0xFF34D399) else Color(0xFF059669)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(40.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp 2", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Facebook & Instagram
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { uriHandler.openUri("https://www.facebook.com/share/1BNn32qoJo/") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE0E7FF),
                                contentColor = Color(0xFF4F46E5)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(40.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Facebook", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { uriHandler.openUri("https://www.instagram.com/ur___abdur____rahman__2008") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFFCE7F3),
                                contentColor = Color(0xFFDB2777)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(40.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Instagram", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Bento Card 2: About Company
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White
            ),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(Color(0xFF059669), Color(0xFF10B981))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "NexVora Lab's Ofc",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                        Text(
                            text = if (lang == "bn") "উদ্ভাবনী টেক ল্যাব" else "Innovative Tech Lab",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF059669)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "NexVora Lab's Ofc focuses on creating innovative Android applications designed to improve productivity, entertainment, learning, and digital experiences.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Mission block (Bento item style)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF0FDF4))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = (if (lang == "bn") "আমাদের লক্ষ্য" else "OUR MISSION").uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF059669),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Build fast, beautiful, privacy-friendly, and user-focused applications accessible to everyone.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Bento Card 3: Technical Information & Credits
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White
            ),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Technical Info Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (lang == "bn") "প্রযুক্তিগত তথ্য" else "Technical Information",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "v1.0.0",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Divider(color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))

                // Credits Section
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = (if (lang == "bn") "কৃতিত্ব" else "CREDITS").uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (lang == "bn") "ডেভেলপার" else "Developer", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(text = "Prince AR Abdur Rahman", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1E293B))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (lang == "bn") "প্রকাশক" else "Publisher", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(text = "NexVora Lab's Ofc", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1E293B))
                    }
                }

                Divider(color = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9))

                // Copyright footer
                Text(
                    text = "© 2026 NexVora Lab's Ofc. All Rights Reserved.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // 1. Tax Dialog
    if (showTaxDialog) {
        AlertDialog(
            onDismissRequest = { showTaxDialog = false },
            title = { Text(Loc.t("tax", lang)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = if (lang == "bn") "ট্যাক্স হার সেট করুন (শতকরা %):" else "Set custom tax percentage (%):", fontSize = 12.sp)
                    OutlinedTextField(
                        value = taxInput,
                        onValueChange = { taxInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rate = taxInput.toDoubleOrNull() ?: 0.0
                        viewModel.setTaxRate(rate)
                        showTaxDialog = false
                    }
                ) {
                    Text(Loc.t("save", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTaxDialog = false }) {
                    Text(Loc.t("cancel", lang))
                }
            }
        )
    }

    // 2. PIN Lock Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("PIN Lock Passcode") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = Loc.t("pin_lock_desc", lang), fontSize = 12.sp)
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                pinInput = it
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("e.g. 1234") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updatePinLock(pinInput.ifBlank { null })
                        showPinDialog = false
                    }
                ) {
                    Text(Loc.t("save", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text(Loc.t("cancel", lang))
                }
            }
        )
    }

    // 3. Reset Database confirmation
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(if (lang == "bn") "ডাটাবেজ সম্পূর্ণ রিসেট" else "Reset Entire Shop Database?") },
            text = { Text(if (lang == "bn") "সতর্কতা: এটি আপনার দোকান প্রোফাইল, সমস্ত বিক্রিত রসিদ এবং পণ্য চিরতরে ডিলিট করে দেবে!" else "Warning: This will delete your shop config, products, customers, invoices, and expenses permanently. This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetShop()
                        showResetDialog = false
                        onResetCompleted()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(Loc.t("cancel", lang))
                }
            }
        )
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    iconColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = titleColor)
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        }

        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Go", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
