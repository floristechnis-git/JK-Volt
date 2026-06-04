package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun SettingsScreen(
    currentTheme: String,
    onThemeChanged: (String) -> Unit,
    currentTextSize: String,
    onTextSizeChanged: (String) -> Unit,
    currentLanguage: String,
    onLanguageChanged: (String) -> Unit,
    onNavigateToCapacitor: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    textSizeMultiplier: Float = 1.0f
) {
    val scrollState = rememberScrollState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showTextSizeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // PERSONALIZATION SECTION
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = Loc.get("set_personal", currentLanguage),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = (16 * textSizeMultiplier).sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column {
                    // Theme Row Toggle
                    SettingsItemRow(
                        icon = Icons.Default.Palette,
                        title = Loc.get("set_theme", currentLanguage),
                        subtitle = currentTheme.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() },
                        onClick = { showThemeDialog = true },
                        textSizeMultiplier = textSizeMultiplier
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))

                    // Text Size Setting
                    SettingsItemRow(
                        icon = Icons.Default.TextFields,
                        title = Loc.get("set_text", currentLanguage),
                        subtitle = currentTextSize.uppercase(Locale.US),
                        onClick = { showTextSizeDialog = true },
                        textSizeMultiplier = textSizeMultiplier
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))

                    // Language Row
                    SettingsItemRow(
                        icon = Icons.Default.Language,
                        title = Loc.get("set_lang", currentLanguage),
                        subtitle = when (currentLanguage) {
                            "en" -> "English"
                            "es" -> "Español"
                            "hi" -> "हिन्दी"
                            "mni" -> "মৈতৈলোন"
                            else -> "English"
                        },
                        onClick = { showLanguageDialog = true },
                        textSizeMultiplier = textSizeMultiplier
                    )
                }
            }
        }

        // LEGAL SECTION
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = Loc.get("set_legal", currentLanguage),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = (16 * textSizeMultiplier).sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column {
                    // Privacy Policy Item
                    SettingsItemRow(
                        icon = Icons.Default.Shield,
                        title = Loc.get("nav_privacy", currentLanguage),
                        subtitle = Loc.get("set_privacy_sub", currentLanguage),
                        onClick = onNavigateToPrivacy,
                        textSizeMultiplier = textSizeMultiplier
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))

                    // Terms Item
                    SettingsItemRow(
                        icon = Icons.Default.Description,
                        title = Loc.get("nav_terms", currentLanguage),
                        subtitle = Loc.get("set_terms_sub", currentLanguage),
                        onClick = onNavigateToTerms,
                        textSizeMultiplier = textSizeMultiplier
                    )
                }
            }
        }

        // MORE UTILITIES SECTION
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = Loc.get("set_more", currentLanguage),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = (16 * textSizeMultiplier).sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                // Capacitor Calculator Screen Entry
                SettingsItemRow(
                    icon = Icons.Default.Memory,
                    title = Loc.get("nav_capacitor", currentLanguage),
                    subtitle = Loc.get("set_cap_sub", currentLanguage),
                    onClick = onNavigateToCapacitor,
                    textSizeMultiplier = textSizeMultiplier
                )
            }
        }

        // Footer block
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Bolt brand mark",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = Loc.get("set_version", currentLanguage),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = (14 * textSizeMultiplier).sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = Loc.get("set_developed", currentLanguage),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = (11 * textSizeMultiplier).sp),
                color = MaterialTheme.colorScheme.outline
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
    }

    // THEME CHOOSER DIALOG
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Text(
                    text = "Choose Theme",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = (20 * textSizeMultiplier).sp, fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("system", "light", "dark").forEach { themeOption ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onThemeChanged(themeOption)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = themeOption.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() },
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = (16 * textSizeMultiplier).sp)
                            )
                            RadioButton(
                                selected = (currentTheme == themeOption),
                                onClick = {
                                    onThemeChanged(themeOption)
                                    showThemeDialog = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(Loc.get("cancel", currentLanguage), style = MaterialTheme.typography.labelLarge.copy(fontSize = (14 * textSizeMultiplier).sp))
                }
            }
        )
    }

    // TEXT SIZE CHOOSER DIALOG
    if (showTextSizeDialog) {
        AlertDialog(
            onDismissRequest = { showTextSizeDialog = false },
            title = {
                Text(
                    text = "Choose Text Size",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = (20 * textSizeMultiplier).sp, fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("normal", "large", "xlarge").forEach { sizeOption ->
                        val displayStr = when (sizeOption) {
                            "normal" -> "Normal"
                            "large" -> "Large (1.2x)"
                            "xlarge" -> "Extra Large (1.4x)"
                            else -> "Normal"
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onTextSizeChanged(sizeOption)
                                    showTextSizeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = displayStr,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = when (sizeOption) {
                                        "normal" -> 16.sp
                                        "large" -> 19.sp
                                        "xlarge" -> 22.sp
                                        else -> 16.sp
                                    }
                                )
                            )
                            RadioButton(
                                selected = (currentTextSize == sizeOption),
                                onClick = {
                                    onTextSizeChanged(sizeOption)
                                    showTextSizeDialog = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTextSizeDialog = false }) {
                    Text(Loc.get("cancel", currentLanguage), style = MaterialTheme.typography.labelLarge.copy(fontSize = (14 * textSizeMultiplier).sp))
                }
            }
        )
    }

    // LANGUAGE CHOOSER DIALOG
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text(
                    text = Loc.get("choose_lang", currentLanguage),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = (20 * textSizeMultiplier).sp, fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("en", "es", "hi", "mni").forEach { langOption ->
                        val displayStr = when (langOption) {
                            "en" -> "English"
                            "es" -> "Español"
                            "hi" -> "हिन्दी"
                            "mni" -> "মৈতৈলোন"
                            else -> "English"
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLanguageChanged(langOption)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = displayStr,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = (16 * textSizeMultiplier).sp)
                            )
                            RadioButton(
                                selected = (currentLanguage == langOption),
                                onClick = {
                                    onLanguageChanged(langOption)
                                    showLanguageDialog = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(Loc.get("cancel", currentLanguage), style = MaterialTheme.typography.labelLarge.copy(fontSize = (14 * textSizeMultiplier).sp))
                }
            }
        )
    }
}

@Composable
fun SettingsItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    textSizeMultiplier: Float,
    showArrow: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (16 * textSizeMultiplier).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (13 * textSizeMultiplier).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (showArrow) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate detail arrow",
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
