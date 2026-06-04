package com.example

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource


@Composable
fun ResistorCalcScreen(
    textSizeMultiplier: Float = 1.0f,
    currentLanguage: String = "en"
) {
    var bandCount by remember { mutableStateOf(4) } // 3, 4, 5, or 6

    // Define colors
    val digitColors = listOf(
        ResistorColor.BLACK, ResistorColor.BROWN, ResistorColor.RED, ResistorColor.ORANGE, ResistorColor.YELLOW,
        ResistorColor.GREEN, ResistorColor.BLUE, ResistorColor.VIOLET, ResistorColor.GRAY, ResistorColor.WHITE
    )

    // For first Digit (Band 1), Black cannot be used as leading digit
    val digitColorsNoBlack = listOf(
        ResistorColor.BROWN, ResistorColor.RED, ResistorColor.ORANGE, ResistorColor.YELLOW,
        ResistorColor.GREEN, ResistorColor.BLUE, ResistorColor.VIOLET, ResistorColor.GRAY, ResistorColor.WHITE
    )

    val multiplierColors = listOf(
        ResistorColor.BLACK, ResistorColor.BROWN, ResistorColor.RED, ResistorColor.ORANGE, ResistorColor.YELLOW,
        ResistorColor.GREEN, ResistorColor.BLUE, ResistorColor.VIOLET, ResistorColor.GOLD, ResistorColor.SILVER
    )

    // Strictly standard tolerance colors that exist on real physical components
    val toleranceColors = listOf(
        ResistorColor.BROWN, ResistorColor.RED, ResistorColor.GREEN, ResistorColor.BLUE,
        ResistorColor.VIOLET, ResistorColor.GOLD, ResistorColor.SILVER
    )

    // Strictly standard temp coeff colors that exist on real physical components
    val tempColors = listOf(
        ResistorColor.BROWN, ResistorColor.RED, ResistorColor.ORANGE, ResistorColor.YELLOW,
        ResistorColor.BLUE, ResistorColor.VIOLET
    )

    // Current selections (nothing selected by default)
    var band1 by remember { mutableStateOf<ResistorColor?>(null) } // 1st digit
    var band2 by remember { mutableStateOf<ResistorColor?>(null) } // 2nd digit
    var band3 by remember { mutableStateOf<ResistorColor?>(null) }  // 3rd digit (only for 5 or 6 bands)
    var multiplier by remember { mutableStateOf<ResistorColor?>(null) }
    var tolerance by remember { mutableStateOf<ResistorColor?>(null) }
    var tempCoeff by remember { mutableStateOf<ResistorColor?>(null) } // ppm (only for 6 bands)

    var showResultCard by remember { mutableStateOf(false) }

    val isAllSelected = remember(bandCount, band1, band2, band3, multiplier, tolerance, tempCoeff) {
        when (bandCount) {
            3 -> band1 != null && band2 != null && multiplier != null
            4 -> band1 != null && band2 != null && multiplier != null && tolerance != null
            5 -> band1 != null && band2 != null && band3 != null && multiplier != null && tolerance != null
            6 -> band1 != null && band2 != null && band3 != null && multiplier != null && tolerance != null && tempCoeff != null
            else -> false
        }
    }

    LaunchedEffect(bandCount) {
        band1 = null
        band2 = null
        band3 = null
        multiplier = null
        tolerance = null
        tempCoeff = null
        showResultCard = false
    }

    // Hide result card if selection becomes incomplete
    LaunchedEffect(isAllSelected) {
        if (!isAllSelected) {
            showResultCard = false
        }
    }

    // Compute value
    val calculatedDetails = remember(bandCount, band1, band2, band3, multiplier, tolerance, tempCoeff) {
        val digs = when (bandCount) {
            3, 4 -> {
                val b1 = band1?.value ?: 0
                val b2 = band2?.value ?: 0
                b1 * 10 + b2
            }
            5, 6 -> {
                val b1 = band1?.value ?: 0
                val b2 = band2?.value ?: 0
                val b3 = band3?.value ?: 0
                b1 * 100 + b2 * 10 + b3
            }
            else -> 0
        }
        val multValue = multiplier?.multiplier ?: 1.0
        val resistance = digs * multValue

        val tolText = when (bandCount) {
            3 -> "±20%"
            else -> "±${tolerance?.tolerance ?: 5.0}%"
        }

        val tempText = if (bandCount == 6) "${tempCoeff?.tempCoeff ?: 100} ppm/K" else null

        Triple(resistance, tolText, tempText)
    }

    val finalResistanceValue = calculatedDetails.first
    val currentToleranceValue = calculatedDetails.second
    val currentTempCo = calculatedDetails.third

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Clicking anywhere other than the result card dismisses it
                showResultCard = false
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .padding(bottom = 180.dp) // space for output card/button
        ) {
            // Graphic Canvas Area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .shadow(4.dp, shape = RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceContainerHighest,
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Resistor Drawing
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(100.dp)
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val midY = canvasHeight / 2

                        // Wire Leads (metallic lead line)
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF71717A), Color(0xFFE4E4E7), Color(0xFF52525B))
                            ),
                            topLeft = Offset(0f, midY - 4.dp.toPx()),
                            size = Size(canvasWidth, 8.dp.toPx()),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )

                        // Resistor Body (Beige Cylinder)
                        val bodyLeft = canvasWidth * 0.25f
                        val bodyWidth = canvasWidth * 0.5f
                        val bodyHeight = 56.dp.toPx()
                        val bodyTop = midY - (bodyHeight / 2)

                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFDCCFB3), Color(0xFFF5EBD6), Color(0xFFC4B79C), Color(0xFF8C7F66)
                                )
                            ),
                            topLeft = Offset(bodyLeft, bodyTop),
                            size = Size(bodyWidth, bodyHeight),
                            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                        )

                        // Outline contour
                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.15f),
                            topLeft = Offset(bodyLeft, bodyTop),
                            size = Size(bodyWidth, bodyHeight),
                            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                        )

                        // Calculate and draw bands
                        val totalBands = bandCount
                        val bandWidth = 14.dp.toPx()
                        val usableWidth = bodyWidth * 0.8f
                        val spacing = usableWidth / (totalBands - 1).coerceAtLeast(1)

                        for (i in 0 until totalBands) {
                            val activeColor = when (i) {
                                0 -> band1?.displayColor
                                1 -> band2?.displayColor
                                2 -> {
                                    if (totalBands >= 5) band3?.displayColor else multiplier?.displayColor
                                }
                                3 -> {
                                    if (totalBands == 3) {
                                        // No tolerance band visibly drawn, or transparent outline
                                        Color.Transparent
                                    } else if (totalBands == 4) {
                                        tolerance?.displayColor
                                    } else {
                                        multiplier?.displayColor
                                    }
                                }
                                4 -> {
                                    // tolerance band for 5/6 bands
                                    tolerance?.displayColor
                                }
                                5 -> {
                                    // temp band for 6 bands
                                    tempCoeff?.displayColor
                                }
                                else -> Color.Transparent
                            }

                            val bandX = bodyLeft + (bodyWidth * 0.1f) + (i * spacing) - (bandWidth / 2)
                            if (activeColor != null && activeColor != Color.Transparent) {
                                // Draw Color Stripe
                                drawRect(
                                    color = activeColor,
                                    topLeft = Offset(bandX, bodyTop),
                                    size = Size(bandWidth, bodyHeight)
                                )

                                // Apply 3D Lighting Overlay for stripes
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.25f),
                                            Color.White.copy(alpha = 0.2f),
                                            Color.Black.copy(alpha = 0.35f)
                                        )
                                    ),
                                    topLeft = Offset(bandX, bodyTop),
                                    size = Size(bandWidth, bodyHeight)
                                )
                            } else if (activeColor == null && (totalBands > 3 || i < 3)) {
                                // Draw a very subtle faint strip outline indicating an unselected band
                                drawRect(
                                    color = Color.Black.copy(alpha = 0.08f),
                                    topLeft = Offset(bandX, bodyTop),
                                    size = Size(bandWidth, bodyHeight)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = Loc.getFormatted("res_band_count", currentLanguage, bandCount),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = (13 * textSizeMultiplier).sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Band selection options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer, shape = CircleShape)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(3, 4, 5, 6).forEach { num ->
                    val selected = bandCount == num
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                            )
                            .clickable { bandCount = num },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$num",
                            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = (14 * textSizeMultiplier).sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Color selection rows based on active bands
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Band 1 selection (leading digit - Black is excluded since leading digit cannot be black)
                ColorSelectorRow(
                    label = Loc.get("res_band1", currentLanguage),
                    colors = digitColorsNoBlack,
                    selectedColor = band1,
                    onColorSelected = { band1 = it },
                    textSizeMultiplier = textSizeMultiplier
                )

                // Band 2 selection
                ColorSelectorRow(
                    label = Loc.get("res_band2", currentLanguage),
                    colors = digitColors,
                    selectedColor = band2,
                    onColorSelected = { band2 = it },
                    textSizeMultiplier = textSizeMultiplier
                )

                // Band 3 selection (Digit 3) - only displayed for 5 & 6 bands
                if (bandCount >= 5) {
                    ColorSelectorRow(
                        label = Loc.get("res_band3", currentLanguage),
                        colors = digitColors,
                        selectedColor = band3,
                        onColorSelected = { band3 = it },
                        textSizeMultiplier = textSizeMultiplier
                    )
                }

                // Multiplier Selector
                ColorSelectorRow(
                    label = Loc.get("res_multiplier", currentLanguage),
                    colors = multiplierColors,
                    selectedColor = multiplier,
                    onColorSelected = { multiplier = it },
                    textSizeMultiplier = textSizeMultiplier,
                    showText = true,
                    isMultiplier = true
                )

                // Tolerance Selector - only for 4, 5, 6 bands
                if (bandCount >= 4) {
                    ColorSelectorRow(
                        label = Loc.get("res_tolerance", currentLanguage),
                        colors = toleranceColors,
                        selectedColor = tolerance,
                        onColorSelected = { tolerance = it },
                        textSizeMultiplier = textSizeMultiplier,
                        showText = true,
                        isTolerance = true
                    )
                }

                // Temperature Coefficient - only for 6 bands
                if (bandCount == 6) {
                    ColorSelectorRow(
                        label = Loc.get("res_temp", currentLanguage),
                        colors = tempColors,
                        selectedColor = tempCoeff,
                        onColorSelected = { tempCoeff = it },
                        textSizeMultiplier = textSizeMultiplier,
                        showText = true,
                        isTemp = true
                    )
                }
            }
        }

        // Show the bold, dynamic Calculate Button when fully selected but result isn't calculating
        if (isAllSelected && !showResultCard) {
            Button(
                onClick = { showResultCard = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 8.dp)
                    .height(56.dp)
                    .shadow(8.dp, shape = RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = Loc.get("res_calculate", currentLanguage).uppercase(Locale.US),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = (16 * textSizeMultiplier).sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // Animated Smooth Results Slide-In Dashboard Card (consuming clicks so we don't hide it clicking inside)
        AnimatedVisibility(
            visible = showResultCard && isAllSelected,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 8.dp)
                    .shadow(8.dp, shape = RoundedCornerShape(24.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Consumes clicks inside the card so dialog isn't dismissed
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = Loc.get("res_calc_val", currentLanguage),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            fontSize = (11 * textSizeMultiplier).sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = formatValue(finalResistanceValue),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = (34 * textSizeMultiplier).sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatUnit(finalResistanceValue),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = (18 * textSizeMultiplier).sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Loc.getFormatted("res_tolerance_val", currentLanguage, currentToleranceValue),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = (14 * textSizeMultiplier).sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )

                        if (currentTempCo != null) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = " • $currentTempCo",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = (14 * textSizeMultiplier).sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColorSelectorRow(
    label: String,
    colors: List<ResistorColor>,
    selectedColor: ResistorColor?,
    onColorSelected: (ResistorColor) -> Unit,
    textSizeMultiplier: Float,
    showText: Boolean = false,
    isMultiplier: Boolean = false,
    isTolerance: Boolean = false,
    isTemp: Boolean = false
) {
    Column {
        Text(
            text = label.uppercase(Locale.US),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = (11 * textSizeMultiplier).sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(colors) { config ->
                val isSelected = config == selectedColor
                val isWhiteColor = config.displayColor == Color.White

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(config.displayColor)
                        .border(
                            width = if (isSelected) 3.dp else if (isWhiteColor) 1.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else if (isWhiteColor) MaterialTheme.colorScheme.outlineVariant else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(config) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected color",
                            tint = if (isWhiteColor) Color.Black else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Bottom info label over color
                    if (showText) {
                        val badgeText = when {
                            isMultiplier -> when (config) {
                                ResistorColor.GOLD -> "10⁻¹"
                                ResistorColor.SILVER -> "10⁻²"
                                else -> config.multiplier?.let { formatExponent(it) } ?: ""
                            }
                            isTolerance -> "${config.tolerance}%"
                            isTemp -> "${config.tempCoeff}"
                            else -> ""
                        }

                        if (badgeText.isNotEmpty()) {
                            Text(
                                text = badgeText,
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isWhiteColor || config == ResistorColor.YELLOW) Color.Black else Color.White
                                ),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 2.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatExponent(value: Double): String {
    return when (value) {
        1.0 -> "1"
        10.0 -> "10"
        100.0 -> "10²"
        1000.0 -> "10³"
        10000.0 -> "10⁴"
        100000.0 -> "10⁵"
        1000000.0 -> "10⁶"
        10000000.0 -> "10⁷"
        100000000.0 -> "10⁸"
        1000000000.0 -> "10⁹"
        else -> value.toInt().toString()
    }
}

fun formatValue(resValue: Double): String {
    return when {
        resValue >= 1_000_000_000 -> String.format(Locale.US, "%.2f", resValue / 1_000_000_000.0).trimEnd('0').trimEnd('.')
        resValue >= 1_000_000 -> String.format(Locale.US, "%.2f", resValue / 1_000_000.0).trimEnd('0').trimEnd('.')
        resValue >= 1_000 -> String.format(Locale.US, "%.2f", resValue / 1_000.0).trimEnd('0').trimEnd('.')
        else -> String.format(Locale.US, "%.2f", resValue).trimEnd('0').trimEnd('.')
    }
}

fun formatUnit(resValue: Double): String {
    return when {
        resValue >= 1_000_000_000 -> "GΩ"
        resValue >= 1_000_000 -> "MΩ"
        resValue >= 1_000 -> "kΩ"
        else -> "Ω"
    }
}

// Resistor Color definition structure
enum class ResistorColor(
    val colorName: String,
    val value: Int?,
    val multiplier: Double?,
    val tolerance: Double?,
    val tempCoeff: Int?,
    val displayColor: Color
) {
    BLACK("Black", 0, 1.0, null, null, Color(0xFF2d2d30)),
    BROWN("Brown", 1, 10.0, 1.0, 100, Color(0xFF8B5A2B)),
    RED("Red", 2, 100.0, 2.0, 50, Color(0xFFEF4444)),
    ORANGE("Orange", 3, 1000.0, null, 15, Color(0xFFF97316)),
    YELLOW("Yellow", 4, 10000.0, null, 25, Color(0xFFEAB308)),
    GREEN("Green", 5, 100000.0, 0.5, 20, Color(0xFF22C55E)),
    BLUE("Blue", 6, 1000000.0, 0.25, 10, Color(0xFF2563EB)),
    VIOLET("Violet", 7, 10000000.0, 0.1, 5, Color(0xFF9333EA)),
    GRAY("Gray", 8, 100000000.0, 0.05, 1, Color(0xFF9CA3AF)),
    WHITE("White", 9, 1000000000.0, null, null, Color(0xFFFFFFFF)),
    GOLD("Gold", null, 0.1, 5.0, null, Color(0xFFD4AF37)),
    SILVER("Silver", null, 0.01, 10.0, null, Color(0xFFC0C0C0))
}
