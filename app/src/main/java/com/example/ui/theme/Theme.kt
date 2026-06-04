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

private val Scheme = lightColorScheme(
    primary = BarkBlue,
    onPrimary = PureWhite,
    secondary = NeonCyan,
    onSecondary = BarkBlue,
    tertiary = NeonCyan,
    onTertiary = BarkBlue,
    background = OffWhite,
    onBackground = BarkBlue,
    surface = PureWhite,
    onSurface = BarkBlue,
    surfaceVariant = LightCyanBackground,
    onSurfaceVariant = BarkBlue,
    error = CoralError,
    onError = PureWhite,
    primaryContainer = BarkBlue,
    onPrimaryContainer = PureWhite,
    secondaryContainer = LightCyanBackground,
    onSecondaryContainer = BarkBlue,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = CoralError
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color set to false to strictly enforce and persist our corporate brand colors
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = Scheme // Enforce the custom high-fidelity Bark-Blue & Neon-Cyan White palette consistently

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
