package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun MyApplicationTheme(
  activeLayoutId: Int = 3,
  selectedFontType: String = "Royal Serif",
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val (primaryColor, secondaryColor) = when (activeLayoutId) {
    1 -> Pair(Color(0xFF1B4D3E), Color(0xFF4C7B6B)) // Classic Emerald
    2 -> Pair(Color(0xFF8E4162), Color(0xFFC38D9E)) // Vintage Rose
    3 -> Pair(Color(0xFF7A2048), Color(0xFFB58A30)) // Royal Burgundy (Default)
    4 -> Pair(Color(0xFF1E293B), Color(0xFF64748B)) // Midnight Slate
    5 -> Pair(Color(0xFF111111), Color(0xFFC5A059)) // Golden Luxury
    6 -> Pair(Color(0xFF2B2B2B), Color(0xFF666666)) // Minimalist Clean
    7 -> Pair(Color(0xFF2E5A44), Color(0xFF4CA873)) // Eco Mint
    8 -> Pair(Color(0xFFD35400), Color(0xFFE67E22)) // Vibrant Peach
    9 -> Pair(Color(0xFF1A365D), Color(0xFF319795)) // Ocean Breeze
    10 -> Pair(Color(0xFF7E4E30), Color(0xFFD4A373)) // Retro Chic
    11 -> Pair(Color(0xFF5F4B8B), Color(0xFFA29BFE)) // Lavender Mist
    12 -> Pair(Color(0xFFE0115F), Color(0xFF00F5FF)) // Neon Noir
    13 -> Pair(Color(0xFF0D3B66), Color(0xFFF4D35E)) // Enterprise Blue
    14 -> Pair(Color(0xFF556B2F), Color(0xFFC5A059)) // Matcha Tea
    15 -> Pair(Color(0xFF800020), Color(0xFFC59B27)) // Prestige Crimson
    16 -> Pair(Color(0xFF4A154B), Color(0xFFE0115F)) // Plum Velvet
    else -> Pair(Color(0xFF7A2048), Color(0xFFB58A30))
  }

  val isNeonNoir = activeLayoutId == 12
  val baseBgLight = if (activeLayoutId == 11) Color(0xFFF7F4FD) else Color(0xFFFDFBF7)
  val baseBgDark = if (isNeonNoir) Color(0xFF0C0C0E) else Color(0xFF120C10)
  val baseSurfaceDark = if (isNeonNoir) Color(0xFF121216) else Color(0xFF1E141A)

  val colorScheme = if (darkTheme || isNeonNoir) {
    darkColorScheme(
      primary = primaryColor,
      secondary = secondaryColor,
      tertiary = secondaryColor,
      background = baseBgDark,
      surface = baseSurfaceDark,
      onPrimary = Color.White,
      onBackground = if (isNeonNoir) Color(0xFF00F5FF) else Color(0xFFEDE2E6),
      onSurface = Color(0xFFEDE2E6)
    )
  } else {
    lightColorScheme(
      primary = primaryColor,
      secondary = secondaryColor,
      tertiary = secondaryColor,
      background = baseBgLight,
      surface = Color.White,
      onPrimary = Color.White,
      onBackground = Color(0xFF2E2528),
      onSurface = Color(0xFF2E2528)
    )
  }

  // Choose Font Family based on dynamic user selection (Settings)
  val activeFontFamily = when (selectedFontType) {
    "Royal Serif" -> FontFamily.Serif
    "Modern Chic" -> FontFamily.SansSerif
    "Technical Mono" -> FontFamily.Monospace
    else -> FontFamily.Default
  }

  val dynamicTypography = androidx.compose.material3.Typography(
    bodyLarge = androidx.compose.ui.text.TextStyle(
      fontFamily = activeFontFamily,
      fontWeight = FontWeight.Normal,
      fontSize = 15.sp,
      lineHeight = 22.sp,
      letterSpacing = 0.5.sp
    ),
    bodyMedium = androidx.compose.ui.text.TextStyle(
      fontFamily = activeFontFamily,
      fontWeight = FontWeight.Normal,
      fontSize = 13.sp,
      lineHeight = 18.sp,
      letterSpacing = 0.25.sp
    ),
    bodySmall = androidx.compose.ui.text.TextStyle(
      fontFamily = activeFontFamily,
      fontWeight = FontWeight.Normal,
      fontSize = 11.sp,
      lineHeight = 15.sp,
      letterSpacing = 0.4.sp
    ),
    titleLarge = androidx.compose.ui.text.TextStyle(
      fontFamily = activeFontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 20.sp,
      lineHeight = 26.sp,
      letterSpacing = 0.sp
    ),
    titleMedium = androidx.compose.ui.text.TextStyle(
      fontFamily = activeFontFamily,
      fontWeight = FontWeight.Bold,
      fontSize = 15.sp,
      lineHeight = 22.sp,
      letterSpacing = 0.15.sp
    ),
    labelSmall = androidx.compose.ui.text.TextStyle(
      fontFamily = activeFontFamily,
      fontWeight = FontWeight.Medium,
      fontSize = 11.sp,
      lineHeight = 15.sp,
      letterSpacing = 0.5.sp
    )
  )

  MaterialTheme(colorScheme = colorScheme, typography = dynamicTypography, content = content)
}
