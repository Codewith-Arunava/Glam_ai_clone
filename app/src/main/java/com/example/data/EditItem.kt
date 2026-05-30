package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "edit_items")
data class EditItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // "PHOTO" or "VIDEO"
    val thumbnailUrl: String, // Can be built-in asset name, or external/generated URL
    val timestamp: Long = System.currentTimeMillis(),
    val activePreset: String = "None",
    val softSkin: Float = 0f,
    val slimFace: Float = 0f,
    val eyeEnlarge: Float = 0f,
    val glow: Float = 0f,
    val textOverlay: String = "",
    val aspectRatio: String = "9:16",
    val videoEffect: String = "None",
    val startTrim: Float = 0f,
    val endTrim: Float = 100f,
    val aiPrompt: String = ""
) : Serializable
