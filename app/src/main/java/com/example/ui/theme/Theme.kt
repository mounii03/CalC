package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = OperatorKeyBg,
    onPrimary = Color.White,
    secondary = NumKeyBg,
    onSecondary = Color.White,
    tertiary = AccentTeal,
    onTertiary = Color.White,
    background = SlateBg,
    onBackground = Color.White,
    surface = DisplayBg,
    onSurface = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = LightOperatorKeyBg,
    onPrimary = Color.White,
    secondary = LightNumKeyBg,
    onSecondary = Color(0xFF4C1D95),
    tertiary = LightAccentTeal,
    onTertiary = Color.White,
    background = LightBg,
    onBackground = Color(0xFF1E1735),
    surface = LightDisplayBg,
    onSurface = Color(0xFF1E1735)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
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
