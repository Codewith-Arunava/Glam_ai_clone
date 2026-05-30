package com.example.data

@androidx.compose.runtime.Immutable
data class MediaPreset(
    val id: String,
    val title: String,
    val type: String, // "PHOTO" or "VIDEO"
    val url: String,
    val durationSecs: Float = 0f,
    val defaultPreset: String = "Classic",
    val description: String = "High fidelity style preview template"
)

@androidx.compose.runtime.Immutable
data class AestheticFilter(
    val id: String,
    val name: String,
    val colorOverlayHex: String,
    val description: String,
    val iconName: String,
    val hasScanlines: Boolean = false,
    val hasGrain: Boolean = false,
    val hasGlow: Boolean = false
)

@androidx.compose.runtime.Immutable
data class AestheticBackground(
    val id: String,
    val name: String,
    val url: String,
    val description: String
)

object PresetCatalog {
    val sampleMedia = listOf(
        MediaPreset(
            id = "preset_fashion_1",
            title = "Golden Glow Portrait",
            type = "PHOTO",
            url = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=800",
            defaultPreset = "Classic Glow",
            description = "High fashion elegant studio look with delicate warm contours."
        ),
        MediaPreset(
            id = "preset_cyber_2",
            title = "Cyber Neon Lookbook",
            type = "PHOTO",
            url = "https://images.unsplash.com/photo-1515621061946-eff1c2a352bd?auto=format&fit=crop&q=80&w=800",
            defaultPreset = "Cyberpunk Neo",
            description = "Ultraviolet and neon teal ambient look captured in a metropolitan streetscape."
        ),
        MediaPreset(
            id = "preset_video_runway",
            title = "Cinematic Runway Loop",
            type = "VIDEO",
            url = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=800",
            durationSecs = 15.0f,
            defaultPreset = "Retro VHS",
            description = "Short-form model walk, optimized for story and video effect layer testing."
        ),
        MediaPreset(
            id = "preset_man_classic",
            title = "Aesthetic Minimalist",
            type = "PHOTO",
            url = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=800",
            defaultPreset = "B&W Noir",
            description = "Clean aesthetic portrait focusing on shadows, contrast and skin retouch."
        ),
        MediaPreset(
            id = "preset_video_tokyo",
            title = "Tokyo Night Traffic",
            type = "VIDEO",
            url = "https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&q=80&w=800",
            durationSecs = 20.0f,
            defaultPreset = "Vintage film",
            description = "Hypnotic neon light trails ideal for retro film layers and sound loops."
        )
    )

    val filters = listOf(
        AestheticFilter(
            id = "filter_none",
            name = "Original",
            colorOverlayHex = "#00000000",
            description = "No color alteration",
            iconName = "Block"
        ),
        AestheticFilter(
            id = "filter_vintage",
            name = "Retro VHS",
            colorOverlayHex = "#40E699FF", // tint mix sepia/blue
            description = "Analog warm scanlines & nostalgia date stamps",
            iconName = "Tv",
            hasScanlines = true,
            hasGrain = true
        ),
        AestheticFilter(
            id = "filter_cyber",
            name = "Teal & Cyber",
            colorOverlayHex = "#3000FFFF", // neon purple
            description = "Electric neon lighting overlays",
            iconName = "Bolt",
            hasGlow = true
        ),
        AestheticFilter(
            id = "filter_gold",
            name = "Glam Glow",
            colorOverlayHex = "#20FFD700", // gold glow tint
            description = "Delicate golden aura skin brightening",
            iconName = "AutoAwesome",
            hasGlow = true
        ),
        AestheticFilter(
            id = "filter_monochrome",
            name = "B&W Film",
            colorOverlayHex = "#90333333", // monochrome conversion descriptor
            description = "Classic cinematic high-grain black and white",
            iconName = "CameraRoll",
            hasGrain = true
        ),
        AestheticFilter(
            id = "filter_vapor",
            name = "Vaporwave",
            colorOverlayHex = "#35FF40A1", // pink tint
            description = "80s pink neon dreamscape atmosphere",
            iconName = "Palette",
            hasScanlines = true,
            hasGlow = true
        )
    )

    val backgrounds = listOf(
        AestheticBackground(
            id = "bg_paris",
            name = "Paris Bistro Cafe",
            url = "https://images.unsplash.com/photo-1549144511-f099e773c147?auto=format&fit=crop&q=80&w=600",
            description = "Romantic coffee shop street exterior with warm highlights."
        ),
        AestheticBackground(
            id = "bg_tokyo",
            name = "Cyberpunk Alley",
            url = "https://images.unsplash.com/photo-1503899036084-c55cdd92da26?auto=format&fit=crop&q=80&w=600",
            description = "Dazzling neon signs in Shinjuku alleys reflecting in raindrops."
        ),
        AestheticBackground(
            id = "bg_castle",
            name = "Cloud Kingdom",
            url = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&q=80&w=600",
            description = "Dreamy majestic fantasy castle floating above glowing pink puffy clouds."
        ),
        AestheticBackground(
            id = "bg_space",
            name = "Cosmic Shuttle Interior",
            url = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&q=80&w=600",
            description = "Sleek metallic starship cockpit facing distant galaxy clusters."
        )
    )
}
