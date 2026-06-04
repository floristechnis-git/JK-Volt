package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferences = PreferencesManager(this)

        setContent {
            // Read saved preference states with dynamic change callbacks
            var appThemeState by remember { mutableStateOf(preferences.theme) }
            var appTextSizeState by remember { mutableStateOf(preferences.textSize) }
            var appLanguageState by remember { mutableStateOf(preferences.language) }
            var isOnboardedState by remember { mutableStateOf(preferences.isOnboarded) }

            // Decide current dark theme configuration
            val darkTheme = when (appThemeState) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            // Decide current text size scale multiplier
            val textMultiplier = when (appTextSizeState) {
                "large" -> 1.2f
                "xlarge" -> 1.4f
                else -> 1.0f
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppShell(
                        isOnboarded = isOnboardedState,
                        onOnboardingCompleted = {
                            preferences.isOnboarded = true
                            isOnboardedState = true
                        },
                        currentTheme = appThemeState,
                        onThemeChanged = {
                            preferences.theme = it
                            appThemeState = it
                        },
                        currentTextSize = appTextSizeState,
                        onTextSizeChanged = {
                            preferences.textSize = it
                            appTextSizeState = it
                        },
                        currentLanguage = appLanguageState,
                        onLanguageChanged = {
                            preferences.language = it
                            appLanguageState = it
                        },
                        textSizeMultiplier = textMultiplier
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell(
    isOnboarded: Boolean,
    onOnboardingCompleted: () -> Unit,
    currentTheme: String,
    onThemeChanged: (String) -> Unit,
    currentTextSize: String,
    onTextSizeChanged: (String) -> Unit,
    currentLanguage: String,
    onLanguageChanged: (String) -> Unit,
    textSizeMultiplier: Float
) {
    // Client-side simple state router
    var currentScreen by remember { mutableStateOf(if (isOnboarded) "resistor" else "welcome") }
    
    // Backstack history of preceding screens
    val backstack = remember { mutableStateListOf<String>() }

    fun navigateTo(screenKey: String) {
        if (currentScreen != screenKey) {
            backstack.add(currentScreen)
            currentScreen = screenKey
        }
    }

    fun navigateBack() {
        if (backstack.isNotEmpty()) {
            currentScreen = backstack.removeAt(backstack.lastIndex)
        }
    }

    // Capture physical Android hardware back button presses safely
    BackHandler(enabled = backstack.isNotEmpty()) {
        navigateBack()
    }

    // Responsive window class adaptive layout
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 600.dp

        if (currentScreen == "welcome") {
            WelcomeScreen(
                onGetStarted = {
                    onOnboardingCompleted()
                    navigateTo("resistor")
                },
                textSizeMultiplier = textSizeMultiplier,
                currentLanguage = currentLanguage
            )
        } else {
            Scaffold(
                topBar = {
                    // Adapt the top bar layout contextually
                    val isSubScreen = currentScreen in listOf("settings", "terms", "privacy", "capacitor")

                    if (!isSubScreen) {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = "Bolt decoration icon",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Text(
                                        text = "JK Volt",
                                        fontWeight = FontWeight.Black,
                                        fontSize = (24 * textSizeMultiplier).sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = { navigateTo("settings") }) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Open Settings Screen",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    } else {
                        // Title segment for detail screens
                        val subScreenTitle = when (currentScreen) {
                            "settings" -> Loc.get("nav_settings", currentLanguage)
                            "capacitor" -> Loc.get("nav_capacitor", currentLanguage)
                            "terms" -> Loc.get("nav_terms", currentLanguage)
                            "privacy" -> Loc.get("nav_privacy", currentLanguage)
                            else -> "JK Volt"
                        }

                        TopAppBar(
                            title = {
                                Text(
                                    text = subScreenTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (20 * textSizeMultiplier).sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { navigateBack() }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Return back preceding screen",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                },
                bottomBar = {
                    val isMainScreen = currentScreen in listOf("resistor", "ohms_law", "smd_code", "wire_gauge")
                    if (!isWideScreen && isMainScreen) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = (currentScreen == "resistor"),
                                onClick = { navigateTo("resistor") },
                                label = { Text(Loc.get("nav_resistor", currentLanguage), fontSize = (11 * textSizeMultiplier).sp) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Reorder,
                                        contentDescription = "Tab Resistor Color calculator"
                                    )
                                }
                            )

                            NavigationBarItem(
                                selected = (currentScreen == "ohms_law"),
                                onClick = { navigateTo("ohms_law") },
                                label = { Text(Loc.get("nav_ohms_law", currentLanguage), fontSize = (11 * textSizeMultiplier).sp) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Calculate,
                                        contentDescription = "Tab Ohm's Law formulas solver"
                                    )
                                }
                            )

                            NavigationBarItem(
                                selected = (currentScreen == "smd_code"),
                                onClick = { navigateTo("smd_code") },
                                label = { Text(Loc.get("nav_smd_code", currentLanguage), fontSize = (11 * textSizeMultiplier).sp) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Apps,
                                        contentDescription = "Tab SMD Resistor marks decoder"
                                    )
                                }
                            )

                            NavigationBarItem(
                                selected = (currentScreen == "wire_gauge"),
                                onClick = { navigateTo("wire_gauge") },
                                label = { Text(Loc.get("nav_wire_gauge", currentLanguage), fontSize = (11 * textSizeMultiplier).sp) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Straighten,
                                        contentDescription = "Tab AWG sizes drop analyzer"
                                    )
                                }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Flanking vertical sidebar elements on wide/expanded tablet screens
                    val isMainScreen = currentScreen in listOf("resistor", "ohms_law", "smd_code", "wire_gauge")
                    if (isWideScreen && isMainScreen) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.width(96.dp)
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))

                            NavigationRailItem(
                                selected = (currentScreen == "resistor"),
                                onClick = { navigateTo("resistor") },
                                label = { Text(Loc.get("nav_resistor", currentLanguage), fontSize = (11 * textSizeMultiplier).sp, fontWeight = FontWeight.Bold) },
                                icon = {
                                    Icon(imageVector = Icons.Default.Reorder, contentDescription = "Resistor")
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            NavigationRailItem(
                                selected = (currentScreen == "ohms_law"),
                                onClick = { navigateTo("ohms_law") },
                                label = { Text(Loc.get("nav_ohms_law", currentLanguage), fontSize = (11 * textSizeMultiplier).sp, fontWeight = FontWeight.Bold) },
                                icon = {
                                    Icon(imageVector = Icons.Default.Calculate, contentDescription = "Ohm's Law")
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            NavigationRailItem(
                                selected = (currentScreen == "smd_code"),
                                onClick = { navigateTo("smd_code") },
                                label = { Text(Loc.get("nav_smd_code", currentLanguage), fontSize = (11 * textSizeMultiplier).sp, fontWeight = FontWeight.Bold) },
                                icon = {
                                    Icon(imageVector = Icons.Default.Apps, contentDescription = "SMD Code")
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            NavigationRailItem(
                                selected = (currentScreen == "wire_gauge"),
                                onClick = { navigateTo("wire_gauge") },
                                label = { Text(Loc.get("nav_wire_gauge", currentLanguage), fontSize = (11 * textSizeMultiplier).sp, fontWeight = FontWeight.Bold) },
                                icon = {
                                    Icon(imageVector = Icons.Default.Straighten, contentDescription = "Wire Gauge")
                                }
                            )
                        }
                    }

                    // Centered Calculators Screen viewport
                    Box(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "Page Transition Router"
                        ) { target ->
                            when (target) {
                                "resistor" -> ResistorCalcScreen(textSizeMultiplier = textSizeMultiplier, currentLanguage = currentLanguage)
                                "ohms_law" -> OhmsLawCalcScreen(textSizeMultiplier = textSizeMultiplier, currentLanguage = currentLanguage)
                                "smd_code" -> SmdCodeCalcScreen(textSizeMultiplier = textSizeMultiplier, currentLanguage = currentLanguage)
                                "wire_gauge" -> WireGaugeCalcScreen(textSizeMultiplier = textSizeMultiplier, currentLanguage = currentLanguage)
                                "capacitor" -> CapacitorCalcScreen(textSizeMultiplier = textSizeMultiplier, currentLanguage = currentLanguage)
                                "settings" -> SettingsScreen(
                                    currentTheme = currentTheme,
                                    onThemeChanged = onThemeChanged,
                                    currentTextSize = currentTextSize,
                                    onTextSizeChanged = onTextSizeChanged,
                                    currentLanguage = currentLanguage,
                                    onLanguageChanged = onLanguageChanged,
                                    onNavigateToCapacitor = { navigateTo("capacitor") },
                                    onNavigateToTerms = { navigateTo("terms") },
                                    onNavigateToPrivacy = { navigateTo("privacy") },
                                    textSizeMultiplier = textSizeMultiplier
                                )
                                "terms" -> TermsScreen(onBack = { navigateBack() }, textSizeMultiplier = textSizeMultiplier)
                                "privacy" -> PrivacyScreen(onBack = { navigateBack() }, textSizeMultiplier = textSizeMultiplier)
                            }
                        }
                    }
                }
            }
        }
    }
}
