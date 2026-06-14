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

private val DarkColorScheme =
  darkColorScheme(
    primary = BoutiquePrimary,
    secondary = BoutiqueSecondary,
    tertiary = BoutiqueTertiary,
    background = BoutiqueBackgroundDark,
    surface = BoutiqueSurfaceDark,
    onPrimary = BoutiqueOnPrimaryDark,
    onBackground = BoutiqueOnBackgroundDark,
    onSurface = BoutiqueOnBackgroundDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BoutiquePrimary,
    secondary = BoutiqueSecondary,
    tertiary = BoutiqueTertiary,
    background = BoutiqueBackgroundLight,
    surface = BoutiqueSurfaceLight,
    onPrimary = BoutiqueOnPrimaryLight,
    onBackground = BoutiqueOnBackgroundLight,
    onSurface = BoutiqueOnBackgroundLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
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
