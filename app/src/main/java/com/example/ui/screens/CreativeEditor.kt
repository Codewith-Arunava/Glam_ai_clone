package com.example.ui.screens

import android.graphics.Color as AndroidColor
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.AiOperationState
import com.example.ui.EditorViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreativeEditor(
    viewModel: EditorViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val media by viewModel.selectedMedia.collectAsState()
    val filter by viewModel.selectedFilter.collectAsState()
    val bgByPreset by viewModel.selectedBg.collectAsState()

    // Screen States
    val activeTab = remember { mutableStateOf("Beauty") } // "Beauty", "Filters", "BG Swap", "Timeline", "Layout"
    val isExporting by viewModel.isExporting.collectAsState()
    val exportProgress by viewModel.exportProgress.collectAsState()
    val lastExportedItem by viewModel.lastExportedItem.collectAsState()

    // Smooth sliders states
    val smoothState by viewModel.skinSmooth.collectAsState()
    val slimState by viewModel.slimFace.collectAsState()
    val eyeState by viewModel.eyeEnlarge.collectAsState()
    val glowState by viewModel.faceGlow.collectAsState()

    // Format layout states
    val ratioState by viewModel.activeRatio.collectAsState()
    val textOverlayState by viewModel.textOverlay.collectAsState()

    // Sequence States
    val startTrimState by viewModel.startTrim.collectAsState()
    val endTrimState by viewModel.endTrim.collectAsState()
    val playProgressState by viewModel.playProgress.collectAsState()
    val isPlayingState by viewModel.isPlaying.collectAsState()

    val aiState by viewModel.aiState.collectAsState()
    var showExportDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Observe last exported item to show success alert
    LaunchedEffect(lastExportedItem) {
        if (lastExportedItem != null) {
            showExportDialog = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF09090B)) // Deep dark Zinc-950 canvas frame
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Screen Title & Save bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Text(
                    text = if (media.type == "VIDEO") "Cinematic Video Editor" else "High-Fashion Retouch",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { viewModel.exportAndSave() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD946EF)), // Vibrant Fuchsia EXPORT button
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("export_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Export",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Central Canvas Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Compute Aspect Ratio
                val currentRatio = when (ratioState) {
                    "1:1" -> 1f
                    "9:16" -> 0.5625f
                    "4:5" -> 0.8f
                    else -> 0.5625f
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(currentRatio)
                        .clip(RoundedCornerShape(32.dp)) // Bold rounded-[2rem] corner radius matching the theme
                        .background(Color.Black)
                        .border(1.dp, Color(0xFF27272A), RoundedCornerShape(32.dp)) // border-zinc-800
                ) {
                    // Photo / Background Swap image layers (Coil supported)
                    AsyncImage(
                        model = bgByPreset?.url ?: media.url,
                        contentDescription = "Viewport Preview",
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                drawContent() // Draw original image first

                                // Apply skin glow effect
                                if (glowState > 0.1f) {
                                    drawRect(
                                        color = Color.White.copy(alpha = glowState * 0.25f),
                                        size = size
                                    )
                                }

                                // Apply Filter Tint overlay descriptions
                                if (filter.id != "filter_none") {
                                    val filterColor = Color(AndroidColor.parseColor(filter.colorOverlayHex))
                                    drawRect(
                                        color = filterColor.copy(alpha = 0.22f),
                                        size = size,
                                        blendMode = androidx.compose.ui.graphics.BlendMode.Color
                                    )
                                }

                                // Apply procedural analog VHS scanline layers
                                if (filter.hasScanlines) {
                                    val pixelStep = 8f
                                    var currentY = 0f
                                    while (currentY < size.height) {
                                        drawLine(
                                            color = Color.Black.copy(alpha = 0.15f),
                                            start = Offset(0f, currentY),
                                            end = Offset(size.width, currentY),
                                            strokeWidth = 2.dp.toPx()
                                        )
                                        currentY += pixelStep
                                    }
                                }

                                // Apply retro film grain layers
                                if (filter.hasGrain) {
                                    val random = Random(42)
                                    for (i in 0..12) {
                                        val x = random.nextFloat() * size.width
                                        val y = random.nextFloat() * size.height
                                        val currentRadius = random.nextFloat() * 4.dp.toPx()
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.12f),
                                            radius = currentRadius,
                                            center = Offset(x, y)
                                        )
                                    }
                                }
                            },
                        contentScale = ContentScale.Crop
                    )

                    // Vertical Split Compare Line Overlay (Vibrant Palette Theme)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(2.dp)
                            .background(Color.White.copy(alpha = 0.40f))
                            .align(Alignment.Center)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .align(Alignment.Center),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Before/After Drag",
                                tint = Color(0xFF09090B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Face Target Retouch active bounding box & text overlay target (Vibrant Palette style HUD)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 170.dp, height = 210.dp)
                                .border(
                                    width = 1.5.dp,
                                    brush = Brush.sweepGradient(listOf(Color(0xFFD946EF).copy(0.4f), Color(0xFF8B5CF6).copy(0.4f))),
                                    shape = RoundedCornerShape(24.dp)
                                )
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFD946EF).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFFD946EF).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "AI TARGET RETOUCH ACTIVE",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Show active AI status banner overlay
                    if (aiState is AiOperationState.Processing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.65f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color(0xFFD946EF))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Consulting Glam AI Engine...",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Floating Watermark Text sticker
                    if (textOverlayState.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = textOverlayState,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Video duration stamp if applicable
                    if (media.type == "VIDEO") {
                        val currentFrameSecs = (playProgressState / 100f) * media.durationSecs
                        Box(
                            modifier = Modifier
                                .padding(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            Text(
                                text = String.format("00:%02d", currentFrameSecs.toInt()),
                                color = Color(0xFFD946EF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // AI dynamic suggestions output logger
            AnimatedVisibility(
                visible = aiState is AiOperationState.Success || aiState is AiOperationState.Error,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)), // Zinc-900
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFD946EF).copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI",
                                    tint = Color(0xFFD946EF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Glam AI Insight",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = { viewModel.editHistoryItem(viewModel.historyList.value.firstOrNull() ?: return@IconButton) }, // Quick reset helper
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hide",
                                    tint = Color.LightGray.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))

                        if (aiState is AiOperationState.Success) {
                            val success = aiState as AiOperationState.Success
                            var desc = "Enhanced contours successfully."
                            var colorGrading = "Neutral Ambient Tint"
                            try {
                                val json = JSONObject(success.responseJson)
                                desc = json.optString("creative_description", desc)
                                colorGrading = json.optString("color_grading", colorGrading)
                            } catch (e: Exception) {
                                // Direct text parsing fallback
                                desc = success.responseJson
                            }

                            Text(
                                text = desc,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Color grading matched: $colorGrading",
                                color = Color(0xFFD946EF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (aiState is AiOperationState.Error) {
                            Text(
                                text = "Error contacting Gemini: ${(aiState as AiOperationState.Error).errorMessage}",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Quick Play controls for short videos
            if (media.type == "VIDEO") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = { viewModel.isPlaying.value = !isPlayingState },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFD946EF), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlayingState) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Tab Selection bar
            ScrollableTabRow(
                selectedTabIndex = getTabIndex(activeTab.value, media.type),
                containerColor = Color(0xFF18181B),
                contentColor = Color.White,
                edgePadding = 12.dp,
                divider = {}
            ) {
                TabItem(text = "Beauty", activeTab = activeTab)
                TabItem(text = "Filters", activeTab = activeTab)
                TabItem(text = "BG Swap", activeTab = activeTab)
                if (media.type == "VIDEO") {
                    TabItem(text = "Timeline", activeTab = activeTab)
                }
                TabItem(text = "Layout", activeTab = activeTab)
            }

            // Bottom Workspace Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
                    .background(Color(0xFF18181B))
                    .padding(horizontal = 20.dp)
            ) {
                when (activeTab.value) {
                    "Beauty" -> BeautyWorkspace(viewModel)
                    "Filters" -> FiltersWorkspace(viewModel)
                    "BG Swap" -> BgSwapWorkspace(viewModel)
                    "Timeline" -> TimelineWorkspace(viewModel)
                    "Layout" -> LayoutWorkspace(viewModel)
                }
            }
        }

        // Simulating rendering progress overlay during export
        if (isExporting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.82f))
                    .clickable(enabled = false) {}, // absorb touch events
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { exportProgress / 100f },
                        color = Color(0xFFD946EF),
                        strokeWidth = 6.dp,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Rendering AI Presets...",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Processing layers & merging textures (${exportProgress.toInt()}%)",
                        color = Color.LightGray.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Export success dialog
        if (showExportDialog) {
            val item = lastExportedItem
            Dialog(onDismissRequest = {
                viewModel.clearLastExport()
                showExportDialog = false
            }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFFD946EF).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00FF87).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Color(0xFF00FF87),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Export Completed!",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your high resolution file '${item?.title}' is compiled and safely stored in your Glam history album.",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                viewModel.clearLastExport()
                                showExportDialog = false
                                onBack()
                            },
                             colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD946EF)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabItem(text: String, activeTab: MutableState<String>) {
    val selected = activeTab.value == text
    Tab(
        selected = selected,
        onClick = { activeTab.value = text },
        text = {
            Text(
                text = text.uppercase(),
                color = if (selected) Color(0xFFD946EF) else Color.LightGray.copy(alpha = 0.7f),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
        },
        modifier = Modifier.testTag("tab_$text")
    )
}

private fun getTabIndex(tab: String, type: String): Int {
    return when (tab) {
        "Beauty" -> 0
        "Filters" -> 1
        "BG Swap" -> 2
        "Timeline" -> if (type == "VIDEO") 3 else 2
        "Layout" -> if (type == "VIDEO") 4 else 3
        else -> 0
    }
}

// --- Tab Workspaces ---

@Composable
fun BeautyWorkspace(viewModel: EditorViewModel) {
    val smooth by viewModel.skinSmooth.collectAsState()
    val slim by viewModel.slimFace.collectAsState()
    val eye by viewModel.eyeEnlarge.collectAsState()
    val glow by viewModel.faceGlow.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Skin Smooth", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${(smooth * 100).toInt()}%", color = Color(0xFFD946EF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = smooth,
                onValueChange = { viewModel.skinSmooth.value = it },
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFD946EF),
                    activeTrackColor = Color(0xFFD946EF)
                ),
                modifier = Modifier.testTag("slider_smooth")
            )
        }

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Liquid Face Slimming", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${(slim * 100).toInt()}%", color = Color(0xFFD946EF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = slim,
                onValueChange = { viewModel.slimFace.value = it },
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFD946EF),
                    activeTrackColor = Color(0xFFD946EF)
                ),
                modifier = Modifier.testTag("slider_slim")
            )
        }

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Aesthetic Eye Enlarging", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${(eye * 100).toInt()}%", color = Color(0xFFD946EF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = eye,
                onValueChange = { viewModel.eyeEnlarge.value = it },
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFD946EF),
                    activeTrackColor = Color(0xFFD946EF)
                ),
                modifier = Modifier.testTag("slider_eye")
            )
        }

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Starlight Skin Glow", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${(glow * 100).toInt()}%", color = Color(0xFFD946EF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = glow,
                onValueChange = { viewModel.faceGlow.value = it },
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFD946EF),
                    activeTrackColor = Color(0xFFD946EF)
                ),
                modifier = Modifier.testTag("slider_glow")
            )
        }

        // Active AI Retouch launch button
        Button(
            onClick = { viewModel.runAiRetouch() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .height(48.dp)
                .testTag("ai_retouch_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D1B2C)),
            border = BorderStroke(1.dp, Color(0xFFD946EF).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI Action",
                tint = Color(0xFFD946EF)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Run AI Beauty Auto-Retouch", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun FiltersWorkspace(viewModel: EditorViewModel) {
    val activeFilter by viewModel.selectedFilter.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "TAP CHOOSE INSTANT FILTER OVERLAY",
            color = Color.LightGray.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(PresetCatalog.filters) { filterItem ->
                val isSelected = filterItem.id == activeFilter.id
                Card(
                    modifier = Modifier
                        .width(90.dp)
                        .height(115.dp)
                        .clickable { viewModel.selectFilter(filterItem) }
                        .testTag("filter_option_${filterItem.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF261D3A) else Color(0xFF161324)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Color(0xFFD946EF) else Color.White.copy(alpha = 0.08f)
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (filterItem.id == "filter_none") Color.DarkGray
                                    else Color(AndroidColor.parseColor(filterItem.colorOverlayHex))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (filterItem.id == "filter_none") Icons.Default.Block else Icons.Default.FilterVintage,
                                contentDescription = filterItem.name,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = filterItem.name,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BgSwapWorkspace(viewModel: EditorViewModel) {
    val activeBg by viewModel.selectedBg.collectAsState()
    val customText by viewModel.customSwapPrompt.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "AI BACKGROUND SWAP & PORTATION STUDIO",
            color = Color.LightGray.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Card(
                    modifier = Modifier
                        .size(80.dp)
                        .clickable { viewModel.selectBg(null) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (activeBg == null) Color(0xFF261D3A) else Color(0xFF161324)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = if (activeBg == null) 2.dp else 1.dp,
                        color = if (activeBg == null) Color(0xFFD946EF) else Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Block, contentDescription = "Original", tint = Color.LightGray)
                            Text("Original", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            items(PresetCatalog.backgrounds) { bgItem ->
                val isSelected = activeBg?.id == bgItem.id
                Card(
                    modifier = Modifier
                        .size(80.dp)
                        .clickable { viewModel.selectBg(bgItem) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161324)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Color(0xFFD946EF) else Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = bgItem.url,
                            contentDescription = bgItem.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(0.6f))
                                .align(Alignment.BottomCenter)
                                .padding(2.dp)
                        ) {
                            Text(
                                text = bgItem.name,
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        Divider(color = Color.White.copy(0.08f))

        Text(
            text = "OR CUSTOM AI STYLING PROMPT",
            color = Color.LightGray.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        TextField(
            value = customText,
            onValueChange = { viewModel.customSwapPrompt.value = it },
            placeholder = { Text("Describe scenery, lighting like 'cozy cabin fireplace'...", fontSize = 12.sp, color = Color.Gray) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF27272A),
                unfocusedContainerColor = Color(0xFF121214),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color(0xFFD946EF)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_custom_input"),
            shape = RoundedCornerShape(10.dp)
        )

        Button(
            onClick = {
                if (customText.isNotEmpty()) {
                    viewModel.runAiBgReplace(customText)
                }
            },
            enabled = customText.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("swap_bg_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD946EF),
                disabledContainerColor = Color.DarkGray
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Wallpaper, contentDescription = "Swap")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate Backdrop with Gemini", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun TimelineWorkspace(viewModel: EditorViewModel) {
    val start by viewModel.startTrim.collectAsState()
    val end by viewModel.endTrim.collectAsState()
    val progress by viewModel.playProgress.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Short Video Timeline", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("Clip: ${start.toInt()}% - ${end.toInt()}%", color = Color(0xFFD946EF), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        // Simulating twin handles trimmer using sliders
        Column {
            Text(
                "Start Crop Marker",
                color = Color.LightGray.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = start,
                onValueChange = {
                    if (it < end - 10f) {
                        viewModel.startTrim.value = it
                    }
                },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFD946EF),
                    activeTrackColor = Color(0xFFD946EF)
                )
            )
        }

        Column {
            Text(
                "End Crop Marker",
                color = Color.LightGray.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = end,
                onValueChange = {
                    if (it > start + 10f) {
                        viewModel.endTrim.value = it
                    }
                },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFD946EF),
                    activeTrackColor = Color(0xFFD946EF)
                )
            )
        }

        // Miniature timeline bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF141120))
                .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(8.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val startX = (start / 100f) * size.width
                val endX = (end / 100f) * size.width

                // Draw excluded trim area - start
                drawRect(
                    color = Color.Red.copy(0.2f),
                    size = Size(startX, size.height)
                )

                // Draw active visual spectrum
                drawRect(
                    color = Color(0xFFD946EF).copy(0.12f),
                    topLeft = Offset(startX, 0f),
                    size = Size(endX - startX, size.height)
                )

                // Draw play head ticker line
                val tickerX = (progress / 100f) * size.width
                drawLine(
                    color = Color.White,
                    start = Offset(tickerX, 0f),
                    end = Offset(tickerX, size.height),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }
    }
}

@Composable
fun LayoutWorkspace(viewModel: EditorViewModel) {
    val ratio by viewModel.activeRatio.collectAsState()
    val captionText by viewModel.textOverlay.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "EXPORT ASPECT FORMATS",
            color = Color.LightGray.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RatioButton(label = "9:16 Story", value = "9:16", activeRatio = ratio, onClick = { viewModel.setAspectRatio("9:16") })
            RatioButton(label = "1:1 Square", value = "1:1", activeRatio = ratio, onClick = { viewModel.setAspectRatio("1:1") })
            RatioButton(label = "4:5 Carousel", value = "4:5", activeRatio = ratio, onClick = { viewModel.setAspectRatio("4:5") })
        }

        Divider(color = Color.White.copy(alpha = 0.08f))

        Text(
            text = "ADD TEXT STICKER LAYER OVERLAY",
            color = Color.LightGray.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        TextField(
            value = captionText,
            onValueChange = { viewModel.textOverlay.value = it },
            placeholder = { Text("E.g. #GLAMVIBES, Summer 2026...", color = Color.Gray, fontSize = 12.sp) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF27272A),
                unfocusedContainerColor = Color(0xFF121214),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color(0xFFD946EF)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("caption_input_field"),
            shape = RoundedCornerShape(10.dp)
        )
    }
}

@Composable
fun RowScope.RatioButton(
    label: String,
    value: String,
    activeRatio: String,
    onClick: () -> Unit
) {
    val isSelected = activeRatio == value
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFFD946EF) else Color(0xFF18181B)
        ),
        modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("ratio_btn_$value"),
        border = BorderStroke(
            1.dp,
            if (isSelected) Color(0xFFD946EF) else Color.White.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(label.split(" ").getOrNull(1) ?: "", fontSize = 8.sp, color = Color.White.copy(0.7f))
        }
    }
}
