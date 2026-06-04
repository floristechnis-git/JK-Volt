package com.example

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

// Database specifications of wires per NEC Chapter 9 Table 8 & Table 310.15
data class ConductorSpec(
    val awg: String,
    val cuAmpacity60: Double,
    val cuAmpacity75: Double,
    val cuAmpacity90: Double,
    val cuResistance: Double, // Ohm per 1000 ft
    val alAmpacity60: Double,
    val alAmpacity75: Double,
    val alAmpacity90: Double,
    val alResistance: Double  // Ohm per 1000 ft
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WireGaugeCalcScreen(
    textSizeMultiplier: Float = 1.0f,
    currentLanguage: String = "en"
) {
    val scrollState = rememberScrollState()

    // Professional conductor specifications
    val wireList = remember {
        listOf(
            ConductorSpec("14 AWG", 15.0, 20.0, 25.0, 3.07, 0.0, 0.0, 0.0, 9999.0), // Al 14 not allowed for power
            ConductorSpec("12 AWG", 20.0, 25.0, 30.0, 1.93, 15.0, 20.0, 25.0, 3.18),
            ConductorSpec("10 AWG", 30.0, 35.0, 40.0, 1.21, 25.0, 30.0, 35.0, 2.00),
            ConductorSpec("8 AWG", 40.0, 50.0, 55.0, 0.778, 30.0, 40.0, 45.0, 1.28),
            ConductorSpec("6 AWG", 55.0, 65.0, 75.0, 0.491, 40.0, 50.0, 60.0, 0.808),
            ConductorSpec("4 AWG", 70.0, 85.0, 95.0, 0.308, 55.0, 65.0, 75.0, 0.508),
            ConductorSpec("2 AWG", 95.0, 115.0, 130.0, 0.194, 75.0, 90.0, 100.0, 0.319),
            ConductorSpec("1/0 AWG", 125.0, 150.0, 170.0, 0.122, 100.0, 120.0, 135.0, 0.201),
            ConductorSpec("2/0 AWG", 145.0, 175.0, 195.0, 0.0967, 115.0, 135.0, 150.0, 0.159),
            ConductorSpec("3/0 AWG", 165.0, 200.0, 225.0, 0.0766, 130.0, 155.0, 175.0, 0.126),
            ConductorSpec("4/0 AWG", 195.0, 230.0, 260.0, 0.0608, 155.0, 180.0, 205.0, 0.100)
        )
    }

    val tempRatings = remember { listOf("60 °C", "75 °C", "90 °C") }
    val dropLimitOptions = remember { listOf(1.0, 2.0, 3.0, 5.0) }

    // Dynamic State Controls
    var isCopper by remember { mutableStateOf(true) } // true: Copper, false: Aluminum
    var isSinglePhase by remember { mutableStateOf(true) } // true: Single Phase, false: Three Phase
    var isMeters by remember { mutableStateOf(false) } // true: Meters, false: Feet

    var selectedWireIndex by remember { mutableStateOf(1) } // Default 12 AWG
    var selectedTempIndex by remember { mutableStateOf(1) } // Default 75 °C
    var targetDropLimit by remember { mutableStateOf(3.0) } // Default 3% limit

    var distanceInput by remember { mutableStateOf("100") }
    var voltageInput by remember { mutableStateOf("120") }
    var currentInput by remember { mutableStateOf("16") }

    var wireExpanded by remember { mutableStateOf(false) }
    var tempExpanded by remember { mutableStateOf(false) }

    // Resolve details for currently selected conductor
    val activeWire = wireList[selectedWireIndex]

    val activeResistance = remember(activeWire, isCopper) {
        if (isCopper) activeWire.cuResistance else activeWire.alResistance
    }

    val maxAmpacity = remember(activeWire, isCopper, selectedTempIndex) {
        when (selectedTempIndex) {
            0 -> if (isCopper) activeWire.cuAmpacity60 else activeWire.alAmpacity60
            1 -> if (isCopper) activeWire.cuAmpacity75 else activeWire.alAmpacity75
            else -> if (isCopper) activeWire.cuAmpacity90 else activeWire.alAmpacity90
        }
    }

    // Precise live physical calculations
    val calculatedOutputs = remember(
        isCopper, isSinglePhase, isMeters, selectedWireIndex, selectedTempIndex,
        distanceInput, voltageInput, currentInput, maxAmpacity
    ) {
        val current = currentInput.toDoubleOrNull() ?: 0.0
        val distance = distanceInput.toDoubleOrNull() ?: 0.0
        val voltage = voltageInput.toDoubleOrNull() ?: 120.0

        val distanceFt = if (isMeters) distance * 3.28084 else distance
        val wireRes = if (isCopper) activeWire.cuResistance else activeWire.alResistance

        // Voltage Drop Formula
        // 1-phase: (2 * L * R * I) / 1000
        // 3-phase: (1.732 * L * R * I) / 1000
        val multiplier = if (isSinglePhase) 2.0 else 1.732
        val rawVDrop = (multiplier * distanceFt * wireRes * current) / 1000.0
        val percentage = if (voltage > 0.0) (rawVDrop / voltage) * 100.0 else 0.0
        val physicalOverload = current > maxAmpacity || maxAmpacity <= 0.0

        Triple(rawVDrop, percentage, physicalOverload)
    }

    val finalVoltsDrop = calculatedOutputs.first
    val percentageDrop = calculatedOutputs.second
    val isOverloaded = calculatedOutputs.third

    // Standard Real-World SMART AWG recommendation engine
    val recommendedAWG = remember(
        isCopper, isSinglePhase, isMeters, selectedTempIndex, targetDropLimit,
        distanceInput, voltageInput, currentInput
    ) {
        val current = currentInput.toDoubleOrNull() ?: 0.0
        val distance = distanceInput.toDoubleOrNull() ?: 0.0
        val voltage = voltageInput.toDoubleOrNull() ?: 120.0
        if (current <= 0 || distance <= 0 || voltage <= 0) return@remember null

        val distanceFt = if (isMeters) distance * 3.28084 else distance
        val phaseMultiplier = if (isSinglePhase) 2.0 else 1.732

        // Find the SMALLEST wire gauge (which has the largest resistance index from the wireList starting from bottom to top)
        // Note: our wireList is sorted 14 AWG (smallest) to 4/0 AWG (largest).
        var result: String? = null

        for (spec in wireList) {
            // Determine material specific parameters
            val resUnit = if (isCopper) spec.cuResistance else spec.alResistance
            val amp = when (selectedTempIndex) {
                0 -> if (isCopper) spec.cuAmpacity60 else spec.alAmpacity60
                1 -> if (isCopper) spec.cuAmpacity75 else spec.alAmpacity75
                else -> if (isCopper) spec.cuAmpacity90 else spec.alAmpacity90
            }

            if (amp <= 0.0) continue // Skip non-permitted combinations (e.g. Al 14)

            // Test physical safety threshold
            if (amp >= current) {
                // Test voltage drop compliance limit
                val testVDrop = (phaseMultiplier * distanceFt * resUnit * current) / 1000.0
                val testPercent = (testVDrop / voltage) * 100.0
                if (testPercent <= targetDropLimit) {
                    result = spec.awg
                    break
                }
            }
        }
        result ?: "4/0 AWG +" // Indicator that the limits exceed standard parameters
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SEGMENTED ROW: Material Choices
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = Loc.get("wire_material_sel", currentLanguage),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(4.dp)
            ) {
                // Copper Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCopper) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { isCopper = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Loc.get("wire_copper", currentLanguage),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isCopper) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Aluminum Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isCopper) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { isCopper = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Loc.get("wire_aluminum", currentLanguage),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (!isCopper) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // SEGMENTED ROW: Phase & Distance Unit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Phase Configuration
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Loc.get("wire_phase_sel", currentLanguage),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSinglePhase) MaterialTheme.colorScheme.secondary else Color.Transparent)
                            .clickable { isSinglePhase = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = Loc.get("wire_single_phase", currentLanguage)
                                .replace("-Phase", "")
                                .replace("Monofásico", "1-F")
                                .replace("एकल चरण", "1-Ph")
                                .replace("সিঙ্গল-ফেজ", "সিঙ্গল"),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSinglePhase) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isSinglePhase) MaterialTheme.colorScheme.secondary else Color.Transparent)
                            .clickable { isSinglePhase = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = Loc.get("wire_three_phase", currentLanguage)
                                .replace("-Phase", "")
                                .replace("Trifásico", "3-F")
                                .replace("त्रि-चरण", "3-Ph")
                                .replace("থ্রি-ফেজ", "থ্রি"),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = if (!isSinglePhase) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Dist Unit Configuration
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Loc.get("wire_dist_unit", currentLanguage),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isMeters) MaterialTheme.colorScheme.secondary else Color.Transparent)
                            .clickable { isMeters = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "FT",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = if (!isMeters) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isMeters) MaterialTheme.colorScheme.secondary else Color.Transparent)
                            .clickable { isMeters = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Meters",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isMeters) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // SEGMENTED ROW: AWG and Temperature Ratings Dropdown Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // AWG Dropdown Container
            Box(modifier = Modifier.weight(1f)) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clickable { wireExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = Loc.get("wire_gauge_sel", currentLanguage),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = (10 * textSizeMultiplier).sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activeWire.awg,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (15 * textSizeMultiplier).sp
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "AWG list drop icon"
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded = wireExpanded,
                    onDismissRequest = { wireExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.45f)
                ) {
                    wireList.forEachIndexed { i, spec ->
                        val isAllowed = !( !isCopper && spec.awg == "14 AWG" )
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = if (isAllowed) spec.awg else "${spec.awg} (Cu Only)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isAllowed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                ) 
                            },
                            onClick = {
                                if (isAllowed) {
                                    selectedWireIndex = i
                                    wireExpanded = false
                                }
                            }
                        )
                    }
                }
            }

            // Temperature Rating Container
            Box(modifier = Modifier.weight(1f)) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clickable { tempExpanded = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = Loc.get("wire_temp_sel", currentLanguage),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = (10 * textSizeMultiplier).sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tempRatings[selectedTempIndex],
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (15 * textSizeMultiplier).sp
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Temperature drop indicator"
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded = tempExpanded,
                    onDismissRequest = { tempExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.45f)
                ) {
                    tempRatings.forEachIndexed { i, label ->
                        DropdownMenuItem(
                            text = { Text(text = label, style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                selectedTempIndex = i
                                tempExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // INPUT: Distance
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Loc.get("wire_distance", currentLanguage),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = (10 * textSizeMultiplier).sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    androidx.compose.foundation.text.BasicTextField(
                        value = distanceInput,
                        onValueChange = { distanceInput = it },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = (16 * textSizeMultiplier).sp
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Text(
                    text = if (isMeters) "m" else "ft",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // INPUT: Voltage & Voltage Presets Row
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Loc.get("wire_sys_volt", currentLanguage),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = (10 * textSizeMultiplier).sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        androidx.compose.foundation.text.BasicTextField(
                            value = voltageInput,
                            onValueChange = { voltageInput = it },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = (16 * textSizeMultiplier).sp
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text(
                        text = "V",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Volts Chips Presets
            val voltPresets = listOf("12", "24", "120", "240", "480")
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                voltPresets.forEach { volt ->
                    val selected = voltageInput == volt
                    SuggestionChip(
                        onClick = { voltageInput = volt },
                        label = { Text("$volt V", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)) },
                        colors = if (selected) {
                            SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            SuggestionChipDefaults.suggestionChipColors()
                        }
                    )
                }
            }
        }

        // INPUT: Current Amps & Amps Presets Row
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Loc.get("wire_circuit_load", currentLanguage),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = (10 * textSizeMultiplier).sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        androidx.compose.foundation.text.BasicTextField(
                            value = currentInput,
                            onValueChange = { currentInput = it },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = (16 * textSizeMultiplier).sp
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Text(
                        text = "Amps",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Amps Chips Presets
            val ampPresets = listOf("10", "15", "20", "30", "50")
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ampPresets.forEach { amp ->
                    val selected = currentInput == amp
                    SuggestionChip(
                        onClick = { currentInput = amp },
                        label = { Text("$amp A", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)) },
                        colors = if (selected) {
                            SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            SuggestionChipDefaults.suggestionChipColors()
                        }
                    )
                }
            }
        }

        // CONFIG: TARGET DROP LIMIT CHIPS SELECTOR
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = Loc.get("wire_target_drop", currentLanguage),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                dropLimitOptions.forEach { spec ->
                    val isSelected = targetDropLimit == spec
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { targetDropLimit = spec },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${spec.toInt()}%",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // OUTPUT 1: SMART RECOMMENDED GAUGE SECTIONS
        val recMaterial = if (isCopper) Loc.get("wire_copper", currentLanguage) else Loc.get("wire_aluminum", currentLanguage)
        val recTemp = tempRatings[selectedTempIndex]

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Auto-Size indicator",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Loc.get("wire_rec_title", currentLanguage).uppercase(Locale.US),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp,
                            fontSize = (11 * textSizeMultiplier).sp
                        ),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = recommendedAWG ?: "— AWG",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = (34 * textSizeMultiplier).sp
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (recommendedAWG != null && recommendedAWG.contains("+")) {
                        "Load is too high or distance is too far for a single 4/0 conductor. Consider parallel sizing runs."
                    } else if (recommendedAWG != null) {
                        "Smallest NEC safe conductor for $currentInput A continuous load with safe ampacity limits & under $targetDropLimit% voltage drop using $recMaterial ($recTemp)."
                    } else {
                        "Please enter circuit distance and current parameters to recommend."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = (12 * textSizeMultiplier).sp,
                        lineHeight = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        // OUTPUT 2: THERMAL TEMPERATURE AND AMPACITIES LIMITS (NEC table 310.15)
        val termBannerColor = if (isOverloaded) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        val termTextColor = if (isOverloaded) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = termBannerColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Loc.get("wire_max_safe", currentLanguage),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = (15 * textSizeMultiplier).sp
                        ),
                        color = termTextColor
                    )

                    if (isOverloaded) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Thermal limit exceeded alarm",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (maxAmpacity <= 0.0) "N/A" else "${maxAmpacity.toInt()}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = (38 * textSizeMultiplier).sp
                        ),
                        color = termTextColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Amps",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = (15 * textSizeMultiplier).sp
                        ),
                        color = termTextColor.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (maxAmpacity <= 0.0) {
                        "14 AWG Aluminum is not permitted under physical standard NEC specifications."
                    } else if (isOverloaded) {
                        Loc.getFormatted("wire_danger", currentLanguage, maxAmpacity.toInt(), activeWire.awg)
                    } else {
                        Loc.getFormatted("wire_safe_limit", currentLanguage, activeWire.awg, tempRatings[selectedTempIndex])
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = (12 * textSizeMultiplier).sp,
                        lineHeight = 16.sp
                    ),
                    color = termTextColor
                )
            }
        }

        // OUTPUT 3: REAL-TIME VOLTAGE DROP DETAILS
        val dropExceeded = percentageDrop > targetDropLimit
        val dropStatusColor = if (dropExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Loc.get("wire_calc_drop", currentLanguage),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = (15 * textSizeMultiplier).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "${String.format(Locale.US, "%.2f", finalVoltsDrop)} V",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${String.format(Locale.US, "%.2f", percentageDrop)}%",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = dropStatusColor,
                        fontSize = (38 * textSizeMultiplier).sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Indicator to represent target vs current drop
                val targetSafeVal = targetDropLimit.toFloat()
                val currentProgress = (percentageDrop.toFloat() / (targetSafeVal * 1.5f)).coerceIn(0.01f, 1.0f)

                LinearProgressIndicator(
                    progress = currentProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = dropStatusColor,
                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${Loc.get("wire_target_drop", currentLanguage)}: ${targetDropLimit.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (dropExceeded) {
                        Text(
                            text = Loc.get("wire_exceeds", currentLanguage),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text(
                            text = Loc.get("wire_acceptable", currentLanguage),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // DETAILS: MATHEMATICAL FORMULA METADATA
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "info icon",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Calculation Metadata",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                val distanceFtText = if (isMeters) {
                    val distM = distanceInput.toDoubleOrNull() ?: 0.0
                    "${String.format(Locale.US, "%.1f", distM * 3.28084)} ft"
                } else {
                    "$distanceInput ft"
                }
                Text(
                    text = "• Resistivity (R): $activeResistance Ω / 1000 ft\n" +
                           "• Distance computed: $distanceFtText\n" +
                           "• Applied Limit: $targetDropLimit%\n" +
                           "• Formula: V_drop = " + (if (isSinglePhase) "2" else "1.732") + " * L * R * I / 1000",
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}
