package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.util.Base64
import android.provider.MediaStore
import android.graphics.ImageDecoder
import android.os.Build
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BookmarkedItem
import com.example.data.MedicalReport
import com.example.ui.theme.*
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmaSenseApp(viewModel: PharmaViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                listOf(
                    Triple("Home", Icons.Filled.Home, "home_tab"),
                    Triple("Search", Icons.Filled.Search, "search_tab"),
                    Triple("Reports", Icons.Filled.Assignment, "reports_tab"),
                    Triple("Interactions", Icons.Filled.CompareArrows, "interactions_tab"),
                    Triple("AI Chat", Icons.Filled.QuestionAnswer, "chat_tab"),
                    Triple("Profile", Icons.Filled.Person, "profile_tab")
                ).forEach { (tabName, icon, tag) ->
                    NavigationBarItem(
                        selected = currentTab == tabName,
                        onClick = {
                            viewModel.currentTab.value = tabName
                            focusManager.clearFocus()
                        },
                        icon = { Icon(icon, contentDescription = tabName) },
                        label = { Text(tabName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        modifier = Modifier.testTag(tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .clinicalDotGrid(MaterialTheme.colorScheme.primary)
        ) {
            AnimatedContent(
                targetState = currentTab,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "TabTransition"
            ) { targetTab ->
                when (targetTab) {
                    "Home" -> HomeTabContent(viewModel, onSettingsClick = { showSettingsDialog = true })
                    "Search" -> SearchTabContent(viewModel, onSettingsClick = { showSettingsDialog = true })
                    "Reports" -> ReportsTabContent(viewModel, onSettingsClick = { showSettingsDialog = true })
                    "Interactions" -> InteractionsTabContent(viewModel, onSettingsClick = { showSettingsDialog = true })
                    "AI Chat" -> ChatTabContent(viewModel, onSettingsClick = { showSettingsDialog = true })
                    "Profile" -> ProfileTabContent(viewModel, onSettingsClick = { showSettingsDialog = true })
                    else -> HomeTabContent(viewModel, onSettingsClick = { showSettingsDialog = true })
                }
            }
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(viewModel = viewModel, onDismiss = { showSettingsDialog = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(viewModel: PharmaViewModel, onDismiss: () -> Unit) {
    val viewAsOrbit by viewModel.viewAsOrbit.collectAsStateWithLifecycle()
    val useAiForLabReports by viewModel.useAiForLabReports.collectAsStateWithLifecycle()
    val useAiForSymptoms by viewModel.useAiForSymptoms.collectAsStateWithLifecycle()
    val useAiForInteractions by viewModel.useAiForInteractions.collectAsStateWithLifecycle()
    val useAiForScanner by viewModel.useAiForScanner.collectAsStateWithLifecycle()
    val useAiForDashboard by viewModel.useAiForDashboard.collectAsStateWithLifecycle()
    val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()

    var apiKeyInput by remember { mutableStateOf(customApiKey) }
    val context = androidx.compose.ui.platform.LocalContext.current

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "App Settings & Preferences",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- SECTION 1: INTERACTIVE ORBIT PREFERENCE ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Interactive Categories Orbit ⚛️",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Display catalog diseases in a revolving orbital wheel instead of a simple scroll list.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = viewAsOrbit,
                                onCheckedChange = { viewModel.setViewAsOrbit(it) },
                                modifier = Modifier.testTag("setting_orbit_switch")
                            )
                        }
                    }
                }

                // --- SECTION 2: AI VS OFFLINE TOGGLES ---
                Text(
                    text = "Feature Engine Config (AI vs. Offline)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Lab Reports Analyzer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Lab Reports Analyzer 📊", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Enabled: Gemini Clinical Analysis. Disabled: Local medical ranges scanner.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = useAiForLabReports,
                                onCheckedChange = { viewModel.setUseAiForLabReports(it) },
                                modifier = Modifier.testTag("setting_ai_reports_switch")
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        // Symptom Triage Check
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Symptom Triage Check 🩺", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Enabled: Gemini symptom assistant. Disabled: Local red flags & triage guidelines.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = useAiForSymptoms,
                                onCheckedChange = { viewModel.setUseAiForSymptoms(it) },
                                modifier = Modifier.testTag("setting_ai_symptoms_switch")
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        // Drug Interaction Checker
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Drug Interaction Matrix 💊", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Enabled: Deep AI molecular overlaps. Disabled: Local SQLite interaction table.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = useAiForInteractions,
                                onCheckedChange = { viewModel.setUseAiForInteractions(it) },
                                modifier = Modifier.testTag("setting_ai_interactions_switch")
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        // Label Scanner
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Medicine Container Label Scanner 🔍", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Enabled: AI pharmaceutical label parsing. Disabled: Local molecular database indexing.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = useAiForScanner,
                                onCheckedChange = { viewModel.setUseAiForScanner(it) },
                                modifier = Modifier.testTag("setting_ai_scanner_switch")
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        // Dashboard Recommendations
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Dashboard Advice & Discoveries 🥗", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Enabled: Custom AI nutrition charts and news. Disabled: Deterministic offline tables.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = useAiForDashboard,
                                onCheckedChange = { viewModel.setUseAiForDashboard(it) },
                                modifier = Modifier.testTag("setting_ai_dashboard_switch")
                            )
                        }
                    }
                }

                // --- SECTION 3: CUSTOM GEMINI API KEY ---
                Text(
                    text = "API Key Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Provide Your Own Gemini API Key 🔑",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "By default, this app uses our shared pre-configured key. If you exhaust the shared quota limit, you can easily obtain a free personal key from Google AI Studio and register it below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            label = { Text("Gemini API Key") },
                            placeholder = { Text("AIzaSy...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("custom_api_key_input"),
                            trailingIcon = {
                                if (apiKeyInput.isNotEmpty()) {
                                    IconButton(onClick = { apiKeyInput = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                apiKeyInput = ""
                                viewModel.setCustomApiKey("")
                                android.widget.Toast.makeText(context, "API Key cleared. Using default key.", android.widget.Toast.LENGTH_SHORT).show()
                            }) {
                                Text("Reset to Default")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.setCustomApiKey(apiKeyInput.trim())
                                    android.widget.Toast.makeText(context, "Custom API Key saved successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("save_api_key_button")
                            ) {
                                Text("Save Key")
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 4.dp))

                        // KEY ACQUISITION GUIDE
                        Text(
                            text = "💡 How to copy and register your key:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            StepItem(1, "Click the link button below to open Google AI Studio in your browser.")
                            StepItem(2, "Log in with any standard Google or Gmail account.")
                            StepItem(3, "Click the 'Get API Key' button at the top left of the dashboard.")
                            StepItem(4, "Create a key (or copy an existing one) and copy its alphanumeric code.")
                            StepItem(5, "Paste the copied key in the text field above and click 'Save Key'.")
                        }

                        val intentUri = "https://aistudio.google.com/"
                        Button(
                            onClick = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(intentUri))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("get_api_key_link_button")
                        ) {
                            Icon(Icons.Default.Launch, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Get Free Gemini API Key Direct Link 🌐")
                        }
                    }
                }

                // --- SECTION 3.5: USER MANUAL GUIDE ---
                Text(
                    text = "App User Manual & Practice Guide",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "📖 Comprehensive User Manual",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = "PharmaSense is designed as a professional computer science research prototype. Learn how to leverage all its diagnostic and exploration modules:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ManualStepItem("1. Home Dashboard & Rotating Orbit ⚛️", "Toggle the 'Interactive Categories Orbit' switch at the top. On the Home tab, a revolving circular orbital grid will appear. Tap on neurology, cardiology, etc. to see their sub-classes. Tap on the center atomic core (⚛️) to play/pause the orbital revolving animation.")
                            ManualStepItem("2. Diagnostic Suite Sheet Analyzer 📊", "Go to the 'Reports' tab. Enter numerical results for Hb, WBC, Glucose, or Blood Pressure. Tap 'Analyze Lab Report' to compute local reference deviations. If AI Mode is enabled, the results will be routed to Gemini for full generic medication suggestions and clinical summaries.")
                            ManualStepItem("3. Medical Image Upload 📸", "Inside 'Analyze Reports', tap the 'Select & Load Report' action to upload a medical sheet. The app will securely encode the image and send it to Gemini for optical character recognition and branded alternative suggestions.")
                            ManualStepItem("4. Symptom Checker & Quick Triage 🩺", "In the 'Symptom Checker' sub-tab, enter symptoms or tap quick selection chips like 'Severe Chest Pain'. The system performs instant red-flags scans and provides triage directions.")
                            ManualStepItem("5. Drug Interaction Matrix 💊", "Go to the 'Interactions' tab. Search and pick a baseline drug (e.g. Metformin), then check its molecular overlaps and contraindications against other compounds locally or with AI.")
                            ManualStepItem("6. Personal AI Key Setup 🔑", "If the preloaded free shared quota limit is exhausted, enter your Gemini Key above. The client will immediately route your operations via your own secure, free Google developer channel.")
                        }
                    }
                }

                // --- SECTION 4: APP INFORMATION ("ABOUT") ---
                Text(
                    text = "App Architecture & Specifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "ℹ️ Tech Stack Specifications",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            BulletItem("Frontend Architecture: Kotlin, Jetpack Compose, Material Design 3 (M3)")
                            BulletItem("Local Database Engine: SQLite via Android Room ORM")
                            BulletItem("Concurrency Framework: Kotlin Coroutines & Flow State Flows")
                            BulletItem("Network Client Stack: Retrofit 2 with OkHttp 4 client")
                            BulletItem("Data Serialization: kotlinx.serialization library")
                            BulletItem("Third-Party APIs: Google Gemini Pro / Flash Multimodal AI")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// --- COMMONS ---

@Composable
fun SectionHeader(title: String, icon: ImageVector? = null, color: Color = MaterialTheme.colorScheme.primary) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun MarkdownText(text: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    val lines = text.split("\n")
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lines.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) return@forEach

            when {
                trimmed.startsWith("###") -> {
                    Text(
                        text = trimmed.replace("###", "").trim(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                    )
                }
                trimmed.startsWith("##") -> {
                    Text(
                        text = trimmed.replace("##", "").trim(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                }
                trimmed.startsWith("•") || trimmed.startsWith("-") || trimmed.startsWith("*") -> {
                    Row(
                        modifier = Modifier.padding(start = 12.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "• ",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        val textPart = trimmed.substring(1).trim().replace("**", "")
                        Text(
                            text = textPart,
                            style = MaterialTheme.typography.bodyMedium,
                            color = color
                        )
                    }
                }
                trimmed.startsWith("1.") || trimmed.startsWith("2.") || trimmed.startsWith("3.") || trimmed.startsWith("4.") || trimmed.startsWith("5.") -> {
                    Row(
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = trimmed.take(3),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = trimmed.drop(3).trim().replace("**", ""),
                            style = MaterialTheme.typography.bodyMedium,
                            color = color
                        )
                    }
                }
                else -> {
                    Text(
                        text = trimmed.replace("**", ""),
                        style = MaterialTheme.typography.bodyMedium,
                        color = color,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

// Custom decorative card background canvas drawing
fun Modifier.glassyGlow(color: Color): Modifier = this.drawBehind {
    drawCircle(
        color = color.copy(alpha = 0.08f),
        radius = size.width / 2.2f,
        center = Offset(size.width * 0.9f, size.height * 0.1f)
    )
    drawCircle(
        color = color.copy(alpha = 0.04f),
        radius = size.width / 3.5f,
        center = Offset(size.width * 0.1f, size.height * 0.85f)
    )
}

// Clinical premium dot pattern background drawing
fun Modifier.clinicalDotGrid(color: Color): Modifier = this.drawBehind {
    val dotSpacing = 24.dp.toPx()
    val dotRadius = 1.2.dp.toPx()
    val width = size.width
    val height = size.height
    
    var x = 0f
    while (x < width) {
        var y = 0f
        while (y < height) {
            drawCircle(
                color = color.copy(alpha = 0.04f),
                radius = dotRadius,
                center = Offset(x, y)
            )
            y += dotSpacing
        }
        x += dotSpacing
    }
}

// --- 1. HOME TAB ---

@Composable
fun RadialCategoryOrbitalWheel(viewModel: PharmaViewModel) {
    var activeIndex by remember { mutableStateOf(0) }
    var angleOffset by remember { mutableStateOf(0f) }
    var isAutoRotating by remember { mutableStateOf(true) }

    var selectedSubclass by remember { mutableStateOf<MedicineSubclass?>(null) }
    var selectedDrug by remember { mutableStateOf<CommonDrugInfo?>(null) }

    // Reset drill-down state on active index change
    LaunchedEffect(activeIndex) {
        selectedSubclass = null
        selectedDrug = null
    }
    
    // Connected category indices map (matching similar health contexts from React template!)
    val relatedIndices = remember {
        mapOf(
            0 to 4, // Neurology <-> Endocrinology (Brain <-> Hormones)
            1 to 5, // Gastrology <-> Allergies & Pain (Stomach <-> Pain Relief)
            2 to 0, // Cardiology <-> Neurology (Heart <-> Brain)
            3 to 5, // Infections & Fungus <-> Allergies & Pain
            4 to 2, // Endocrinology <-> Cardiology
            5 to 1  // Allergies & Pain <-> Gastrology
        )
    }

    // Auto-rotation clock
    LaunchedEffect(isAutoRotating) {
        if (isAutoRotating) {
            while (true) {
                withFrameMillis {
                    angleOffset = (angleOffset + 0.28f) % 360f
                }
            }
        }
    }

    val categories = MedicalCatalog.categories

    // Map categories to high-fidelity, high-resolution emojis & custom visual properties
    val categoryDetails = remember {
        listOf(
            Triple("🧠", Icons.Default.Info, "Neurology"),
            Triple("🥗", Icons.Default.Refresh, "Gastrology"),
            Triple("🫀", Icons.Default.Favorite, "Cardiology"),
            Triple("🦠", Icons.Default.Warning, "Infections & Fungus"),
            Triple("🩸", Icons.Default.Settings, "Endocrinology"),
            Triple("💊", Icons.Default.Info, "Allergies & Pain")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Selector Header with toggle description
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Interactive Medical Wheel",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Explore nodes, connection lines & conditions",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            // Spin Controller / Pause Indicator
            IconButton(
                onClick = { isAutoRotating = !isAutoRotating },
                modifier = Modifier
                    .background(
                        if (isAutoRotating) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    )
                    .size(34.dp)
            ) {
                Icon(
                    imageVector = if (isAutoRotating) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Playback Control",
                    tint = if (isAutoRotating) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Circular Orbital Interactive Box
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            val width = maxWidth
            val height = maxHeight
            
            val radius = 92.dp
            val density = androidx.compose.ui.platform.LocalDensity.current
            
            val radiusPx = with(density) { radius.toPx() }
            val centerX = with(density) { (width / 2).toPx() }
            val centerY = with(density) { (height / 2).toPx() }

            // Pulse effects for centerpiece
            val infiniteTransition = rememberInfiniteTransition(label = "corePulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.35f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1600, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "scale"
            )
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 0.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1600, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "alpha"
            )

            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary

            // Draw radial orbital helper lines, concentric rings and glowing path beams
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cX = size.width / 2
                val cY = size.height / 2

                // 1. Draw outer primary dashed circular orbit
                drawCircle(
                    color = primaryColor.copy(alpha = 0.15f),
                    radius = radiusPx,
                    center = Offset(cX, cY),
                    style = Stroke(
                        width = 2.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                    )
                )

                // 2. Draw inner concentric helper pathway ring
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.08f),
                    radius = radiusPx * 0.58f,
                    center = Offset(cX, cY),
                    style = Stroke(
                        width = 1.2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 10f), 0f)
                    )
                )

                // 3. Draw active connector beam to selected node element
                val activeAngle = (activeIndex * (360f / categories.size) + angleOffset) % 360f
                val activeRadian = Math.toRadians(activeAngle.toDouble())
                val endX = cX + (radiusPx * Math.cos(activeRadian)).toFloat()
                val endY = cY + (radiusPx * Math.sin(activeRadian)).toFloat()

                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.85f), secondaryColor.copy(alpha = 0.15f))
                    ),
                    start = Offset(cX, cY),
                    end = Offset(endX, endY),
                    strokeWidth = 3.5f
                )

                // 4. Draw connection line to related categories (The React "Link" effect!)
                val relatedIdx = relatedIndices[activeIndex] ?: -1
                if (relatedIdx != -1) {
                    val relAngle = (relatedIdx * (360f / categories.size) + angleOffset) % 360f
                    val relRadian = Math.toRadians(relAngle.toDouble())
                    val rX = cX + (radiusPx * Math.cos(relRadian)).toFloat()
                    val rY = cY + (radiusPx * Math.sin(relRadian)).toFloat()

                    drawLine(
                        color = secondaryColor.copy(alpha = 0.35f),
                        start = Offset(endX, endY),
                        end = Offset(rX, rY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                }
            }

            // Central Glowing Aura Nucleus
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.Center)
                    .drawBehind {
                        drawCircle(
                            color = primaryColor.copy(alpha = pulseAlpha),
                            radius = (size.width / 2) * pulseScale
                        )
                    }
            )

            // Center core visual anchor
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.Center)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(primaryColor, secondaryColor)
                        ),
                        CircleShape
                    )
                    .border(1.5.dp, Color.White.copy(alpha = 0.55f), CircleShape)
                    .clickable {
                        isAutoRotating = !isAutoRotating
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚛️",
                    fontSize = 22.sp
                )
            }

            // Floating nodes representation
            categories.forEachIndexed { idx, item ->
                val (emoji, _, _) = categoryDetails.getOrElse(idx) {
                    Triple("🩺", Icons.Default.Info, item.first)
                }

                val nodeAngle = (idx * (360f / categories.size) + angleOffset) % 360f
                val radian = Math.toRadians(nodeAngle.toDouble())
                
                // Absolute polar placement coordinates in DP
                val offsetXDp = with(density) { (radiusPx * Math.cos(radian)).toFloat().toDp() }
                val offsetYDp = with(density) { (radiusPx * Math.sin(radian)).toFloat().toDp() }

                val isSelected = activeIndex == idx
                val isRelatedNeighbor = idx == relatedIndices[activeIndex]
                
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = offsetXDp, y = offsetYDp)
                        .size(if (isSelected) 56.dp else 44.dp)
                        .background(
                            color = when {
                                isSelected -> primaryColor
                                isRelatedNeighbor -> secondaryColor.copy(alpha = 0.65f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
                            },
                            shape = CircleShape
                        )
                        .border(
                            width = if (isSelected) 2.5.dp else if (isRelatedNeighbor) 1.8.dp else 1.dp,
                            color = when {
                                isSelected -> Color.White
                                isRelatedNeighbor -> secondaryColor
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                            },
                            shape = CircleShape
                        )
                        .clickable {
                            activeIndex = idx
                            isAutoRotating = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = if (isSelected) 22.sp else 17.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    // Extra indicator halo for selections
                    if (isSelected || isRelatedNeighbor) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(
                                    width = 1.dp,
                                    color = (if (isSelected) primaryColor else secondaryColor).copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selected category full details drawer card
        val activeItem = categories[activeIndex]
        val activeEmojis = categoryDetails.getOrElse(activeIndex) { Triple("🩺", Icons.Default.Info, activeItem.first) }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedDrug != null) {
                    // level 3: Drug Details & Molecular structure (ChemSketch/PubChem API)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = { selectedDrug = null },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "Back to Subclass",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = selectedDrug!!.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = selectedDrug!!.hindiName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                selectedSubclass = null
                                selectedDrug = null
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))

                    // Drug Info Cards
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Dose: ${selectedDrug!!.standardDose}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Text(
                            text = "🎯 Indications: ${selectedDrug!!.indications}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Sasta Alternative Brand comparison (User requested generic suggestions)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.22f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "💡 Affordable & High-Quality Substitute",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "SAVE ${selectedDrug!!.costPercentSaving}%",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = "Prescribed Brand: ${selectedDrug!!.representativeBrand} (Expensive)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "Generic Substitute: ${selectedDrug!!.genericAlternative}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }

                        // ChemSketch API replacement using PubChem REST 2D depiction
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⚛️ Molecular Chemical Structure (Chem Sketch 2D)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/${selectedDrug!!.name}/PNG",
                                    contentDescription = "${selectedDrug!!.name} Structure",
                                    modifier = Modifier.fillMaxHeight(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "2D Molecular structure depiction dynamically loaded for chemical analysis.",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (selectedSubclass != null) {
                    // level 2: Subclass treatment category
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = { selectedSubclass = null },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = selectedSubclass!!.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Text(
                        text = selectedSubclass!!.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Select a drug to view structure & sasta alternative:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        selectedSubclass!!.commonDrugs.forEach { drug ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedDrug = drug },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = drug.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Indication: ${drug.indications}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = "Open Drug Details",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // level 1: Main Category Overview (Polished Infographic Panel with zero text clutter)
                    val subclasses = MedicalCatalog.categorySubclasses[activeItem.first] ?: emptyList()
                    val totalDrugs = subclasses.sumOf { it.commonDrugs.size }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(activeEmojis.first, fontSize = 18.sp)
                            }
                            Column {
                                Text(
                                    text = activeItem.first,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "THERAPEUTIC SPECIALTY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        // Node Nav Arrows
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = {
                                    activeIndex = (activeIndex - 1 + categories.size) % categories.size
                                    isAutoRotating = false
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "Previous Node",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    activeIndex = (activeIndex + 1) % categories.size
                                    isAutoRotating = false
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Next Node",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Dynamic Mini Stats Badges (Infographic Style!)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "🔬 ${subclasses.size} Drug Classes",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "💊 ${totalDrugs} Core Formulations",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    // Short, crisp clinical summary
                    Text(
                        text = activeItem.second,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    // Therapeutic Focus Areas (Horizontal tag chips instead of massive vertical card lists!)
                    if (subclasses.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Therapeutic Focus Areas:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                subclasses.forEach { subclass ->
                                    Surface(
                                        onClick = { selectedSubclass = subclass },
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("🔍", fontSize = 11.sp)
                                            Text(
                                                text = subclass.name,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Single clean action button & dynamic assistant hook
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                viewModel.currentTab.value = "Search"
                                viewModel.onSearchQueryChanged(activeItem.first)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Search Care Guidelines ➡️", fontWeight = FontWeight.Black)
                        }

                        TextButton(
                            onClick = {
                                viewModel.currentTab.value = "AI Chat"
                                viewModel.chatInput.value = "Tell me about typical medical management, drug classes, and lifestyle guidelines for ${activeItem.first}."
                            }
                        ) {
                            Text(
                                text = "Consult Clinical AI on ${activeItem.first} ✨",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- 1. HOME TAB ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTabContent(viewModel: PharmaViewModel, onSettingsClick: () -> Unit) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var activeHomeView by remember { mutableStateOf("dashboard") } // "dashboard", "categories_list", "category_detail"
    var selectedInfographicCategory by remember { mutableStateOf<InfographicCategory?>(null) }
    var selectedDrugForDetail by remember { mutableStateOf<InfographicDrug?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showDisclaimer by remember { mutableStateOf(true) }

    if (activeHomeView == "categories_list") {
        CategoriesListView(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onBackClick = { activeHomeView = "dashboard" },
            onCategorySelect = { category ->
                selectedInfographicCategory = category
                activeHomeView = "category_detail"
            },
            onDrugSelect = { drug -> selectedDrugForDetail = drug }
        )
    } else if (activeHomeView == "category_detail") {
        val currentCategory = selectedInfographicCategory
        if (currentCategory != null) {
            CategoryDetailView(
                category = currentCategory,
                onBackClick = { activeHomeView = "categories_list" },
                onDrugSelect = { drug -> selectedDrugForDetail = drug }
            )
        } else {
            activeHomeView = "dashboard"
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Dashboard Header with Settings Button (Concise & Premium)
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💊", fontSize = 20.sp)
                    }
                    Column {
                        Text(
                            text = "PharmaSense",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Clinical Hub",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                        .size(38.dp)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 2. Compact Academic Pill Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(30.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(30.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Project Details",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Project: Anish Kumar (Roll: BPH/10041/23)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 3. Infographic Core Database Counters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Drug Count
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("100+", fontSize = 22.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        Text("Drugs", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                // Classes Count
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("10", fontSize = 22.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                        Text("Therapies", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                // Offline Local Engine
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("100%", fontSize = 22.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.tertiary)
                        Text("Offline", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // 4. Interactive 100 Drugs Guide Entry Card (Visual, Short Text)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        searchQuery = ""
                        activeHomeView = "categories_list"
                    }
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Book",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📖 100 Drugs Handbook",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Indications, Side-Effects & Generic Alternatives",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Go",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // 5. Quick Search Card (Compact)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.currentTab.value = "Search"
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Search diseases, drug class, symptoms...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
            }

            // 6. Popular Categories Toggle + Orbital Wheel / Grid
            var viewAsOrbit by remember { mutableStateOf(true) }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Popular Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                TextButton(
                    onClick = { viewAsOrbit = !viewAsOrbit },
                    modifier = Modifier.testTag("categories_view_toggle")
                ) {
                    Text(
                        text = if (viewAsOrbit) "View List 📋" else "Interactive Orbit ⚛️",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (viewAsOrbit) {
                RadialCategoryOrbitalWheel(viewModel = viewModel)
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(MedicalCatalog.categories) { (cat, desc) ->
                        val icon = when (cat) {
                            "Cardiovascular" -> Icons.Default.Favorite
                            "Respiratory" -> Icons.Default.Info
                            "Gastrointestinal" -> Icons.Default.Refresh
                            "Infectious Diseases" -> Icons.Default.Warning
                            "Endocrine" -> Icons.Default.Settings
                            else -> Icons.Default.Info
                        }
                        Card(
                            modifier = Modifier
                                .width(150.dp)
                                .clickable {
                                    viewModel.currentTab.value = "Search"
                                    viewModel.onSearchQueryChanged(cat)
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                                Text(cat, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(desc, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }

            // 7. Emergency Triage Shortcut (Compact & Premium, Alert Pill)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DangerAlertRed.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DangerAlertRed.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(DangerAlertRed.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationImportant,
                                    contentDescription = "Alert",
                                    tint = DangerAlertRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Emergency Triage",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = DangerAlertRed
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(DangerAlertRed, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "CRITICAL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 9.sp
                            )
                        }
                    }

                    Text(
                        text = "Instant clinical safety evaluation for chest pain, sudden breathlessness, or acute symptoms.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            viewModel.currentTab.value = "Reports"
                            viewModel.symptomInput.value = "Chest pain, breathlessness"
                            viewModel.analyzeSymptoms()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerAlertRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("triage_eval_btn")
                    ) {
                        Text("Trigger Clinical Assessment ⚡", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            // Bottom Disclaimer Card (Dismissible and Solid Surface to block dot background overlaps)
            if (showDisclaimer) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🛡️", fontSize = 20.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PharmaSense AI is an educational smart pharmacy information companion. Always consult clinical practitioners for personal treatment plans.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp
                            )
                        }
                        IconButton(
                            onClick = { showDisclaimer = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        } // Close Column
    } // Close else block

    // Interactive Detail Dialog for Home Tab Handbook click events
    selectedDrugForDetail?.let { drug ->
        AlertDialog(
            onDismissRequest = { selectedDrugForDetail = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${drug.number}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column {
                        Text(
                            text = drug.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Reference Monograph",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text("🔬 Clinical Indication / Uses", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(drug.uses, style = MaterialTheme.typography.bodyMedium)
                    }

                    Column {
                        Text("⚠️ Side Effects", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = DangerAlertRed)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(drug.sideEffects, style = MaterialTheme.typography.bodyMedium)
                    }

                    Column {
                        Text("💡 Safe Use Precautions", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(drug.precautions, style = MaterialTheme.typography.bodyMedium)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("💸 Cost Savings / Substitutes", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(drug.alternativeBrand, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedDrugForDetail = null
                        viewModel.currentTab.value = "AI Chat"
                        viewModel.chatInput.value = "Provide comprehensive information about ${drug.name}: indications, standard pediatric and adult dosing, mechanism of action, complete list of contraindications, and general patient counseling notes."
                    }
                ) {
                    Text("Ask Chat AI ⚡")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDrugForDetail = null }) {
                    Text("Close")
                }
            }
        )
    }
}

// --- 1.5 COMMON DRUGS QUICK GUIDE NAVIGATION VIEWS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesListView(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onCategorySelect: (InfographicCategory) -> Unit,
    onDrugSelect: (InfographicDrug) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Back Button
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "100 Drugs Guide",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Select a therapeutic class",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Search Bar inside Infographic
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth().testTag("drugs_guide_search"),
            placeholder = { Text("Search 100 drugs, uses, or side effects...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Title Plate: Styled like the cute cloud-ribbon banner in screenshot
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                        )
                    ),
                    RoundedCornerShape(24.dp)
                )
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "☁️ 10 THERAPEUTIC CLASSES ☁️",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "10 Essential Drugs per Class • Monograph Reference",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (searchQuery.isBlank()) {
            // Draw Category Cards as a list
            CommonDrugsData.categories.forEach { category ->
                val colorVal = Color(android.graphics.Color.parseColor(category.accentColorHex))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCategorySelect(category) }
                        .border(
                            BorderStroke(1.5.dp, colorVal.copy(alpha = 0.35f)),
                            RoundedCornerShape(16.dp)
                ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorVal.copy(alpha = 0.04f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Colored Index Circle
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(colorVal, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${category.index}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = category.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "10 essential compounds • Monograph reference",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Open",
                            tint = colorVal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        } else {
            // Direct search list across all categories
            val matchingDrugs = remember(searchQuery) {
                CommonDrugsData.categories.flatMap { cat ->
                    cat.drugs.map { drug -> Pair(cat, drug) }
                }.filter { (_, drug) ->
                    drug.name.contains(searchQuery, ignoreCase = true) ||
                    drug.uses.contains(searchQuery, ignoreCase = true) ||
                    drug.sideEffects.contains(searchQuery, ignoreCase = true)
                }
            }

            if (matchingDrugs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No drugs match your query.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                matchingDrugs.forEach { (category, drug) ->
                    val colorVal = Color(android.graphics.Color.parseColor(category.accentColorHex))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, colorVal.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .clickable { onDrugSelect(drug) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(colorVal.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${drug.number}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Black,
                                color = colorVal
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = drug.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "(${category.title.substringAfter(" ").substringBefore(" (")})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorVal,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Uses: ${drug.uses}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Side-Effects: ${drug.sideEffects}",
                                style = MaterialTheme.typography.labelSmall,
                                color = DangerAlertRed.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Quick Tips Section (styled exactly like screenshot)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.5.dp, Color(0xFFE0C068)), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF5)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Quick Tips",
                        tint = Color(0xFFD4AF37),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "CLINICAL RESEARCH NOTES",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF8B7355),
                        letterSpacing = 1.sp
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Always identify active ingredients & exact salt concentrations.",
                        "Evaluate clinical indications against professional textbooks.",
                        "Note critical side-effects & contraindications.",
                        "Document cost-saving generic substitutions for project analysis.",
                        "Cross-verify molecular drug interactions on-device."
                    ).forEach { tip ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "•",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8B7355)
                            )
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5D4037),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailView(
    category: InfographicCategory,
    onBackClick: () -> Unit,
    onDrugSelect: (InfographicDrug) -> Unit
) {
    val colorVal = Color(android.graphics.Color.parseColor(category.accentColorHex))
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Back Button
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = colorVal
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.title.substringAfter(" "),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = colorVal
                )
                Text(
                    text = "Therapeutic Class ${category.index} of 10",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // mini banner for category
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorVal.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                .border(1.5.dp, colorVal.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(colorVal, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${category.index}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "CLINICAL RESEARCH COMPLIANCE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = colorVal
                    )
                }
                Text(
                    text = "Contains 10 standard medications, therapeutic applications, primary side-effects, and cost-saving generic alternatives.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // List of 10 Drugs in Category
        category.drugs.forEach { drug ->
            ExpandableDrugCard(
                drug = drug,
                colorVal = colorVal,
                onDrugSelect = onDrugSelect
            )
        }
    }
}

@Composable
fun ExpandableDrugCard(
    drug: InfographicDrug,
    colorVal: Color,
    onDrugSelect: (InfographicDrug) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { isExpanded = !isExpanded }
            .border(
                width = if (isExpanded) 1.5.dp else 1.dp,
                color = if (isExpanded) colorVal else colorVal.copy(alpha = 0.25f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) colorVal.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Rounded Index bubble
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(colorVal.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${drug.number}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Black,
                        color = colorVal
                    )
                }

                // Drug and basic info
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = drug.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = colorVal.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (!isExpanded) {
                        Text(
                            text = "Uses: ${drug.uses}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = colorVal.copy(alpha = 0.15f))
                    
                    // Uses Section
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🔬", fontSize = 14.sp)
                            Text(
                                "Clinical Uses:", 
                                style = MaterialTheme.typography.labelSmall, 
                                fontWeight = FontWeight.Bold, 
                                color = colorVal
                            )
                        }
                        Text(
                            text = drug.uses, 
                            style = MaterialTheme.typography.bodySmall, 
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 22.dp, top = 2.dp)
                        )
                    }

                    // Side Effects Section
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("⚠️", fontSize = 14.sp)
                            Text(
                                "Side-Effects:", 
                                style = MaterialTheme.typography.labelSmall, 
                                fontWeight = FontWeight.Bold, 
                                color = DangerAlertRed
                            )
                        }
                        Text(
                            text = drug.sideEffects, 
                            style = MaterialTheme.typography.bodySmall, 
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 22.dp, top = 2.dp)
                        )
                    }

                    // Precautions Section
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("💡", fontSize = 14.sp)
                            Text(
                                "Safe Use Precautions:", 
                                style = MaterialTheme.typography.labelSmall, 
                                fontWeight = FontWeight.Bold, 
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Text(
                            text = drug.precautions, 
                            style = MaterialTheme.typography.bodySmall, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 22.dp, top = 2.dp)
                        )
                    }

                    // Branded Generic / Common Substitutes Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("💸", fontSize = 14.sp)
                                Text(
                                    "Common Indian Brands & Savings:", 
                                    style = MaterialTheme.typography.labelSmall, 
                                    fontWeight = FontWeight.Black, 
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = drug.alternativeBrand, 
                                style = MaterialTheme.typography.bodySmall, 
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(start = 22.dp)
                            )
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onDrugSelect(drug) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, colorVal)
                        ) {
                            Text("Full Monograph ➡️", color = colorVal, fontSize = 11.sp, maxLines = 1)
                        }

                        Button(
                            onClick = { onDrugSelect(drug) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorVal)
                        ) {
                            Text("Consult AI ✨", fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

// --- 1.5 COMMON DRUGS QUICK GUIDE VIEW ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonDrugsQuickGuideView(viewModel: PharmaViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDrugForDetail by remember { mutableStateOf<InfographicDrug?>(null) }

    val filteredCategories = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            CommonDrugsData.categories
        } else {
            CommonDrugsData.categories.map { category ->
                category.copy(
                    drugs = category.drugs.filter { drug ->
                        drug.name.contains(searchQuery, ignoreCase = true) ||
                        drug.uses.contains(searchQuery, ignoreCase = true) ||
                        drug.sideEffects.contains(searchQuery, ignoreCase = true)
                    }
                )
            }.filter { it.drugs.isNotEmpty() }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Bar inside Infographic
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().testTag("drugs_guide_search"),
            placeholder = { Text("Search 100 drugs, uses, or side effects...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Title Plate: Styled like the cute cloud-ribbon banner in screenshot
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                        )
                    ),
                    RoundedCornerShape(24.dp)
                )
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "☁️ 100 COMMON DRUGS ☁️",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "⚡ & THEIR USES & SIDE EFFECTS ⚡",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 0.5.sp
                )
                HorizontalDivider(
                    modifier = Modifier.width(100.dp).padding(vertical = 4.dp),
                    thickness = 1.5.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
                Text(
                    text = "Interactive Research Infographic • Quick Practice Guide",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Draw Category Cards
        filteredCategories.forEach { category ->
            val colorVal = Color(android.graphics.Color.parseColor(category.accentColorHex))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.5.dp, colorVal.copy(alpha = 0.4f)),
                        RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorVal.copy(alpha = 0.02f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Category Title Block
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorVal.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorVal
                        )
                        Box(
                            modifier = Modifier
                                .background(colorVal, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "N = ${category.drugs.size}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // List of Drugs in Category
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        category.drugs.forEach { drug ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                    .clickable { selectedDrugForDetail = drug }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Rounded Index bubble
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(colorVal.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${drug.number}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Black,
                                        color = colorVal
                                    )
                                }

                                // Drug and uses column
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = drug.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Details",
                                            tint = colorVal.copy(alpha = 0.6f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Uses: ${drug.uses}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(1.dp))
                                    Text(
                                        text = "Side-Effects: ${drug.sideEffects}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DangerAlertRed.copy(alpha = 0.85f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Tips Section (styled exactly like screenshot)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.5.dp, Color(0xFFE0C068)), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF5)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Quick Tips",
                        tint = Color(0xFFD4AF37),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "QUICK TIPS",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF8B7355),
                        letterSpacing = 1.sp
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Use medicines only as prescribed by doctor.",
                        "Complete the full course of antibiotics.",
                        "Do not share medicines with others.",
                        "Report any side effects to your doctor.",
                        "Store medicines in a cool, dry place."
                    ).forEach { tip ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "•",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8B7355)
                            )
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5D4037),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    // Interactive Detail Dialog
    selectedDrugForDetail?.let { drug ->
        AlertDialog(
            onDismissRequest = { selectedDrugForDetail = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${drug.number}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column {
                        Text(
                            text = drug.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Reference Monograph",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text("🔬 Clinical Indication / Uses", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(drug.uses, style = MaterialTheme.typography.bodyMedium)
                    }

                    Column {
                        Text("⚠️ Side Effects", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = DangerAlertRed)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(drug.sideEffects, style = MaterialTheme.typography.bodyMedium)
                    }

                    Column {
                        Text("💡 Safe Use Precautions", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(drug.precautions, style = MaterialTheme.typography.bodyMedium)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("💸 Cost Savings / Substitutes", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(drug.alternativeBrand, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedDrugForDetail = null
                        viewModel.currentTab.value = "AI Chat"
                        viewModel.chatInput.value = "Provide comprehensive information about ${drug.name}: indications, standard pediatric and adult dosing, mechanism of action, complete list of contraindications, and general patient counseling notes."
                    }
                ) {
                    Text("Ask Chat AI ⚡")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDrugForDetail = null }) {
                    Text("Close")
                }
            }
        )
    }
}

// --- 2. SEARCH / MEDICAL CATALOG TAB ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTabContent(viewModel: PharmaViewModel, onSettingsClick: () -> Unit) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedDisease by viewModel.selectedDisease.collectAsStateWithLifecycle()
    val selectedDrug by viewModel.selectedDrug.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    val filteredDiseases = remember(query) {
        if (query.trim().isEmpty()) {
            MedicalCatalog.diseases
        } else {
            MedicalCatalog.diseases.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                it.overview.contains(query, ignoreCase = true)
            }
        }
    }

    val filteredDrugs = remember(query) {
        if (query.trim().isEmpty()) {
            MedicalCatalog.drugs
        } else {
            MedicalCatalog.drugs.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.drugClass.contains(query, ignoreCase = true) ||
                it.uses.any { use -> use.contains(query, ignoreCase = true) }
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = if (selectedDisease != null) "Disease Profile" 
                                   else if (selectedDrug != null) "Drug Profile" 
                                   else "Medical Information Catalog",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        if (selectedDisease != null || selectedDrug != null) {
                            IconButton(onClick = {
                                viewModel.selectedDisease.value = null
                                viewModel.selectedDrug.value = null
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = onSettingsClick, modifier = Modifier.testTag("settings_button")) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
                
                if (selectedDisease == null && selectedDrug == null) {
                    TextField(
                        value = query,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Search Diabetes, Metformin, Asthma...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("search_input"),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                selectedDisease != null -> {
                    DiseaseProfileView(disease = selectedDisease!!, viewModel = viewModel)
                }
                selectedDrug != null -> {
                    DrugProfileView(drug = selectedDrug!!, viewModel = viewModel)
                }
                else -> {
                    // Search catalog main list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        // Scan Box Banner Invitation
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // Go to scan simulator in reports
                                            viewModel.currentTab.value = "Reports"
                                        }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Medicine Label Scanner", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                            Text("Scan medication sheets to extract active values using Gemini.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                                        }
                                    }
                                    Icon(
                                        Icons.Default.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        // Sections of results
                        if (filteredDiseases.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Diseases & Conditions (${filteredDiseases.size})", icon = Icons.Default.Info)
                            }
                            items(filteredDiseases) { disease ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectedDisease.value = disease
                                            focusManager.clearFocus()
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = disease.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text(disease.category, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                                colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = disease.overview,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        if (filteredDrugs.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Medications & Drugs (${filteredDrugs.size})", icon = Icons.Default.Medication)
                            }
                            items(filteredDrugs) { drug ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectedDrug.value = drug
                                            focusManager.clearFocus()
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = drug.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = drug.drugClass.split(" ").first(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Mechanism: " + drug.mechanism,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        if (filteredDiseases.isEmpty() && filteredDrugs.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No matching items found.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiseaseProfileView(disease: Disease, viewModel: PharmaViewModel) {
    val scrollState = rememberScrollState()
    val bookmarks by viewModel.bookmarkedList.collectAsStateWithLifecycle()
    val isBookmarked by viewModel.isBookmarkedFlow(disease.id).collectAsState(initial = false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Disease Profile Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(disease.category, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = {
                        viewModel.toggleBookmark("disease", disease.id, disease.name, disease.category)
                    }) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Text(disease.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(disease.overview, style = MaterialTheme.typography.bodyMedium, lineHeight = 21.sp)
            }
        }

        // Section: Symptoms
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(title = "Common Symptoms", icon = Icons.Default.Coronavirus)
                disease.symptoms.forEach { symptom ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(symptom, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Section: Causes
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(title = "Primary Causes & Risks", icon = Icons.Default.Warning)
                disease.causes.forEach { cause ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("•", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(cause, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Section: Lifestyle Tips (with horizontal cards containing emojis matching the infographic)
        SectionHeader(title = "Lifestyle & Preventive Tips", icon = Icons.Default.SelfImprovement)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            disease.tips.forEach { tip ->
                val emoji = when {
                    tip.contains("Eat", ignoreCase = true) -> "🥗"
                    tip.contains("Exercise", ignoreCase = true) -> "🏃"
                    tip.contains("Monitor", ignoreCase = true) -> "🩸"
                    tip.contains("Hydrated", ignoreCase = true) || tip.contains("water", ignoreCase = true) -> "💧"
                    else -> "🧘"
                }
                Card(
                    modifier = Modifier.width(160.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(emoji, fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Section: Commonly used drugs
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(title = "Pharmacological Classes & Medicines", icon = Icons.Default.LocalPharmacy)
                Text(
                    text = "The following classes are typical. Click a medication to read details:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                disease.drugClasses.forEach { (clazz, drugs) ->
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Text(clazz, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        
                        drugs.split(",").forEach { drugName ->
                            val trimmedDrug = drugName.trim().replace("(Tamiflu)", "")
                            val matchingDrugObject = remember {
                                MedicalCatalog.drugs.firstOrNull { it.name.contains(trimmedDrug, ignoreCase = true) }
                            }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = matchingDrugObject != null) {
                                        viewModel.selectedDrug.value = matchingDrugObject
                                        viewModel.selectedDisease.value = null
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "  → $drugName",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (matchingDrugObject != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (matchingDrugObject != null) {
                                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrugProfileView(drug: Drug, viewModel: PharmaViewModel) {
    val scrollState = rememberScrollState()
    val isBookmarked by viewModel.isBookmarkedFlow(drug.id).collectAsState(initial = false)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(drug.drugClass, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    IconButton(onClick = {
                        viewModel.toggleBookmark("drug", drug.id, drug.name, drug.drugClass)
                    }) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Text(drug.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            }
        }

        // Clinical indication / Uses
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(title = "Approved Clinical Indications", icon = Icons.Default.CheckCircleOutline, color = MaterialTheme.colorScheme.secondary)
                drug.uses.forEach { use ->
                    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                        Text("•", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(use, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Mechanism of Action
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(title = "Mechanism of Action", icon = Icons.Default.Settings, color = MaterialTheme.colorScheme.secondary)
                Text(drug.mechanism, style = MaterialTheme.typography.bodyMedium, lineHeight = 21.sp)
            }
        }

        // Side effects
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(title = "Common Associated Side Effects", icon = Icons.Default.Sick, color = MaterialTheme.colorScheme.secondary)
                drug.sideEffects.forEach { side ->
                    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.error, CircleShape))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(side, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Dose (Adults)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(title = "Standard Dose (Adults)", icon = Icons.Default.Scale, color = MaterialTheme.colorScheme.secondary)
                Text(drug.dose, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }
        }

        // Contraindications (Alert styled)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SectionHeader(title = "Strict Contraindications & Warnings", icon = Icons.Default.DoNotDisturb, color = MaterialTheme.colorScheme.error)
                drug.contraindications.forEach { contra ->
                    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Error, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(contra, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }
    }
}

// --- 3. REPORTS TAB (MEDICAL REPORT ANALYZER & SYMPTOM EVAL & SCANNER) ---

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReportsTabContent(viewModel: PharmaViewModel, onSettingsClick: () -> Unit) {
    var selectedToolSubTab by remember { mutableStateOf("Report") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Clinical Diagnostic Suite",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        TabRow(
            selectedTabIndex = if (selectedToolSubTab == "Report") 0 else if (selectedToolSubTab == "Symptom") 1 else 2,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(selected = selectedToolSubTab == "Report", onClick = { selectedToolSubTab = "Report" }) {
                Text("Analyze Reports", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedToolSubTab == "Symptom", onClick = { selectedToolSubTab = "Symptom" }) {
                Text("Symptom Checker", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedToolSubTab == "Scanner", onClick = { selectedToolSubTab = "Scanner" }) {
                Text("Label Scanner", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold)
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedToolSubTab) {
                "Report" -> ReportAnalyzerPage(viewModel)
                "Symptom" -> SymptomCheckerPage(viewModel)
                "Scanner" -> LabelScannerSimulatorPage(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportAnalyzerPage(viewModel: PharmaViewModel) {
    val repoLiveList by viewModel.reportsList.collectAsStateWithLifecycle()
    val loading by viewModel.reportAnalysisLoading.collectAsStateWithLifecycle()
    val activeDetail by viewModel.activeDetailedReport.collectAsStateWithLifecycle()

    val hgb by viewModel.hemoglobinInput.collectAsStateWithLifecycle()
    val wbc by viewModel.wbcInput.collectAsStateWithLifecycle()
    val platelets by viewModel.plateletInput.collectAsStateWithLifecycle()
    val glucose by viewModel.glucoseInput.collectAsStateWithLifecycle()
    val bpSys by viewModel.bpSystolicInput.collectAsStateWithLifecycle()
    val bpDia by viewModel.bpDiastolicInput.collectAsStateWithLifecycle()
    val title by viewModel.reportTitleInput.collectAsStateWithLifecycle()

    if (activeDetail != null) {
        // Show detail of saved report analysis worksheet
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.activeDetailedReport.value = null }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text("Worksheet Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = { viewModel.deleteReport(activeDetail!!.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(activeDetail!!.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Recorded Input Numbers: " + activeDetail!!.labData,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Risk Level: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text(
                            text = activeDetail!!.riskLevel,
                            color = when (activeDetail!!.riskLevel) {
                                "High" -> DangerAlertRed
                                "Moderate" -> WarningOrange
                                else -> SuccessGreen
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = "Identified Markers: " + activeDetail!!.possibleIndication,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(title = "Gemini Clinical Insights Analysis", icon = Icons.Default.Insights)
                    MarkdownText(text = activeDetail!!.analysisMarkdown)
                }
            }
        }
    } else {
        // Show Form + History
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Decipher Medical Lab Sheets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        Text("Upload a lab report or doctor's prescription photo, or enter readings manually. PharmaSense AI calculates range deviations, explains findings, and suggests cheaper branded generic medicine alternatives.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 1.dp)

                        // --- Dynamic Image/Photo Upload Option ---
                        val context = LocalContext.current
                        val imageUriString by viewModel.reportImageUriString.collectAsStateWithLifecycle()
                        val launcher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.GetContent()
                        ) { uri: Uri? ->
                            if (uri != null) {
                                try {
                                    val inputStream = context.contentResolver.openInputStream(uri)
                                    val bytes = inputStream?.readBytes()
                                    inputStream?.close()
                                    if (bytes != null) {
                                        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                                        viewModel.reportImageUriString.value = uri.toString()
                                        viewModel.reportImageBase64.value = base64
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        if (imageUriString != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                AsyncImage(
                                    model = imageUriString,
                                    contentDescription = "Selected Report",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                IconButton(
                                    onClick = {
                                        viewModel.reportImageUriString.value = null
                                        viewModel.reportImageBase64.value = null
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                                }
                            }

                            Text(
                                text = "✨ Prescription/Report Photo Selected! Gemini will scan the photo, list possible medicines, and match them with cheaper, high-quality alternatives from top pharma brands.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Card(
                                onClick = { launcher.launch("image/*") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Upload Photo",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Upload Prescription / Lab Report Photo",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Scan doctor prescriptions or lab sheets to find cost-saving medicine brands",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        if (imageUriString == null) {
                            Text(
                                text = "— OR ENTER MEASUREMENTS MANUALLY —",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = title,
                                onValueChange = { viewModel.reportTitleInput.value = it },
                                label = { Text("Report Title") },
                                modifier = Modifier.fillMaxWidth().testTag("rep_title_input"),
                                singleLine = true
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = hgb,
                                    onValueChange = { viewModel.hemoglobinInput.value = it },
                                    label = { Text("Hemoglobin (g/dL)") },
                                    modifier = Modifier.weight(1f).testTag("rep_hgb_input"),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = wbc,
                                    onValueChange = { viewModel.wbcInput.value = it },
                                    label = { Text("WBC Count (cells)") },
                                    modifier = Modifier.weight(1f).testTag("rep_wbc_input"),
                                    singleLine = true
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = platelets,
                                    onValueChange = { viewModel.plateletInput.value = it },
                                    label = { Text("Platelets (count)") },
                                    modifier = Modifier.weight(1f).testTag("rep_plt_input"),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = glucose,
                                    onValueChange = { viewModel.glucoseInput.value = it },
                                    label = { Text("Blood Glucose (mg/dL)") },
                                    modifier = Modifier.weight(1f).testTag("rep_glu_input"),
                                    singleLine = true
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = bpSys,
                                    onValueChange = { viewModel.bpSystolicInput.value = it },
                                    label = { Text("BP Systolic (mmHg)") },
                                    modifier = Modifier.weight(1f).testTag("rep_bps_input"),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = bpDia,
                                    onValueChange = { viewModel.bpDiastolicInput.value = it },
                                    label = { Text("BP Diastolic (mmHg)") },
                                    modifier = Modifier.weight(1f).testTag("rep_bpd_input"),
                                    singleLine = true
                                )
                            }
                        } else {
                            // If an image is uploaded, we still allow title customization
                            OutlinedTextField(
                                value = title,
                                onValueChange = { viewModel.reportTitleInput.value = it },
                                label = { Text("Report Title / Name") },
                                modifier = Modifier.fillMaxWidth().testTag("rep_title_input"),
                                singleLine = true
                            )
                        }

                        if (loading) {
                            Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (imageUriString != null) "Gemini is scanning photo & matching cheaper alternatives... (Up to 15s)" else "Gemini is analyzing counts... (Up to 15s)",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.analyzeLabReport() },
                                modifier = Modifier.fillMaxWidth().testTag("rep_submit_btn"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Insights, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (imageUriString != null) "Scan Photo & Find Cheap Alternatives" else "Analyze and Save Lab Report",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // History sections
            item {
                SectionHeader(title = "Saved Reports History (${repoLiveList.size})", icon = Icons.Outlined.History)
            }

            if (repoLiveList.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No previously analyzed medical sheets. Enter measurements above.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(repoLiveList) { report ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.activeDetailedReport.value = report },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(report.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(report.labData, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Markers: " + report.possibleIndication,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (report.riskLevel) {
                                            "High" -> DangerAlertRed.copy(alpha = 0.12f)
                                            "Moderate" -> WarningOrange.copy(alpha = 0.12f)
                                            else -> SuccessGreen.copy(alpha = 0.12f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = report.riskLevel,
                                    color = when (report.riskLevel) {
                                        "High" -> DangerAlertRed
                                        "Moderate" -> WarningOrange
                                        else -> SuccessGreen
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SymptomCheckerPage(viewModel: PharmaViewModel) {
    val query by viewModel.symptomInput.collectAsStateWithLifecycle()
    val res by viewModel.symptomAnalysisResult.collectAsStateWithLifecycle()
    val loading by viewModel.symptomAnalysisLoading.collectAsStateWithLifecycle()
    val isEmergency by viewModel.symptomIsEmergency.collectAsStateWithLifecycle()

    val suggestions = listOf("Fever", "Dry Cough", "Wheezing", "Heartburn", "High Blood Sugar", "Severe Chest Pain")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Triage Symptom Checker", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Type any symptoms you are experiencing. The checker will perform immediate red-flags safety checks, then compile logical causes.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.symptomInput.value = it },
                    placeholder = { Text("E.g. Fever, coughing and headaches...") },
                    modifier = Modifier.fillMaxWidth().testTag("symptom_input"),
                    minLines = 2,
                    maxLines = 4
                )

                // Suggestion chips
                Text("Quick Selection Chips:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    suggestions.forEach { sug ->
                        SuggestionChip(
                            onClick = { viewModel.symptomInput.value = sug },
                            label = { Text(sug) }
                        )
                    }
                }

                if (loading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Gemini is examining symptoms...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.analyzeSymptoms() },
                            modifier = Modifier.weight(1f).testTag("symptom_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Check Symptoms", fontWeight = FontWeight.Bold)
                        }
                        
                        if (res != null || query.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { viewModel.clearSymptomState() },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Clear")
                            }
                        }
                    }
                }
            }
        }

        if (res != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = if (isEmergency) DangerAlertRed else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isEmergency) DangerAlertRed.copy(alpha = 0.04f) 
                                     else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isEmergency) Icons.Default.Dangerous else Icons.Default.FactCheck,
                            contentDescription = null,
                            tint = if (isEmergency) DangerAlertRed else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (isEmergency) "CRITICAL WARNING" else "Triage Triage Indications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isEmergency) DangerAlertRed else MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    MarkdownText(text = res!!, color = if (isEmergency) DangerAlertRed else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun LabelScannerSimulatorPage(viewModel: PharmaViewModel) {
    val result by viewModel.labelScannerResult.collectAsStateWithLifecycle()
    val loading by viewModel.labelScannerLoading.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Gemini Scanner Simulator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Simulates choosing/analyzing medicine label strips using the multi-modal Gemini REST API pipeline.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Text("Select a Medicine Container to Scan:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                // 3 Mock Medicine Containers to Scan
                listOf(
                    "metformin" to "Metformin 500mg Tablets Strip",
                    "ciprofloxacin" to "Ciprofloxacin 250mg USP Packs",
                    "omeprazole" to "Omeprazole Acid Suppressant delayed release bottle"
                ).forEach { (key, label) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.simulateLabelScan(key) }
                            .border(1.dp, MaterialTheme.colorScheme.primaryHex.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (key == "metformin") "💊" else if (key == "ciprofloxacin") "🧪" else "🍼", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                if (loading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Gemini is reading medical packaging...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        if (result != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LibraryAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan Analysis Sheet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { viewModel.resetScanner() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    MarkdownText(text = result!!)
                }
            }
        }
    }
}

// Color compatibility mapping
val ColorScheme.primaryHex: Color get() = this.primary

// --- 4. INTERACTIONS TAB ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractionsTabContent(viewModel: PharmaViewModel, onSettingsClick: () -> Unit) {
    val d1 by viewModel.interactionDrug1.collectAsStateWithLifecycle()
    val d2 by viewModel.interactionDrug2.collectAsStateWithLifecycle()
    val risk by viewModel.interactionResultRisk.collectAsStateWithLifecycle()
    val outcome by viewModel.interactionResultText.collectAsStateWithLifecycle()
    val loading by viewModel.interactionLoading.collectAsStateWithLifecycle()

    val availableMeds = MedicalCatalog.drugs.map { it.name }

    var expanded1 by remember { mutableStateOf(false) }
    var expanded2 by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dashboard Header with Settings Button
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Interactions Matrix",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Analyze molecular matches & overlapping chemical side effects",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Drug Interaction Analyzer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Select multiple medications from your regimen to analyze potential conflicts, absorption blocks, or toxic synergisms.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                // Select Med 1
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = expanded1,
                        onExpandedChange = { expanded1 = !expanded1 }
                    ) {
                        OutlinedTextField(
                            value = d1,
                            onValueChange = { viewModel.interactionDrug1.value = it },
                            label = { Text("First Medication") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded1) },
                            modifier = Modifier.fillMaxWidth().menuAnchor().testTag("int_drug1_input"),
                            readOnly = false
                        )
                        ExposedDropdownMenu(
                            expanded = expanded1,
                            onDismissRequest = { expanded1 = false }
                        ) {
                            availableMeds.forEach { med ->
                                DropdownMenuItem(
                                    text = { Text(med) },
                                    onClick = {
                                        viewModel.interactionDrug1.value = med
                                        expanded1 = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Select Med 2
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = expanded2,
                        onExpandedChange = { expanded2 = !expanded2 }
                    ) {
                        OutlinedTextField(
                            value = d2,
                            onValueChange = { viewModel.interactionDrug2.value = it },
                            label = { Text("Second Medication") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded2) },
                            modifier = Modifier.fillMaxWidth().menuAnchor().testTag("int_drug2_input"),
                            readOnly = false
                        )
                        ExposedDropdownMenu(
                            expanded = expanded2,
                            onDismissRequest = { expanded2 = false }
                        ) {
                            availableMeds.forEach { med ->
                                DropdownMenuItem(
                                    text = { Text(med) },
                                    onClick = {
                                        viewModel.interactionDrug2.value = med
                                        expanded2 = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (loading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Gemini is exploring biochemistry lattices...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.checkDrugInteractions() },
                            modifier = Modifier.weight(1f).testTag("int_submit_btn"),
                            shape = RoundedCornerShape(8.dp),
                            enabled = d1.isNotEmpty() && d2.isNotEmpty()
                        ) {
                            Text("Evaluate Interactions", fontWeight = FontWeight.Bold)
                        }
                        
                        if (risk != null || d1.isNotEmpty() || d2.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { viewModel.resetInteractions() },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Reset")
                            }
                        }
                    }
                }
            }
        }

        if (risk != null && outcome != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = when (risk) {
                            "Severe" -> DangerAlertRed
                            "Moderate" -> WarningOrange
                            "Mild" -> MaterialTheme.colorScheme.secondary
                            else -> SuccessGreen
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = when (risk) {
                        "Severe" -> DangerAlertRed.copy(alpha = 0.03f)
                        "Moderate" -> WarningOrange.copy(alpha = 0.03f)
                        "Mild" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.03f)
                        else -> SuccessGreen.copy(alpha = 0.03f)
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = when (risk) {
                                "Severe" -> Icons.Default.ReportProblem
                                "Moderate" -> Icons.Default.Warning
                                "Mild" -> Icons.Default.Info
                                else -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = when (risk) {
                                "Severe" -> DangerAlertRed
                                "Moderate" -> WarningOrange
                                "Mild" -> MaterialTheme.colorScheme.secondary
                                else -> SuccessGreen
                            }
                        )
                        Text(
                            text = "Result: $risk Interaction",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = when (risk) {
                                "Severe" -> DangerAlertRed
                                "Moderate" -> WarningOrange
                                "Mild" -> MaterialTheme.colorScheme.secondary
                                else -> SuccessGreen
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    MarkdownText(text = outcome!!, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

// --- 5. AI PHARMACY CHAT TAB ---

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatTabContent(viewModel: PharmaViewModel, onSettingsClick: () -> Unit) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val input by viewModel.chatInput.collectAsStateWithLifecycle()
    val loading by viewModel.chatLoading.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Scroll to bottom when messages load/update
    LaunchedEffect(messages.size, loading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat Header with summary metrics
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("PharmaSense Smart Chat", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Ask about chemical formulas, usage limits, or care", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (messages.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearChatHistory() }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    IconButton(onClick = onSettingsClick, modifier = Modifier.testTag("settings_button")) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Message Thread Board
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("👋", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Hello! I am PharmaSense AI.",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ask me anything about prescription doses, active elements, side effects, or simple care advice below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                        )
                        
                        // Default query suggestions cards
                        Spacer(modifier = Modifier.height(12.dp))
                        listOf(
                            "Is Metformin safe if pregnant?",
                            "Tell me side effects of Ciprofloxacin.",
                            "Explain Asthma rescue vs maintenance inhalers."
                        ).forEach { q ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .clickable {
                                        viewModel.chatInput.value = q
                                        viewModel.sendChatMessage()
                                    }
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                PaddingRowInsideChat(q)
                            }
                        }
                    }
                }
            } else {
                items(messages) { message ->
                    val isUser = message.role == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(0.85f),
                            shape = RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                                bottomStart = if (isUser) 12.dp else 0.dp,
                                bottomEnd = if (isUser) 0.dp else 12.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) MaterialTheme.colorScheme.primary 
                                                 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isUser) "You" else "PharmaSense AI",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (isUser) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (isUser) {
                                    Text(text = message.content, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                } else {
                                    MarkdownText(text = message.content)
                                }
                            }
                        }
                    }
                }
            }

            if (loading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(0.5f),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Text("AI is composing answer...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        // Bottom Input box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { viewModel.chatInput.value = it },
                placeholder = { Text("Ask PharmaSense Advisor...") },
                modifier = Modifier.weight(1f).testTag("chat_input"),
                singleLine = true,
                maxLines = 1,
                trailingIcon = {
                    if (input.isNotEmpty() && !loading) {
                        IconButton(onClick = { viewModel.sendChatMessage() }) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun PaddingRowInsideChat(q: String) {
    Row(
        modifier = Modifier.padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(q, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
    }
}

data class AvatarPreset(
    val id: Int,
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val gradient: List<Color>,
    val description: String,
    val emoji: String
)

val AVATAR_PRESETS = listOf(
    AvatarPreset(0, "Default Care", Icons.Default.Person, listOf(Color(0xFF6366F1), Color(0xFF4F46E5)), "Primary Medical Advisor", "🩺"),
    AvatarPreset(1, "Heart Guardian", Icons.Default.Favorite, listOf(Color(0xFFEC4899), Color(0xFFD946EF)), "Cardiovascular Health", "❤️"),
    AvatarPreset(2, "Mind Specialist", Icons.Default.Psychology, listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)), "Neurology & Mental Balance", "🧠"),
    AvatarPreset(3, "Clinical Pharmacist", Icons.Default.LocalHospital, listOf(Color(0xFF10B981), Color(0xFF059669)), "Pharmacology Expert", "💊"),
    AvatarPreset(4, "Preventative Shield", Icons.Default.Shield, listOf(Color(0xFFF59E0B), Color(0xFFD97706)), "Immune & Defense Safeguard", "🛡️"),
    AvatarPreset(5, "Wellness Coach", Icons.Default.SelfImprovement, listOf(Color(0xFF06B6D4), Color(0xFF0891B2)), "Holistic Health Guide", "🧘"),
    AvatarPreset(6, "Pediatric Care", Icons.Default.Face, listOf(Color(0xFFF43F5E), Color(0xFFE11D48)), "Family & Pediatric Health", "👶"),
    AvatarPreset(7, "Therapeutic Specialist", Icons.Default.Healing, listOf(Color(0xFF14B8A6), Color(0xFF0D9488)), "Recovery & Rehab Specialist", "🩹")
)

@Composable
fun ProfileAvatar(
    avatarIndex: Int,
    fullName: String,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    onClick: (() -> Unit)? = null
) {
    val preset = AVATAR_PRESETS.getOrElse(avatarIndex) { AVATAR_PRESETS[0] }
    val avatarModifier = modifier
        .size(size)
        .clip(CircleShape)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    
    Box(
        modifier = avatarModifier
            .background(Brush.linearGradient(preset.gradient)),
        contentAlignment = Alignment.Center
    ) {
        if (avatarIndex == 0) {
            val initials = fullName.trim().split("\\s+".toRegex())
                .mapNotNull { it.firstOrNull()?.toString()?.uppercase() }
                .take(2)
                .joinToString("")
            
            Text(
                text = initials.ifEmpty { "P" },
                color = Color.White,
                style = if (size > 60.dp) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black
            )
        } else {
            Icon(
                imageVector = preset.icon,
                contentDescription = preset.name,
                tint = Color.White,
                modifier = Modifier.size(size * 0.55f)
            )
        }
        
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(size * 0.35f)
                .background(Color.White, CircleShape)
                .padding(2.dp)
                .background(preset.gradient.last(), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = preset.emoji,
                fontSize = (size.value * 0.18f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarSelectionDialog(
    currentSelectedIndex: Int,
    fullName: String,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Choose Therapeutic DP",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Personalize your clinical health profile",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val chunks = AVATAR_PRESETS.chunked(2)
                chunks.forEach { rowPresets ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowPresets.forEach { preset ->
                            val index = preset.id
                            val isSelected = currentSelectedIndex == index
                            Card(
                                onClick = { onSelect(index) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    ProfileAvatar(
                                        avatarIndex = index,
                                        fullName = fullName,
                                        size = 48.dp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = preset.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Confirm", fontWeight = FontWeight.Bold)
            }
        }
    )
}

// --- PROFILE & AUTHENTICATION DASHBOARD TAB ---

@Composable
fun ProfileTabContent(viewModel: PharmaViewModel, onSettingsClick: () -> Unit) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    if (!isLoggedIn) {
        ProfileAuthScreen(viewModel, onSettingsClick)
    } else {
        ProfileDashboardScreen(viewModel, onSettingsClick)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAuthScreen(viewModel: PharmaViewModel, onSettingsClick: () -> Unit) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var age by remember { mutableStateOf(30f) }
    var gender by remember { mutableStateOf("Male") }
    var healthGoal by remember { mutableStateOf("Maintain Good Health") }
    
    var expandedGender by remember { mutableStateOf(false) }
    var expandedGoal by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onSettingsClick, modifier = Modifier.testTag("settings_button")) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
            }
        }
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isRegisterMode) "Create Your Profile" else "Access Your Health Profile",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = if (isRegisterMode) "Set up your medical dashboard, track expenses & receive customized diet & test recommendations." else "Sign in to access your disease history, health data, and personalized AI tips.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        if (isRegisterMode) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("auth_username_input"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (isRegisterMode) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("auth_fullname_input"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
            singleLine = true
        )

        if (isRegisterMode) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Age:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("${age.toInt()} Years", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = age,
                    onValueChange = { age = it },
                    valueRange = 1f..100f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Gender:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedGender,
                    onExpandedChange = { expandedGender = !expandedGender }
                ) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGender) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedGender,
                        onDismissRequest = { expandedGender = false }
                    ) {
                        listOf("Male", "Female", "Other").forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    gender = item
                                    expandedGender = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Health Goal:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedGoal,
                    onExpandedChange = { expandedGoal = !expandedGoal }
                ) {
                    OutlinedTextField(
                        value = healthGoal,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGoal) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedGoal,
                        onDismissRequest = { expandedGoal = false }
                    ) {
                        listOf("Maintain Good Health", "Manage Diabetes", "Control Hypertension", "Optimize Digestion", "Pain Relief Management").forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    healthGoal = item
                                    expandedGoal = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                errorMessage = null
                if (isRegisterMode) {
                    if (username.isBlank() || email.isBlank() || password.isBlank() || fullName.isBlank()) {
                        errorMessage = "Please fill all required fields!"
                    } else {
                        viewModel.registerNewUser(
                            usrName = username,
                            mail = email,
                            name = fullName,
                            pass = password,
                            userAge = age.toInt(),
                            userGender = gender,
                            goal = healthGoal,
                            onSuccess = {},
                            onError = { err -> errorMessage = err }
                        )
                    }
                } else {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please enter both email and password!"
                    } else {
                        viewModel.loginExistingUser(
                            mail = email,
                            pass = password,
                            onSuccess = {},
                            onError = { err -> errorMessage = err }
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("auth_submit_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (isRegisterMode) "Register & Generate Dashboard ⚡" else "Sign In Securely 🛡️",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { 
                isRegisterMode = !isRegisterMode 
                errorMessage = null
            }
        ) {
            Text(
                text = if (isRegisterMode) "Already have an account? Sign In" else "Don't have an account? Register Now",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDashboardScreen(viewModel: PharmaViewModel, onSettingsClick: () -> Unit) {
    val fullName by viewModel.fullName.collectAsStateWithLifecycle()
    val email by viewModel.email.collectAsStateWithLifecycle()
    val age by viewModel.age.collectAsStateWithLifecycle()
    val gender by viewModel.gender.collectAsStateWithLifecycle()
    val healthGoal by viewModel.healthGoal.collectAsStateWithLifecycle()

    val reports by viewModel.reportsList.collectAsStateWithLifecycle()
    val expenses by viewModel.expensesList.collectAsStateWithLifecycle()
    val userDiseases by viewModel.userDiseasesList.collectAsStateWithLifecycle()

    val testingRecommendations by viewModel.testingRecommendations.collectAsStateWithLifecycle()
    val personalDiet by viewModel.personalDiet.collectAsStateWithLifecycle()
    val discoveryNews by viewModel.customDiscoveryNews.collectAsStateWithLifecycle()
    val insightsLoading by viewModel.profileInsightsLoading.collectAsStateWithLifecycle()

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    val avatarIndex by viewModel.avatarIndex.collectAsStateWithLifecycle()
    var showAvatarSelector by remember { mutableStateOf(false) }

    if (showAvatarSelector) {
        AvatarSelectionDialog(
            currentSelectedIndex = avatarIndex,
            fullName = fullName,
            onDismiss = { showAvatarSelector = false },
            onSelect = { idx ->
                viewModel.updateAvatarIndex(idx)
                showAvatarSelector = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. USER PROFILE HEADER CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProfileAvatar(
                            avatarIndex = avatarIndex,
                            fullName = fullName,
                            size = 56.dp,
                            onClick = { showAvatarSelector = true }
                        )
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.clickable { showAvatarSelector = true }
                            ) {
                                Text(
                                    text = fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Avatar",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tap DP to personalize profile 🎨",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { showAvatarSelector = true }
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onSettingsClick,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                                .size(36.dp)
                                .testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Age", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$age Years", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Gender", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(gender, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Health Goal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(healthGoal, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 2. USER DISEASES / CONDITIONS SECTION ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🩺 Track Your Conditions:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Select your medical conditions to customize diet plans, diagnostic screenings, and scientific breakthroughs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val availableDiseases = MedicalCatalog.diseases
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    availableDiseases.forEach { disease ->
                        val isTracked = userDiseases.any { it.diseaseId == disease.id }
                        DiseaseSelectChip(
                            name = disease.name,
                            isSelected = isTracked,
                            onClick = { viewModel.toggleUserDisease(disease.id, disease.name) }
                        )
                    }
                }
            }
        }

        // --- 3. TOTAL MEDICINE EXPENSE TRACKER ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "📊 Medicine Expenses:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        val totalSpent = expenses.sumOf { it.cost }
                        Text(
                            text = "Total Spent: ₹${String.format("%.2f", totalSpent)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Button(
                        onClick = { showAddExpenseDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (expenses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No medicine expenses logged yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        expenses.take(5).forEach { expense ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = expense.drugName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Category: ${expense.category}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "₹${expense.cost}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    IconButton(
                                        onClick = { viewModel.deleteMedicineExpense(expense.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Expense",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                        if (expenses.size > 5) {
                            Text(
                                text = "+ ${expenses.size - 5} more transactions logged",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 4. MEDICAL REPORTS HISTORY SECTION ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📄 Lab Reports History:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (reports.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .clickable { viewModel.currentTab.value = "Reports" }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🧪", fontSize = 24.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("No reports submitted yet", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Click here to add your first lab report for dynamic health analysis.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    val lastReport = reports.first()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔴 Last Analysis: ${lastReport.title}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (lastReport.riskLevel) {
                                                "High" -> MaterialTheme.colorScheme.error
                                                "Moderate" -> Color(0xFFF2994A)
                                                else -> MaterialTheme.colorScheme.primary
                                            },
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = lastReport.riskLevel,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Indication: ${lastReport.possibleIndication}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Details: ${lastReport.labData}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Previous Records:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        reports.drop(1).take(3).forEach { previousReport ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.activeDetailedReport.value = previousReport
                                        viewModel.currentTab.value = "Reports"
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = previousReport.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = previousReport.possibleIndication,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when (previousReport.riskLevel) {
                                                "High" -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                                "Moderate" -> Color(0xFFF2994A).copy(alpha = 0.1f)
                                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            },
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = previousReport.riskLevel,
                                        color = when (previousReport.riskLevel) {
                                            "High" -> MaterialTheme.colorScheme.error
                                            "Moderate" -> Color(0xFFF2994A)
                                            else -> MaterialTheme.colorScheme.primary
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 5. PERSONALIZED MEDICAL RECOGNITIONS SECTION (GEMINI POWERED) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ Personalized AI Insights:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = { viewModel.generateProfileInsights() },
                        enabled = !insightsLoading,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Insights",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (insightsLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Gemini is personalizing your diagnostic tests, custom diet plan & recent health breakthrough news...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                } else {
                    var selectedSubTab by remember { mutableStateOf(0) }

                    TabRow(
                        selectedTabIndex = selectedSubTab,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = selectedSubTab == 0,
                            onClick = { selectedSubTab = 0 },
                            text = { Text("Diagnostics", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedSubTab == 1,
                            onClick = { selectedSubTab = 1 },
                            text = { Text("Diet Plan", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedSubTab == 2,
                            onClick = { selectedSubTab = 2 },
                            text = { Text("Health News", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val activeContent = when (selectedSubTab) {
                        0 -> testingRecommendations ?: "Please tap the refresh icon above to trigger preventative testing guidelines customized to your profile."
                        1 -> personalDiet ?: "Please tap the refresh icon above to design a customized meal nutrition guide based on your disease profile."
                        else -> discoveryNews ?: "Please tap the refresh icon above to generate health news flashes relevant to your conditions."
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = when (selectedSubTab) {
                                        0 -> "🩺 Preventative Screenings"
                                        1 -> "🥗 Personalized Diet Plan"
                                        else -> "🔬 Clinical News & Advancements"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = activeContent,
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddExpenseDialog) {
        var drugName by remember { mutableStateOf("") }
        var costInput by remember { mutableStateOf("") }
        var categoryInput by remember { mutableStateOf("Allergies & Pain") }
        var expandedCat by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            title = { Text("Log Medicine Purchase") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = drugName,
                        onValueChange = { drugName = it },
                        label = { Text("Medicine Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = costInput,
                        onValueChange = { costInput = it },
                        label = { Text("Cost (INR / ₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Category:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        ExposedDropdownMenuBox(
                            expanded = expandedCat,
                            onExpandedChange = { expandedCat = !expandedCat }
                        ) {
                            OutlinedTextField(
                                value = categoryInput,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCat) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedCat,
                                onDismissRequest = { expandedCat = false }
                            ) {
                                listOf("Allergies & Pain", "Cardiology", "Endocrinology", "Gastrology", "Infections & Fungus", "Neurology", "Other").forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item) },
                                        onClick = {
                                            categoryInput = item
                                            expandedCat = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cost = costInput.toDoubleOrNull() ?: 0.0
                        if (drugName.isNotBlank() && cost > 0.0) {
                            viewModel.addMedicineExpense(drugName, cost, categoryInput)
                            showAddExpenseDialog = false
                        }
                    }
                ) {
                    Text("Save Expense")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpenseDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DiseaseSelectChip(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.padding(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BulletItem(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text("•", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 6.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun StepItem(stepNumber: Int, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = "$stepNumber.",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 6.dp)
        )
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ManualStepItem(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
        )
    }
}

