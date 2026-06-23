package com.example.ui

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BookmarkedItem
import com.example.data.MedicalReport
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmaSenseApp(viewModel: PharmaViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

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
                    Triple("AI Chat", Icons.Filled.QuestionAnswer, "chat_tab")
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
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "TabTransition"
            ) { targetTab ->
                when (targetTab) {
                    "Home" -> HomeTabContent(viewModel)
                    "Search" -> SearchTabContent(viewModel)
                    "Reports" -> ReportsTabContent(viewModel)
                    "Interactions" -> InteractionsTabContent(viewModel)
                    "AI Chat" -> ChatTabContent(viewModel)
                    else -> HomeTabContent(viewModel)
                }
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

// --- 1. HOME TAB ---

@Composable
fun RadialCategoryOrbitalWheel(viewModel: PharmaViewModel) {
    var activeIndex by remember { mutableStateOf(0) }
    var angleOffset by remember { mutableStateOf(0f) }
    var isAutoRotating by remember { mutableStateOf(true) }
    
    // Connected category indices map (matching similar health contexts from React template!)
    val relatedIndices = remember {
        mapOf(
            0 to 4, // Cardiovascular <-> Endocrine (Heart <-> Diabetes)
            1 to 3, // Respiratory <-> Infectious Diseases (Lungs <-> Infections)
            2 to 5, // Gastrointestinal <-> Pain Relief
            3 to 1, // Infectious Diseases <-> Respiratory
            4 to 0, // Endocrine <-> Cardiovascular
            5 to 2  // Pain Relief <-> Gastrointestinal
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
            Triple("🫀", Icons.Default.Favorite, "Cardiovascular"),
            Triple("🫁", Icons.Default.Info, "Respiratory"),
            Triple("🥗", Icons.Default.Refresh, "Gastrointestinal"),
            Triple("🦠", Icons.Default.Warning, "Infectious Diseases"),
            Triple("🩸", Icons.Default.Settings, "Endocrine"),
            Triple("💊", Icons.Default.Info, "Pain Relief")
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(activeEmojis.first, fontSize = 15.sp)
                        }
                        Text(
                            text = activeItem.first,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Arrow step backward
                        IconButton(
                            onClick = {
                                activeIndex = (activeIndex - 1 + categories.size) % categories.size
                                isAutoRotating = false
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Previous Node",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Arrow step forward
                        IconButton(
                            onClick = {
                                activeIndex = (activeIndex + 1) % categories.size
                                isAutoRotating = false
                            },
                            modifier = Modifier.size(26.dp)
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

                Text(
                    text = activeItem.second,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                )

                // Render matching connection pathways
                val relatedIdx = relatedIndices[activeIndex] ?: -1
                if (relatedIdx != -1) {
                    val relCategory = categories[relatedIdx]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🔗 Connected Path:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = "${activeItem.first} conditions correlate with ${relCategory.first}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Search action
                    Button(
                        onClick = {
                            viewModel.currentTab.value = "Search"
                            viewModel.onSearchQueryChanged(activeItem.first)
                        },
                        modifier = Modifier.weight(1.1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View Care", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }

                    // Chat/Ask AI advice context card
                    OutlinedButton(
                        onClick = {
                            viewModel.currentTab.value = "AI Chat"
                            viewModel.chatInput.value = "Provide a concise overview of typical care guidelines, key drug classes, and basic self-management advice for the ${activeItem.first} category (${activeItem.second})."
                        },
                        modifier = Modifier.weight(0.9f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                    ) {
                        Text("Ask Chat AI ⚡", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- 1. HOME TAB ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTabContent(viewModel: PharmaViewModel) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Clinical Banner (Self-Drawn/Canvas Vector Accent Header)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .glassyGlow(MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Column(
                    modifier = Modifier.fillMaxHeight().fillMaxWidth(0.68f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "PharmaSense AI",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Your Smart Pharmacy Companion",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Explore medicines, check interactions & analyze lab sheets instantly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.70f)
                    )
                }
                
                // Drawn pill geometry on canvas representing active clinical design
                Canvas(
                    modifier = Modifier
                        .size(75.dp)
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp)
                ) {
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(MedicalTeal, SecondaryTeal)
                        ),
                        topLeft = Offset(10f, 20f),
                        size = this.size / 1.5f,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
                    )
                    // Draw red capsule head cross
                    drawCircle(
                        color = Color.White.copy(alpha = 0.28f),
                        radius = 20f,
                        center = Offset(size.width / 2.3f, size.height / 2f)
                    )
                }
            }
        }

        // Quick Search Card
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
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Search diseases, drug class, symptoms...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }
        }

        // Popular Disease Categories Grid with Interactive React-inspired Orbital Toggle
        var viewAsOrbit by remember { mutableStateOf(true) }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Popular Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            TextButton(
                onClick = { viewAsOrbit = !viewAsOrbit },
                modifier = Modifier.testTag("categories_view_toggle")
            ) {
                Text(
                    text = if (viewAsOrbit) "View List 📋" else "Interactive Orbit ⚛️",
                    style = MaterialTheme.typography.bodySmall,
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

        // RED FLAG TRIAGE CARD (Highly dynamic health safety guide)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, DangerAlertRed.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = DangerAlertRed.copy(alpha = 0.03f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationImportant,
                        contentDescription = "Alert",
                        tint = DangerAlertRed,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = "Emergency Triage Guide",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = DangerAlertRed
                    )
                }
                Text(
                    text = "If you or someone around you experiences severe symptoms like:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                
                // Bullet points
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        "Sudden, crushing Chest Pain spreading to arm or jaw",
                        "Acute breathing difficulties or sudden severe wheezing",
                        "Numbness/weakness on one side of face or body"
                    ).forEach { symptom ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = DangerAlertRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(symptom, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f))
                        }
                    }
                }
                
                Button(
                    onClick = {
                        viewModel.currentTab.value = "Reports" // Redirect directly to safety evaluation
                        viewModel.symptomInput.value = "Chest pain, breathlessness"
                        viewModel.analyzeSymptoms()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerAlertRed),
                    modifier = Modifier.fillMaxWidth().testTag("triage_eval_btn"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Trigger Quick Triage Assessment", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Who Can Use / About Screen Guide
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Who Can Use This App?",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("👨‍🎓", fontSize = 24.sp)
                    Column {
                        Text("Students & Traineers", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("Perfect for offline learning, chemical categories checks, and quick drug profile reference.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("👨‍⚕️", fontSize = 24.sp)
                    Column {
                        Text("Pharmacy Professionals", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("Validate dosage classes and rapid dual-drug interactions lookup in seconds.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🏥", fontSize = 24.sp)
                    Column {
                        Text("General Public", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("Decipher your quantitative diagnostic lab results and research healthier habits.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Bottom Disclaimer
        Text(
            text = "PharmaSense AI is an educational smart pharmacy information companion. Always consult clinical practitioners for personal treatment plans.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 12.dp)
        )
    }
}

// --- 2. SEARCH / MEDICAL CATALOG TAB ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTabContent(viewModel: PharmaViewModel) {
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
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(disease.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text(disease.category) },
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
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(drug.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            Text(
                                                text = drug.drugClass.split(" ").first(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
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
fun ReportsTabContent(viewModel: PharmaViewModel) {
    var selectedToolSubTab by remember { mutableStateOf("Report") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
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
                        Text("Input quantitative reading counts manually. PharmaSense AI calculates range deviations and parses next medical actions.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

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

                        if (loading) {
                            Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Gemini is analyzing counts... (Up to 15s)", style = MaterialTheme.typography.bodySmall)
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
                                Text("Analyze and Save Lab Report", fontWeight = FontWeight.Bold)
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
fun InteractionsTabContent(viewModel: PharmaViewModel) {
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
fun ChatTabContent(viewModel: PharmaViewModel) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                
                if (messages.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearChatHistory() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = MaterialTheme.colorScheme.error)
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
