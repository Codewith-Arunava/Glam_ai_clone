package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiService
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.InputStream
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface AiOperationState {
    object Idle : AiOperationState
    object Processing : AiOperationState
    data class Success(val responseJson: String, val message: String) : AiOperationState
    data class Error(val errorMessage: String) : AiOperationState
}

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = EditRepository(db.editDao())

    // List of creations (history) loaded directly from SQLite database Flow
    val historyList: StateFlow<List<EditItem>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current subject being edited (Default to first sample photo)
    private val _selectedMedia = MutableStateFlow<MediaPreset>(PresetCatalog.sampleMedia[0])
    val selectedMedia: StateFlow<MediaPreset> = _selectedMedia.asStateFlow()

    // Interactive sliders for Retouching
    val skinSmooth = MutableStateFlow(0f)
    val slimFace = MutableStateFlow(0f)
    val eyeEnlarge = MutableStateFlow(0f)
    val faceGlow = MutableStateFlow(0f)

    // Layout configuration
    val activeRatio = MutableStateFlow("9:16") // "9:16", "1:1", "4:5"
    val textOverlay = MutableStateFlow("")

    // Timeline control for Video trimming
    val startTrim = MutableStateFlow(0f)
    val endTrim = MutableStateFlow(100f)
    val playProgress = MutableStateFlow(0f)
    val isPlaying = MutableStateFlow(false)

    // Filter Layers
    private val _selectedFilter = MutableStateFlow<AestheticFilter>(PresetCatalog.filters[0])
    val selectedFilter: StateFlow<AestheticFilter> = _selectedFilter.asStateFlow()

    // Background replacement config
    private val _selectedBg = MutableStateFlow<AestheticBackground?>(null)
    val selectedBg: StateFlow<AestheticBackground?> = _selectedBg.asStateFlow()

    val customSwapPrompt = MutableStateFlow("")

    // Active AI operation feedback
    private val _aiState = MutableStateFlow<AiOperationState>(AiOperationState.Idle)
    val aiState: StateFlow<AiOperationState> = _aiState.asStateFlow()

    // Export progress tracker
    val isExporting = MutableStateFlow(false)
    val exportProgress = MutableStateFlow(0f)
    private val _lastExportedItem = MutableStateFlow<EditItem?>(null)
    val lastExportedItem: StateFlow<EditItem?> = _lastExportedItem.asStateFlow()

    init {
        // Simple mock player ticker to simulate video frames progressing
        viewModelScope.launch {
            while (true) {
                delay(120)
                if (isPlaying.value && _selectedMedia.value.type == "VIDEO") {
                    val current = playProgress.value
                    val start = startTrim.value
                    val end = endTrim.value
                    if (current >= end || current < start) {
                        playProgress.value = start
                    } else {
                        playProgress.value = current + 2.5f
                    }
                }
            }
        }
    }

    fun selectMedia(media: MediaPreset) {
        _selectedMedia.value = media
        // Reset properties
        skinSmooth.value = 0f
        slimFace.value = 0f
        eyeEnlarge.value = 0f
        faceGlow.value = 0f
        textOverlay.value = ""
        activeRatio.value = if (media.type == "VIDEO") "9:16" else "1:1"
        startTrim.value = 0f
        endTrim.value = 100f
        playProgress.value = 0f
        isPlaying.value = false
        _selectedFilter.value = PresetCatalog.filters[0]
        _selectedBg.value = null
        customSwapPrompt.value = ""
        _aiState.value = AiOperationState.Idle
    }

    fun selectFilter(filter: AestheticFilter) {
        _selectedFilter.value = filter
    }

    fun selectBg(bg: AestheticBackground?) {
        _selectedBg.value = bg
        if (bg != null) {
            customSwapPrompt.value = ""
        }
    }

    fun setAspectRatio(ratio: String) {
        activeRatio.value = ratio
    }

    // Load an item back into memory to re-edit it
    fun editHistoryItem(item: EditItem) {
        val mediaPreset = MediaPreset(
            id = "history_${item.id}",
            title = item.title,
            type = item.type,
            url = item.thumbnailUrl,
            durationSecs = if (item.type == "VIDEO") 12f else 0f,
            defaultPreset = item.activePreset
        )
        _selectedMedia.value = mediaPreset
        skinSmooth.value = item.softSkin
        slimFace.value = item.slimFace
        eyeEnlarge.value = item.eyeEnlarge
        faceGlow.value = item.glow
        textOverlay.value = item.textOverlay
        activeRatio.value = item.aspectRatio
        startTrim.value = item.startTrim
        endTrim.value = item.endTrim
        _selectedBg.value = null
        customSwapPrompt.value = item.aiPrompt

        val filterMatched = PresetCatalog.filters.find { it.name == item.activePreset }
        _selectedFilter.value = filterMatched ?: PresetCatalog.filters[0]
        _aiState.value = AiOperationState.Idle
    }

    /**
     * Call the Gemini API to analyze the current image and generate realistic retouch specs.
     * We download the image into a Bitmap in the background if it's a URL, then pass it to the service.
     */
    fun runAiRetouch() {
        val promptText = if (customSwapPrompt.value.isNotEmpty()) {
            customSwapPrompt.value
        } else {
            "Glow style: smoothing and contours enhancement"
        }

        viewModelScope.launch {
            _aiState.value = AiOperationState.Processing
            try {
                var bitmap: Bitmap? = null
                val imageUrl = _selectedMedia.value.url

                // Safely download bitmap on IO dispatcher for multimodal analysis
                if (imageUrl.startsWith("http")) {
                    bitmap = downloadBitmap(imageUrl)
                }

                val result = GeminiService.analyzeAndEnhancePhoto(
                    bitmap = bitmap,
                    userPrompt = promptText,
                    strength = skinSmooth.value
                )

                if (result == "API_KEY_MISSING_FALLBACK") {
                    // Graceful fallback simulation
                    delay(1500)
                    val mockJson = """
                        {
                           "creative_description": "Ambient retouching completed! Boosted local contrast and diffused ambient noise perfectly.",
                           "suggested_skin_tune": "Smoothness enhanced to 90%, face contours shaded.",
                           "color_grading": "Cinematic Pastel Rose Gold tint",
                           "recommended_preset": "Glam Soft Dream",
                           "applied_overlay_layer": "Satin Skin Radiance"
                        }
                    """.trimIndent()
                    _aiState.value = AiOperationState.Success(
                        responseJson = mockJson,
                        message = "AI Simulation succeeded! (Add real GEMINI_API_KEY in secrets panel for live processing)"
                    )
                } else if (result.startsWith("ERROR:")) {
                    _aiState.value = AiOperationState.Error(result)
                } else {
                    _aiState.value = AiOperationState.Success(
                        responseJson = result,
                        message = "Live Gemini AI enhancement applied successfully!"
                    )
                }
            } catch (e: Exception) {
                Log.e("EditorVM", "AI Retouch execution exception", e)
                _aiState.value = AiOperationState.Error(e.localizedMessage ?: "Unknown error occurred")
            }
        }
    }

    /**
     * Call the Gemini API to perform custom background removal & replacement analysis.
     */
    fun runAiBgReplace(customPrompt: String) {
        viewModelScope.launch {
            _aiState.value = AiOperationState.Processing
            customSwapPrompt.value = customPrompt
            try {
                var bitmap: Bitmap? = null
                val imageUrl = _selectedMedia.value.url

                if (imageUrl.startsWith("http")) {
                    bitmap = downloadBitmap(imageUrl)
                }

                val result = GeminiService.swapBackground(bitmap, customPrompt)

                if (result == "API_KEY_MISSING_FALLBACK") {
                    delay(1500)
                    val mockJson = """
                        {
                          "detected_foreground": "Clean subject detection isolated.",
                          "new_atmosphere": "$customPrompt",
                          "segmentation_mask_ref": "Segmented accurately. Blended light spill corrected.",
                          "color_match_ambient": "Warm backdrop temperature matching. Sunset flares matched."
                        }
                    """.trimIndent()
                    _aiState.value = AiOperationState.Success(
                        responseJson = mockJson,
                        message = "AI Background swap simulated! Paste real GEMINI_API_KEY in secrets panel to go live."
                    )
                } else if (result.startsWith("ERROR:")) {
                    _aiState.value = AiOperationState.Error(result)
                } else {
                    _aiState.value = AiOperationState.Success(
                        responseJson = result,
                        message = "Gemini AI replaced your background background seamlessly!"
                    )
                }
            } catch (e: Exception) {
                _aiState.value = AiOperationState.Error(e.localizedMessage ?: "Swap failed")
            }
        }
    }

    // Helper task to safely obtain images for Gemini multimodal API
    private suspend fun downloadBitmap(urlStr: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlStr)
            val connection = url.openConnection()
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.getInputStream()
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Log.e("EditorVM", "Failed to download preview bitmap: $urlStr", e)
            null
        }
    }

    /**
     * Export the active composition, showing progress bar and saving to local database history.
     */
    fun exportAndSave(customTitle: String? = null) {
        viewModelScope.launch {
            if (isExporting.value) return@launch
            isExporting.value = true
            exportProgress.value = 0f

            // Simulate creative rendering queue
            while (exportProgress.value < 100f) {
                delay(100)
                exportProgress.value += 5f
            }

            val finalTitle = customTitle ?: (_selectedMedia.value.title + " Edits")
            val savedItem = EditItem(
                title = finalTitle,
                type = _selectedMedia.value.type,
                thumbnailUrl = _selectedMedia.value.url,
                activePreset = _selectedFilter.value.name,
                softSkin = skinSmooth.value,
                slimFace = slimFace.value,
                eyeEnlarge = eyeEnlarge.value,
                glow = faceGlow.value,
                textOverlay = textOverlay.value,
                aspectRatio = activeRatio.value,
                videoEffect = if (_selectedMedia.value.type == "VIDEO") _selectedFilter.value.name else "None",
                startTrim = startTrim.value,
                endTrim = endTrim.value,
                aiPrompt = customSwapPrompt.value
            )

            // Save to Room SQLite Database
            val primaryKeyId = repository.saveItem(savedItem)
            _lastExportedItem.value = savedItem.copy(id = primaryKeyId.toInt())

            isExporting.value = false
        }
    }

    fun deleteHistoryItem(item: EditItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun clearLastExport() {
        _lastExportedItem.value = null
    }
}
