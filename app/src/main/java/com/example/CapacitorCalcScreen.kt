package com.example

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun CapacitorCalcScreen(
    textSizeMultiplier: Float = 1.0f,
    currentLanguage: String = "en"
) {
    val scrollState = rememberScrollState()

    var inputCode by remember { mutableStateOf("") }
    var toleranceGuideExpanded by remember { mutableStateOf(false) }

    val toleranceLetterMap = remember {
        mapOf(
            'B' to "±0.10%", 'C' to "±0.25%", 'D' to "±0.5%", 'F' to "±1%",
            'G' to "±2%", 'H' to "±3%", 'J' to "±5%", 'K' to "±10%",
            'M' to "±20%", 'Z' to "+80%, -20%"
        )
    }

    // Live capacitor code decoder
    val results = remember(inputCode) {
        val trimmed = inputCode.trim().uppercase(Locale.US)
        if (trimmed.isEmpty()) return@remember null

        val containsR = trimmed.contains('R')

        if (containsR) {
            val rRegex = Regex("^(\\d+)?R(\\d+)?([A-Z])?$")
            val rMatch = rRegex.matchEntire(trimmed)
            if (rMatch != null) {
                val left = rMatch.groupValues[1]
                val right = rMatch.groupValues[2]
                val toleranceChar = rMatch.groupValues[3].firstOrNull()

                val leftVal = if (left.isEmpty()) "0" else left
                val rightVal = if (right.isEmpty()) "0" else right
                val pf = "$leftVal.$rightVal".toDoubleOrNull() ?: 0.0
                val nf = pf / 1000.0
                val uf = pf / 1000000.0
                val toleranceText = toleranceChar?.let { toleranceLetterMap[it] }
                Quadruple(pf, nf, uf, toleranceText)
            } else {
                null
            }
        } else {
            // Match regex: 1-2 digits, optional multiplier digit (0-9), optional tolerance letter
            val capRegex = Regex("^(\\d{1,2})(\\d{1})?([A-Z])?$")
            val match = capRegex.matchEntire(trimmed)

            if (match != null) {
                val digits = match.groupValues[1]
                val multiplierChar = match.groupValues[2]
                val toleranceChar = match.groupValues[3].firstOrNull()

                val baseValue = digits.toDoubleOrNull() ?: 0.0
                val multiplier = if (multiplierChar.isNotEmpty()) {
                    multiplierChar.toIntOrNull() ?: 0
                } else {
                    0
                }

                val multFactor = when (multiplier) {
                    8 -> 0.01
                    9 -> 0.1
                    else -> Math.pow(10.0, multiplier.toDouble())
                }

                val pf = baseValue * multFactor
                val nf = pf / 1000.0
                val uf = pf / 1000000.0

                val toleranceText = if (toleranceChar != null) {
                    toleranceLetterMap[toleranceChar]
                } else {
                    null
                }

                Quadruple(pf, nf, uf, toleranceText)
            } else {
                null
            }
        }
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

        // Large Code Input Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = null
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
                             text = Loc.get("cap_placeholder", currentLanguage),
                             style = MaterialTheme.typography.titleLarge.copy(
                                 fontWeight = FontWeight.Bold,
                                 color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                 fontSize = (20 * textSizeMultiplier).sp
                             )
                        )
                    },
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center,
                        fontSize = (26 * textSizeMultiplier).sp
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
                            contentDescription = "Clear input text",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quick Reference Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Loc.get("cap_common", currentLanguage),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )

            listOf("104", "473", "222J", "101K").forEach { code ->
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable { inputCode = code }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Display results or empty guide state
        if (results != null) {
            // Result Presentation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Loc.get("cap_header", currentLanguage),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = (16 * textSizeMultiplier).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (results.fourth != null) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = Loc.getFormatted("res_tolerance_val", currentLanguage, results.fourth!!),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Direct conversion boxes
                    CapUnitRow(unit = Loc.get("cap_picofarad", currentLanguage), value = formatWithCommas(results.first), textSizeMultiplier = textSizeMultiplier)
                    CapUnitRow(unit = Loc.get("cap_nanofarad", currentLanguage), value = friendlyDouble(results.second), textSizeMultiplier = textSizeMultiplier)

                    // Hero Microfarad selection
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Loc.get("cap_microfarad", currentLanguage),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = (15 * textSizeMultiplier).sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = friendlyDouble(results.third),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = (26 * textSizeMultiplier).sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        } else {
            // Placeholder Guide State
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = Loc.get("cap_identify", currentLanguage),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = (16 * textSizeMultiplier).sp
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = Loc.get("cap_desc", currentLanguage),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = (14 * textSizeMultiplier).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(text = "Code: 104", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                Text(text = "0.1 µF (100nF)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(text = "Code: 472", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                Text(text = "0.0047 µF (4.7nF)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Tolerance Guide Accordion item
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
                        .clickable { toleranceGuideExpanded = !toleranceGuideExpanded }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Tolerance Letter Guide",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = (16 * textSizeMultiplier).sp
                            )
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(
                    visible = toleranceGuideExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Col 1
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val list1 = listOf(
                                    "B" to "±0.10%",
                                    "C" to "±0.25%",
                                    "D" to "±0.5%",
                                    "F" to "±1%",
                                    "G" to "±2%"
                                )
                                list1.forEach {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = it.first, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text(text = it.second, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(32.dp))

                            // Col 2
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val list2 = listOf(
                                    "J" to "±5%",
                                    "K" to "±10%",
                                    "M" to "±20%",
                                    "Z" to "+80%, -20%"
                                )
                                list2.forEach {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = it.first, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text(text = it.second, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun CapUnitRow(
    unit: String,
    value: String,
    textSizeMultiplier: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = unit,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (14 * textSizeMultiplier).sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = (16 * textSizeMultiplier).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun friendlyDouble(value: Double): String {
    val formatted = String.format(Locale.US, "%.6f", value)
    return formatted.trimEnd('0').trimEnd('.')
}

fun formatWithCommas(value: Double): String {
    return String.format(Locale.US, "%,.0f", value)
}

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
