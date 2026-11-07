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

private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimaryDark,
    onPrimary = DeepDarkBackground,
    primaryContainer = IndigoContainerDark,
    onPrimaryContainer = IndigoPrimaryDark,
    secondary = MintAccentDark,
    onSecondary = DeepDarkBackground,
    secondaryContainer = MintContainerDark,
    onSecondaryContainer = MintAccentDark,
    tertiary = AmberWarning,
    background = DeepDarkBackground,
    onBackground = TextPrimaryDark,
    surface = SurfaceCardDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceCardDarkSubtle,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    error = CoralLimit,
    errorContainer = CoralContainerDark
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = PureWhite,
    primaryContainer = IndigoContainerLight,
    onPrimaryContainer = IndigoPrimary,
    secondary = MintAccent,
    onSecondary = PureWhite,
    secondaryContainer = MintContainerLight,
    onSecondaryContainer = MintAccent,
    tertiary = AmberWarning,
    background = SoftBackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceCardLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceCardLightSubtle,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight,
    error = CoralLimit,
    errorContainer = CoralContainerLight
)

@Composable
fun ForceStopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our intentional serene wellbeing palette by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
