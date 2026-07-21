package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.ShopViewModel

@Composable
fun PinLockScreen(
    viewModel: ShopViewModel,
    onVerified: () -> Unit
) {
    val config by viewModel.shopConfig.collectAsState()
    val targetPin = config?.pinLock ?: ""
    val lang by viewModel.appLanguage.collectAsState()

    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock",
                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (lang == "bn") "আপনার পিন কোড লিখুন" else "Enter your PIN Code",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

            // PIN Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val maxLength = targetPin.length
                for (i in 0 until maxLength) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                if (isError) MaterialTheme.colorScheme.error
                                else if (isFilled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            if (isError) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (lang == "bn") "ভুল পিন কোড! আবার চেষ্টা করুন।" else "Incorrect PIN! Try again.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Numeric Keypad
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "delete")
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    row.forEach { item ->
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    if (item.isEmpty()) MaterialTheme.colorScheme.background
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable(enabled = item.isNotEmpty()) {
                                    isError = false
                                    if (item == "delete") {
                                        if (enteredPin.isNotEmpty()) {
                                            enteredPin = enteredPin.dropLast(1)
                                        }
                                    } else {
                                        if (enteredPin.length < targetPin.length) {
                                            enteredPin += item
                                            if (enteredPin.length == targetPin.length) {
                                                if (enteredPin == targetPin) {
                                                    onVerified()
                                                } else {
                                                    isError = true
                                                    enteredPin = ""
                                                }
                                            }
                                        }
                                    }
                                }
                                .testTag("pin_btn_$item"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item == "delete") {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Backspace",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            } else if (item.isNotEmpty()) {
                                Text(
                                    text = item,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
