package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TermsScreen(
    onBack: () -> Unit,
    textSizeMultiplier: Float = 1.0f
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title Header block
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Terms & Conditions",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = (26 * textSizeMultiplier).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Last Updated: June 2026",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = (12 * textSizeMultiplier).sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Divider()

        // 1. Acceptance
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "1. Acceptance of Terms",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = (16 * textSizeMultiplier).sp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "By using JK Volt, you agree to these simplified terms. If you do not accept these guidelines, do not run or use this system.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = (15 * textSizeMultiplier).sp, lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 2. Age requirement
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "2. Age Requirement",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = (16 * textSizeMultiplier).sp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "This application is designed and rated for individuals aged 10 and above. It contains educational and professional calculators for electrical engineering principles.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = (15 * textSizeMultiplier).sp, lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 3. App Purpose and Liability Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "3. App Purpose & Liability",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (16 * textSizeMultiplier).sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "JK Volt is provided as a reference utility tool for electrical technicians. Calculations should be verified against official safety codes (like the National Electrical Code or local guidelines) before executing high-voltage physical installations.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = (14 * textSizeMultiplier).sp, lineHeight = 21.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning shield symbol",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Text(
                        text = "The PAD Team and owner Yumkhaibam Joykumar Singh are not liable for hardware damage resulting from incorrect inputs or practical application errors.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = (13 * textSizeMultiplier).sp,
                            lineHeight = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Blueprint card representing safety
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Safety First",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = (18 * textSizeMultiplier).sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Always use calibrated equipment for field measurements.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = (13 * textSizeMultiplier).sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center Button "I Understand" that goes back or finishes
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(52.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = "I Understand",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = (16 * textSizeMultiplier).sp
                )
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}
