package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFD946EF), // Fuchsia-500
    secondary = Color(0xFF8B5CF6), // Violet-500
    tertiary = Color(0xFFE879F9), // Fuchsia-400
    background = Color(0xFF09090B), // Zinc-950 deep midnight
    surface = Color(0xFF18181B), // Zinc-900 card surface
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF4F4F5), // Zinc-50 bright text
    onSurface = Color(0xFFF4F4F5)
  )

private val LightColorScheme = DarkColorScheme // Force high-fidelity deep dark mode as standard creative canvas style

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Force fixed Vibrant Palette branding 
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
