package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.api.GeminiService
import com.example.data.EditItem
import com.example.data.MediaPreset
import com.example.data.PresetCatalog
import com.example.ui.EditorViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StudioHub(
    viewModel: EditorViewModel,
    onNavigateToEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val historyItems by viewModel.historyList.collectAsState()
    val isKeyConfigured = remember { GeminiService.isApiKeyConfigured() }

    // Cyberpunk Dark Background gradient of Vibrant Palette (Zinc-950 to charcoal)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF09090B),
                        Color(0xFF121215)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Elegant Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "GLAM",
                            color = Color(0xFFD946EF), // Fuchsia-500
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI",
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 2.sp
                        )
                    }
                    Text(
                        text = "Creative Photo & Video Studio",
                        color = Color.LightGray.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }

                // AI Key Status Pill
                val statusColor = if (isKeyConfigured) Color(0xFF00FF87) else Color(0xFFFFB300)
                val statusText = if (isKeyConfigured) "Gemini Live" else "AI Demo Mode"
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Warning note if API key is absent
                if (!isKeyConfigured) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1908)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFB300).copy(0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Add Your Gemini Key for Live AI",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Go to AI Studio Secrets panel, insert 'GEMINI_API_KEY' to enable fully live generated filters.",
                                        color = Color.LightGray,
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Presets Carousel Section
                item {
                    Text(
                        text = "CHOOSE A PRESET TO EDIT",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 12.dp)
                    )
                    
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(PresetCatalog.sampleMedia) { preset ->
                            PresetMediaCard(
                                preset = preset,
                                onClick = {
                                    viewModel.selectMedia(preset)
                                    onNavigateToEditor()
                                }
                            )
                        }
                    }
                }

                // History Section
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MY ALBERTS / HISTORY",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${historyItems.size} items",
                            color = Color.LightGray.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }

                if (historyItems.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(horizontal = 20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)), // Zinc-900
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.25f)) // Violet outer border
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Empty",
                                    tint = Color(0xFFD946EF).copy(alpha = 0.6f),
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Ready for Your Masterpiece",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Pick an aesthetic template above to start retouches, video effects, or background replacement swaps.",
                                    color = Color.LightGray.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Display historic creations
                    items(historyItems.chunked(2)) { chunk ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (item in chunk) {
                                Box(modifier = Modifier.weight(1f)) {
                                    HistoryCard(
                                        item = item,
                                        onSelect = {
                                            viewModel.editHistoryItem(item)
                                            onNavigateToEditor()
                                        },
                                        onDelete = {
                                            viewModel.deleteHistoryItem(item)
                                        }
                                    )
                                }
                            }
                            if (chunk.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Quick Launch Studio FAB
        ExtendedFloatingActionButton(
            onClick = {
                viewModel.selectMedia(PresetCatalog.sampleMedia[0])
                onNavigateToEditor()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .navigationBarsPadding(),
            containerColor = Color(0xFFD946EF), // Fuchsia Action FAB
            contentColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(Icons.Default.MovieFilter, contentDescription = "Editor")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Studio Editor")
        }
    }
}

@Composable
fun PresetMediaCard(
    preset: MediaPreset,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(210.dp)
            .clickable(onClick = onClick)
            .testTag("preset_card_${preset.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)), // Zinc-900
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.2f)) // Violet neon line
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = preset.url,
                contentDescription = preset.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Play indicator overlays for videos
            if (preset.type == "VIDEO") {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.62f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Video",
                            tint = Color(0xFFD946EF),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${preset.durationSecs.toInt()}s",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Glassmorphism metadata label at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = preset.title,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = preset.defaultPreset,
                        color = Color(0xFFD946EF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryCard(
    item: EditItem,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .clickable(onClick = onSelect)
            .testTag("history_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)), // Zinc-900
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Aspect ratio indicator badges
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = item.aspectRatio,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Close button to delete creation entry
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(28.dp)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Bottom title and timestamp details
            val sdf = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
            val formattedDate = remember(item.timestamp) { sdf.format(Date(item.timestamp)) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.activePreset,
                            color = Color(0xFFD946EF),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formattedDate,
                            color = Color.LightGray.copy(alpha = 0.5f),
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }
    }
}
