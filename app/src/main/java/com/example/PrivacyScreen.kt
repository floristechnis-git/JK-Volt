package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrivacyScreen(
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

        // Header Title
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Privacy Policy",
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

        // Section 1
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "1. Zero Data Collection",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = (16 * textSizeMultiplier).sp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "JK Volt values your absolute privacy. We do not collect, harvest, track, or share any personal information, location details, device IDs, or telemetry data.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = (15 * textSizeMultiplier).sp, lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Section 2
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "2. Strictly Local Storage",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = (16 * textSizeMultiplier).sp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Any preferences you configure within this application (such as your chosen Theme, Text Size, or Language) are stored solely within your device's local memory.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = (15 * textSizeMultiplier).sp, lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Section 3
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "3. No External Transmission",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = (16 * textSizeMultiplier).sp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "The app operates entirely client-side. No data is ever transmitted over the internet to the PAD Team, Yumkhaibam Joykumar Singh, or any third-party servers. The app works 100% offline.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = (15 * textSizeMultiplier).sp, lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Visual design accent card (bento card)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(36.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Privacy by Design",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = (16 * textSizeMultiplier).sp
                        ),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Our architecture is built to ensure you never have to worry about your data leaving your control. No accounts, no clouds, no tracking.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = (13 * textSizeMultiplier).sp,
                            lineHeight = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center Button "Done" that goes back
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(52.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = "Done",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = (16 * textSizeMultiplier).sp
                )
            )
        }

        Divider()

        // Author segment
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Developed with precision by Yumkhaibam Joykumar Singh",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = (11 * textSizeMultiplier).sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.width(32.dp).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Box(modifier = Modifier.width(32.dp).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}
