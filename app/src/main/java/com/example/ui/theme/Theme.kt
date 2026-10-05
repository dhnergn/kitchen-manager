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
    primary = CopperPrimary,
    secondary = SteelCharcoal,
    tertiary = SageGreen,
    background = DarkCarbonBg,
    surface = SteelSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFF4F1DE),
    onSurface = Color(0xFFF4F1DE),
    surfaceVariant = SteelSurface.copy(alpha = 0.8f),
    onSurfaceVariant = Color(0xFFF4F1DE).copy(alpha = 0.8f)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CopperPrimary,
    secondary = SteelCharcoal,
    tertiary = SageGreen,
    background = WarmParchmentBg,
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1E1E22),
    onSurface = Color(0xFF1E1E22),
    surfaceVariant = Color(0xFFECE7C6),
    onSurfaceVariant = Color(0xFF1E1E22).copy(alpha = 0.8f)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled by default to maintain the curated culinary branding
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
