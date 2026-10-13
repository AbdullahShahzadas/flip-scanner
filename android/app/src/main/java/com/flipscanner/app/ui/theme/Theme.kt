package com.flipscanner.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColors = darkColorScheme(
    primary = Emerald,
    onPrimary = BgDark,
    primaryContainer = EmeraldDark,
    onPrimaryContainer = MoneyBright,
    background = BgDark,
    onBackground = androidx.compose.ui.graphics.Color(0xFFE8EEF2),
    surface = SurfaceDark,
    onSurface = androidx.compose.ui.graphics.Color(0xFFE8EEF2),
    surfaceVariant = CardDark,
)

private val LightColors = lightColorScheme(
    primary = EmeraldDark,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = MoneyBright,
    background = BgLight,
    onBackground = androidx.compose.ui.graphics.Color(0xFF101613),
    surface = SurfaceLight,
    onSurface = androidx.compose.ui.graphics.Color(0xFF101613),
)

@Composable
fun FlipScannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is off by default so the emerald "money" brand stays consistent
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FlipTypography,
        content = content
    )
}
