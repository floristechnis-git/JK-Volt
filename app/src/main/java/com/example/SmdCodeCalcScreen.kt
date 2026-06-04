package com.example

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun SmdCodeCalcScreen(
    textSizeMultiplier: Float = 1.0f,
    currentLanguage: String = "en"
) {
    val scrollState = rememberScrollState()

    var inputCode by remember { mutableStateOf("") }
    var referenceExpanded by remember { mutableStateOf(false) }

    // Table mapping EIA-96 Numeric Code (01-96) to its Base value
    val eia96Codes = remember {
        mapOf(
            "01" to 100, "02" to 102, "03" to 105, "04" to 107, "05" to 110, "06" to 113, "07" to 115, "08" to 118, "09" to 121, "10" to 124,
            "11" to 127, "12" to 130, "13" to 133, "14" to 137, "15" to 140, "16" to 143, "17" to 147, "18" to 150, "19" to 154, "20" to 158,
            "21" to 162, "22" to 165, "23" to 169, "24" to 174, "25" to 178, "26" to 182, "27" to 187, "28" to 191, "29" to 196, "30" to 200,
            "31" to 205, "32" to 210, "33" to 215, "34" to 221, "35" to 226, "36" to 232, "37" to 237, "38" to 243, "39" to 249, "40" to 255,
            "41" to 261, "42" to 267, "43" to 274, "44" to 280, "45" to 287, "46" to 294, "47" to 301, "48" to 309, "49" to 316, "50" to 324,
            "51" to 332, "52" to 340, "53" to 348, "54" to 357, "55" to 365, "56" to 374, "57" to 383, "58" to 392, "59" to 402, "60" to 412,
            "61" to 422, "62" to 432, "63" to 442, "64" to 453, "65" to 464, "66" to 475, "67" to 487, "68" to 499, "69" to 511, "70" to 523,
            "71" to 536, "72" to 549, "73" to 562, "74" to 576, "75" to 590, "76" to 604, "77" to 619, "78" to 634, "79" to 649, "80" to 665,
            "81" to 681, "82" to 698, "83" to 715, "84" to 732, "85" to 750, "86" to 768, "87" to 787, "88" to 806, "89" to 825, "90" to 845,
            "91" to 866, "92" to 887, "93" to 909, "94" to 931, "95" to 953, "96" to 976
        )
    }

    // Table mapping EIA-96 Letter Multiplier to its numeric factor
    val eia96Letters = remember {
        mapOf(
            'Z' to 0.001, 'Y' to 0.01, 'R' to 0.01, 'X' to 0.1, 'S' to 0.1,
            'A' to 1.0, 'B' to 10.0, 'H' to 10.0, 'C' to 100.0,
            'D' to 1000.0, 'E' to 10000.0, 'F' to 100000.0
        )
    }

    // Dynamic SMD code solver and output format generator
    val outputState = remember(inputCode) {
        val code = inputCode.trim().uppercase(Locale.US)
        if (code.isEmpty()) return@remember Pair("-- Ω", null)

        val len = code.length

        // 1. Check for standard 3-digit Code
        val threeDigitRegex = Regex("^(\\d{2})(\\d{1})$")
        val threeDigitMatch = threeDigitRegex.matchEntire(code)

        // 1b. Checking decimal variants (e.g. 4R7, R22)
        val hasR = code.contains('R')

        if (len == 3 && threeDigitMatch != null && !hasR) {
            val base = code.substring(0, 2).toDoubleOrNull() ?: 10.0
            val exponent = code.substring(2, 3).toIntOrNull() ?: 0
            val resistance = base * Math.pow(10.0, exponent.toDouble())
            return@remember Pair(friendlyResistance(resistance), "±5% (Standard 3-Digit)")
        }

        // 2. Check for standard 4-digit Code
        val fourDigitRegex = Regex("^(\\d{3})(\\d{1})$")
        val fourDigitMatch = fourDigitRegex.matchEntire(code)

        if (len == 4 && fourDigitMatch != null && !hasR) {
            val base = code.substring(0, 3).toDoubleOrNull() ?: 100.0
            val exponent = code.substring(3, 4).toIntOrNull() ?: 0
            val resistance = base * Math.pow(10.0, exponent.toDouble())
            return@remember Pair(friendlyResistance(resistance), "±1% (Precision 4-Digit)")
        }

        // 3. Check for decimal values containing R
        if (hasR && (len == 3 || len == 4)) {
            val segments = code.split('R')
            if (segments.size == 2) {
                val left = segments[0]
                val right = segments[1]
                val leftVal = if (left.isEmpty()) "0" else left
                val rightVal = if (right.isEmpty()) "0" else right
                val combined = "$leftVal.$rightVal".toDoubleOrNull()
                if (combined != null) {
                    val tol = if (len == 3) "±5%" else "±1%"
                    return@remember Pair(friendlyResistance(combined), "$tol (Decimal Marking)")
                }
            } else if (code == "R") {
                return@remember Pair("-- Ω", null)
            }
        }

        // 4. Check for EIA-96 Code (2 Digits + 1 Letter)
        val eia96Regex = Regex("^(\\d{2})([A-Z])$")
        val eiaMatch = eia96Regex.matchEntire(code)

        if (len == 3 && eiaMatch != null) {
            val digits = eiaMatch.groupValues[1]
            val letter = eiaMatch.groupValues[2].first()

            val baseInt = eia96Codes[digits]
            val factor = eia96Letters[letter]

            if (baseInt != null && factor != null) {
                val resistance = baseInt * factor
                return@remember Pair(friendlyResistance(resistance), "±1% (EIA-96 High-Precision)")
            }
        }

        // If something was entered but didn't match fully yet
        Pair("Analyzing...", null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Alphanumeric Input Board
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputCode,
                    onValueChange = {
                        if (it.length <= 5) {
                            inputCode = it
                        }
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "103, 472, 01A...",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = (22 * textSizeMultiplier).sp
                            )
                        )
                    },
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.5.sp,
                        fontSize = (24 * textSizeMultiplier).sp
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                )

                if (inputCode.isNotEmpty()) {
                    IconButton(
                        onClick = { inputCode = "" },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search input",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Text(
            text = Loc.get("smd_desc", currentLanguage),
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = (12 * textSizeMultiplier).sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Calculated Value Display Card with subtle dynamic elevation
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = Loc.get("smd_header", currentLanguage),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = (11 * textSizeMultiplier).sp
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = outputState.first,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = (32 * textSizeMultiplier).sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                if (outputState.second != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = outputState.second!!,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = (14 * textSizeMultiplier).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Accordion collapsing card detailing multipliers & letters
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { referenceExpanded = !referenceExpanded }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Details info icon",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = Loc.get("smd_ref_title", currentLanguage),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = (16 * textSizeMultiplier).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Toggle Reference visibility",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.animateContentSize()
                    )
                }

                AnimatedVisibility(
                    visible = referenceExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = Loc.get("smd_ref_desc", currentLanguage),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = (12 * textSizeMultiplier).sp,
                                lineHeight = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Reference Tables Grid
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Column 1: Codes
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = Loc.get("smd_letter_mult", currentLanguage),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Divider()

                                val list1 = listOf(
                                    "Z / z" to "0.001",
                                    "Y / R" to "0.01",
                                    "X / S" to "0.1",
                                    "A" to "1",
                                    "B / H" to "10"
                                )
                                list1.forEach {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = it.first, style = MaterialTheme.typography.bodySmall)
                                        Text(text = it.second, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(32.dp))

                            // Column 2: Letters
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = Loc.get("smd_letter_mult", currentLanguage),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Divider()

                                val list2 = listOf(
                                    "C" to "100",
                                    "D" to "1,000",
                                    "E" to "10,000",
                                    "F" to "100,000"
                                )
                                list2.forEach {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = it.first, style = MaterialTheme.typography.bodySmall)
                                        Text(text = it.second, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

// Convert absolute ohms to friendly readout values (e.g. 10000 -> 10 kΩ)
fun friendlyResistance(ohms: Double): String {
    return when {
        ohms >= 1_000_000 -> {
            val formatted = String.format(Locale.US, "%.2f", ohms / 1_000_000.0)
            "${formatted.trimEnd('0').trimEnd('.')} MΩ"
        }
        ohms >= 1_000 -> {
            val formatted = String.format(Locale.US, "%.2f", ohms / 1_000.0)
            "${formatted.trimEnd('0').trimEnd('.')} kΩ"
        }
        else -> {
            val formatted = String.format(Locale.US, "%.2f", ohms)
            "${formatted.trimEnd('0').trimEnd('.')} Ω"
        }
    }
}
