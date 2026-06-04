package com.example

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.sqrt

enum class OhmKey { V, I, R, P }

@Composable
fun OhmsLawCalcScreen(
    textSizeMultiplier: Float = 1.0f,
    currentLanguage: String = "en"
) {
    val scrollState = rememberScrollState()

    // Prefix unit states for parameters
    var vUnit by remember { mutableStateOf("V") }       // "V", "mV", "kV"
    var iUnit by remember { mutableStateOf("A") }       // "A", "mA", "μA"
    var rUnit by remember { mutableStateOf("Ω") }       // "Ω", "kΩ", "MΩ"
    var pUnit by remember { mutableStateOf("W") }       // "W", "mW", "kW"

    // String input values entered by the user
    var voltageInput by remember { mutableStateOf("") }
    var currentInput by remember { mutableStateOf("") }
    var resistanceInput by remember { mutableStateOf("") }
    var powerInput by remember { mutableStateOf("") }

    // Multipliers for prefixes to translate to base units
    fun getMultV(unit: String): Double = when (unit) {
        "mV" -> 0.001
        "kV" -> 1000.0
        else -> 1.0
    }

    fun getMultI(unit: String): Double = when (unit) {
        "mA" -> 0.001
        "μA" -> 0.000001
        else -> 1.0
    }

    fun getMultR(unit: String): Double = when (unit) {
        "kΩ" -> 1000.0
        "MΩ" -> 1000000.0
        else -> 1.0
    }

    fun getMultP(unit: String): Double = when (unit) {
        "mW" -> 0.001
        "kW" -> 1000.0
        else -> 1.0
    }

    // Keeps track of the two parameters entered by the user in chronological sequence
    val userEntered = remember { mutableStateListOf<OhmKey>() }

    fun parse(str: String): Double? {
        if (str.isEmpty()) return null
        return str.toDoubleOrNull()
    }

    fun format(num: Double): String {
        if (num.isNaN() || num.isInfinite()) return ""
        if (num == 0.0) return "0"
        val formatted = String.format(Locale.US, "%.5f", num)
        val cleaned = formatted.trimEnd('0').trimEnd('.')
        return if (cleaned == "-0") "0" else cleaned
    }

    // Active real-time calculating engine with units support
    LaunchedEffect(
        voltageInput, currentInput, resistanceInput, powerInput,
        vUnit, iUnit, rUnit, pUnit, userEntered.size
    ) {
        if (userEntered.size < 2) return@LaunchedEffect

        val key1 = userEntered[0]
        val key2 = userEntered[1]

        val rawV = parse(voltageInput)
        val rawI = parse(currentInput)
        val rawR = parse(resistanceInput)
        val rawP = parse(powerInput)

        // Convert user parameters to base SI units
        val baseV = if (rawV != null) rawV * getMultV(vUnit) else null
        val baseI = if (rawI != null) rawI * getMultI(iUnit) else null
        val baseR = if (rawR != null) rawR * getMultR(rUnit) else null
        val baseP = if (rawP != null) rawP * getMultP(pUnit) else null

        var calculatedBaseV = baseV
        var calculatedBaseI = baseI
        var calculatedBaseR = baseR
        var calculatedBaseP = baseP

        when {
            // 1. Voltage & Current entered
            (key1 == OhmKey.V && key2 == OhmKey.I) || (key1 == OhmKey.I && key2 == OhmKey.V) -> {
                if (baseV != null && baseI != null && baseI != 0.0) {
                    calculatedBaseR = baseV / baseI
                    calculatedBaseP = baseV * baseI
                }
            }
            // 2. Voltage & Resistance entered
            (key1 == OhmKey.V && key2 == OhmKey.R) || (key1 == OhmKey.R && key2 == OhmKey.V) -> {
                if (baseV != null && baseR != null && baseR != 0.0) {
                    calculatedBaseI = baseV / baseR
                    calculatedBaseP = (baseV * baseV) / baseR
                }
            }
            // 3. Voltage & Power entered
            (key1 == OhmKey.V && key2 == OhmKey.P) || (key1 == OhmKey.P && key2 == OhmKey.V) -> {
                if (baseV != null && baseP != null && baseV != 0.0) {
                    calculatedBaseI = baseP / baseV
                    calculatedBaseR = (baseV * baseV) / baseP
                }
            }
            // 4. Current & Resistance entered
            (key1 == OhmKey.I && key2 == OhmKey.R) || (key1 == OhmKey.R && key2 == OhmKey.I) -> {
                if (baseI != null && baseR != null) {
                    calculatedBaseV = baseI * baseR
                    calculatedBaseP = baseI * baseI * baseR
                }
            }
            // 5. Current & Power entered
            (key1 == OhmKey.I && key2 == OhmKey.P) || (key1 == OhmKey.P && key2 == OhmKey.I) -> {
                if (baseI != null && baseP != null && baseI != 0.0) {
                    calculatedBaseV = baseP / baseI
                    calculatedBaseR = baseP / (baseI * baseI)
                }
            }
            // 6. Resistance & Power entered
            (key1 == OhmKey.R && key2 == OhmKey.P) || (key1 == OhmKey.P && key2 == OhmKey.R) -> {
                if (baseR != null && baseP != null && baseR >= 0.0 && baseP >= 0.0) {
                    calculatedBaseV = sqrt(baseP * baseR)
                    calculatedBaseI = sqrt(baseP / baseR)
                }
            }
        }

        // Output calculation parameters turned back to user selected prefix units
        if (OhmKey.V !in userEntered && calculatedBaseV != null) {
            voltageInput = format(calculatedBaseV / getMultV(vUnit))
        }
        if (OhmKey.I !in userEntered && calculatedBaseI != null) {
            currentInput = format(calculatedBaseI / getMultI(iUnit))
        }
        if (OhmKey.R !in userEntered && calculatedBaseR != null) {
            resistanceInput = format(calculatedBaseR / getMultR(rUnit))
        }
        if (OhmKey.P !in userEntered && calculatedBaseP != null) {
            powerInput = format(calculatedBaseP / getMultP(pUnit))
        }
    }

    // Handle user typing and correct oldest selection eviction
    fun handleInputChanged(key: OhmKey, newVal: String) {
        when (key) {
            OhmKey.V -> voltageInput = newVal
            OhmKey.I -> currentInput = newVal
            OhmKey.R -> resistanceInput = newVal
            OhmKey.P -> powerInput = newVal
        }

        if (newVal.isNotEmpty()) {
            if (key !in userEntered) {
                if (userEntered.size >= 2) {
                    // Evict oldest parameter entered
                    val oldest = userEntered.removeAt(0)
                    when (oldest) {
                        OhmKey.V -> voltageInput = ""
                        OhmKey.I -> currentInput = ""
                        OhmKey.R -> resistanceInput = ""
                        OhmKey.P -> powerInput = ""
                    }
                }
                userEntered.add(key)
            }
        } else {
            userEntered.remove(key)
            // If parameters drop below 2, wipecalculated outputs
            if (userEntered.size < 2) {
                if (OhmKey.V !in userEntered) voltageInput = ""
                if (OhmKey.I !in userEntered) currentInput = ""
                if (OhmKey.R !in userEntered) resistanceInput = ""
                if (OhmKey.P !in userEntered) powerInput = ""
            }
        }
    }

    // Dynamic equation metadata for explanation panel
    val equationsBreakdown = remember(userEntered.toList(), voltageInput, currentInput, resistanceInput, powerInput, vUnit, iUnit, rUnit, pUnit) {
        if (userEntered.size < 2) return@remember null

        val key1 = userEntered[0]
        val key2 = userEntered[1]

        val rawV = parse(voltageInput)
        val rawI = parse(currentInput)
        val rawR = parse(resistanceInput)
        val rawP = parse(powerInput)

        val vSI = if (rawV != null) rawV * getMultV(vUnit) else 0.0
        val iSI = if (rawI != null) rawI * getMultI(iUnit) else 0.0
        val rSI = if (rawR != null) rawR * getMultR(rUnit) else 0.0
        val pSI = if (rawP != null) rawP * getMultP(pUnit) else 0.0

        val vClean = String.format(Locale.US, "%.4g", vSI)
        val iClean = String.format(Locale.US, "%.4g", iSI)
        val rClean = String.format(Locale.US, "%.4g", rSI)
        val pClean = String.format(Locale.US, "%.4g", pSI)

        when {
            (key1 == OhmKey.V && key2 == OhmKey.I) || (key1 == OhmKey.I && key2 == OhmKey.V) -> {
                listOf(
                    "Resistance: R = V / I" to "R = $vClean V / $iClean A = $rClean Ω",
                    "Power: P = V × I" to "P = $vClean V × $iClean A = $pClean W"
                )
            }
            (key1 == OhmKey.V && key2 == OhmKey.R) || (key1 == OhmKey.R && key2 == OhmKey.V) -> {
                listOf(
                    "Current: I = V / R" to "I = $vClean V / $rClean Ω = $iClean A",
                    "Power: P = V² / R" to "P = ($vClean)² / $rClean = $pClean W"
                )
            }
            (key1 == OhmKey.V && key2 == OhmKey.P) || (key1 == OhmKey.P && key2 == OhmKey.V) -> {
                listOf(
                    "Current: I = P / V" to "I = $pClean W / $vClean V = $iClean A",
                    "Resistance: R = V² / P" to "R = ($vClean)² / $pClean = $rClean Ω"
                )
            }
            (key1 == OhmKey.I && key2 == OhmKey.R) || (key1 == OhmKey.R && key2 == OhmKey.I) -> {
                listOf(
                    "Voltage: V = I × R" to "V = $iClean A × $rClean Ω = $vClean V",
                    "Power: P = I² × R" to "P = ($iClean)² × $rClean = $pClean W"
                )
            }
            (key1 == OhmKey.I && key2 == OhmKey.P) || (key1 == OhmKey.P && key2 == OhmKey.I) -> {
                listOf(
                    "Voltage: V = P / I" to "V = $pClean W / $iClean A = $vClean V",
                    "Resistance: R = P / I²" to "R = $pClean / ($iClean)² = $rClean Ω"
                )
            }
            (key1 == OhmKey.R && key2 == OhmKey.P) || (key1 == OhmKey.P && key2 == OhmKey.R) -> {
                listOf(
                    "Voltage: V = √(P × R)" to "V = √($pClean W × $rClean Ω) = $vClean V",
                    "Current: I = √(P / R)" to "I = √($pClean W / $rClean Ω) = $iClean A"
                )
            }
            else -> null
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
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = Loc.get("ohms_title", currentLanguage),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = (22 * textSizeMultiplier).sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = Loc.get("ohms_subtitle", currentLanguage),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = (13 * textSizeMultiplier).sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Ohm Parameter Cards Block
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Voltage Card (V)
            OhmInteractiveCard(
                key = OhmKey.V,
                label = Loc.get("ohms_voltage", currentLanguage),
                value = voltageInput,
                units = listOf("V", "mV", "kV"),
                selectedUnit = vUnit,
                onUnitChanged = { vUnit = it },
                userEnteredKeys = userEntered,
                onValueChanged = { handleInputChanged(OhmKey.V, it) },
                textSizeMultiplier = textSizeMultiplier
            )

            // Current Card (I)
            OhmInteractiveCard(
                key = OhmKey.I,
                label = Loc.get("ohms_current", currentLanguage),
                value = currentInput,
                units = listOf("A", "mA", "μA"),
                selectedUnit = iUnit,
                onUnitChanged = { iUnit = it },
                userEnteredKeys = userEntered,
                onValueChanged = { handleInputChanged(OhmKey.I, it) },
                textSizeMultiplier = textSizeMultiplier
            )

            // Resistance Card (R)
            OhmInteractiveCard(
                key = OhmKey.R,
                label = Loc.get("ohms_resistance", currentLanguage),
                value = resistanceInput,
                units = listOf("Ω", "kΩ", "MΩ"),
                selectedUnit = rUnit,
                onUnitChanged = { rUnit = it },
                userEnteredKeys = userEntered,
                onValueChanged = { handleInputChanged(OhmKey.R, it) },
                textSizeMultiplier = textSizeMultiplier
            )

            // Power Card (P)
            OhmInteractiveCard(
                key = OhmKey.P,
                label = Loc.get("ohms_power", currentLanguage),
                value = powerInput,
                units = listOf("W", "mW", "kW"),
                selectedUnit = pUnit,
                onUnitChanged = { pUnit = it },
                userEnteredKeys = userEntered,
                onValueChanged = { handleInputChanged(OhmKey.P, it) },
                textSizeMultiplier = textSizeMultiplier
            )
        }

        // Live educational dynamic formulas steps panel
        AnimatedVisibility(
            visible = equationsBreakdown != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            equationsBreakdown?.let { steps ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "formulas info icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = Loc.get("ohms_formula_used", currentLanguage),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (14 * textSizeMultiplier).sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = Loc.get("ohms_step_desc", currentLanguage),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

                        steps.forEach { (formTitle, calcStr) ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = formTitle,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = calcStr,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = (15 * textSizeMultiplier).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Reset/Clear Action Button
        Button(
            onClick = {
                userEntered.clear()
                voltageInput = ""
                currentInput = ""
                resistanceInput = ""
                powerInput = ""
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "reset button iconic",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Loc.get("ohms_clear", currentLanguage).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = (15 * textSizeMultiplier).sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun OhmInteractiveCard(
    key: OhmKey,
    label: String,
    value: String,
    units: List<String>,
    selectedUnit: String,
    onUnitChanged: (String) -> Unit,
    userEnteredKeys: List<OhmKey>,
    onValueChanged: (String) -> Unit,
    textSizeMultiplier: Float
) {
    val isUserEntered = userEnteredKeys.contains(key)
    val isCalculatedValue = userEnteredKeys.size >= 2 && !isUserEntered

    // Define background and boundary accent coloring reflecting active computation tags
    val cardBg = if (isCalculatedValue) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    val contentColor = if (isCalculatedValue) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val borderStroke = if (isUserEntered) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = borderStroke,
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Label and calculations modes badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = (11 * textSizeMultiplier).sp
                    ),
                    color = if (isCalculatedValue) {
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                if (isCalculatedValue) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "CALCULATED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                fontSize = 8.sp
                            ),
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (isUserEntered) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "INPUT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                fontSize = 8.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Text entry field alongside Prefix selecting dropdown chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty() && !isCalculatedValue) {
                        Text(
                            text = "0.00",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = (18 * textSizeMultiplier).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }

                    androidx.compose.foundation.text.BasicTextField(
                        value = value,
                        onValueChange = { onValueChanged(it) },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isCalculatedValue) FontWeight.ExtraBold else FontWeight.Bold,
                            color = contentColor,
                            fontSize = (18 * textSizeMultiplier).sp
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        readOnly = isCalculatedValue,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Inline Prefix Selector Row
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    units.forEach { u ->
                        val active = selectedUnit == u
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { onUnitChanged(u) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = u,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
