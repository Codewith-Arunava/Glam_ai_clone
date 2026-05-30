package com.example.api

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

// --- Gemini Request / Response models (Moshi supported matching built-in converter) ---

data class ContentPart(
    val text: String? = null,
    val inlineData: InlineDataPart? = null
)

data class InlineDataPart(
    val mimeType: String,
    val data: String
)

data class Content(
    val parts: List<ContentPart>
)

data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

data class Candidate(
    val content: Content?
)

data class GeminiResponse(
    val candidates: List<Candidate>?
)

// --- Retrofit Endpoint Service ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

// --- High level API wrapper and Helpers ---

object GeminiService {
    private const val TAG = "GeminiService"

    // Checks if key is configured
    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrEmpty() && key != "MY_GEMINI_API_KEY" && key != "placeholder"
    }

    // Helper: Convert Bitmap to Base64 String
    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * Ask Gemini to analyse photo & generate custom cinematic/artistic description filters
     */
    suspend fun analyzeAndEnhancePhoto(
        bitmap: Bitmap?,
        userPrompt: String,
        strength: Float
    ): String = withContext(Dispatchers.IO) {
        val key = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext "API_KEY_MISSING_FALLBACK"
        }

        val promptText = """
            You are "Glam AI" Creative Director. The user wants to enhance their photo with style: "$userPrompt".
            The current retouching strength slider is at ${(strength * 100).toInt()}%.
            Analyze the user's intent and return a vivid JSON response detailing how you enhanced it:
            {
               "creative_description": "A 1-sentence poetic explanation of the AI enhancement overlay applied.",
               "suggested_skin_tune": "Retouched details, glowing skin highlight.",
               "color_grading": "Luminous Teal & Orange, Vibrant Warm Shadows",
               "recommended_preset": "Cinematic Golden Hour",
               "applied_overlay_layer": "Radiant Disco Glow"
            }
            Return ONLY this JSON string, no enclosing markdown backticks or extra text.
        """.trimIndent()

        val parts = mutableListOf<ContentPart>()
        parts.add(ContentPart(text = promptText))

        if (bitmap != null) {
            try {
                parts.add(ContentPart(inlineData = InlineDataPart("image/jpeg", bitmap.toBase64())))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to encode bitmap", e)
            }
        }

        val request = GeminiRequest(
            contents = listOf(Content(parts = parts)),
            generationConfig = GenerationConfig(responseMimeType = "application/json")
        )

        try {
            val response = RetrofitClient.service.generateContent(key, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "{}"
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API call failed", e)
            "ERROR: ${e.localizedMessage}"
        }
    }

    /**
     * Solves background replacement and prompt details
     */
    suspend fun swapBackground(
        bitmap: Bitmap?,
        backgroundStylePrompt: String
    ): String = withContext(Dispatchers.IO) {
        val key = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext "API_KEY_MISSING_FALLBACK"
        }

        val promptText = """
            You are "Glam AI" Background Replace studio. The user wants to replace the background with: "$backgroundStylePrompt".
            Analyze the prompt and provide a beautiful, JSON configuration specifying how the segmentation mask is applied and how the foreground is composite.
            Return ONLY a raw JSON string like this:
            {
              "detected_foreground": "A high key fashion model or subject.",
              "new_atmosphere": "$backgroundStylePrompt",
              "segmentation_mask_ref": "Fine edge anti-aliasing feathering applied.",
              "color_match_ambient": "Harmonized highlight reflection matched to background tints."
            }
        """.trimIndent()

        val parts = mutableListOf<ContentPart>()
        parts.add(ContentPart(text = promptText))
        if (bitmap != null) {
            try {
                parts.add(ContentPart(inlineData = InlineDataPart("image/jpeg", bitmap.toBase64())))
            } catch (e: Exception) {
                Log.e(TAG, "Failed encoding bitmap for swap", e)
            }
        }

        val request = GeminiRequest(
            contents = listOf(Content(parts = parts)),
            generationConfig = GenerationConfig(responseMimeType = "application/json")
        )

        try {
            val response = RetrofitClient.service.generateContent(key, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "{}"
        } catch (e: Exception) {
            Log.e(TAG, "Background swap call failed", e)
            "ERROR: ${e.localizedMessage}"
        }
    }
}
