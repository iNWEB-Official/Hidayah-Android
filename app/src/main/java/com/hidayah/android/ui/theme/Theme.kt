package com.hidayah.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Hidayah theme — brand is dark emerald + gold. In-app surfaces are cream (light)
 * or deep green-grey (dark); the user picks system / light / dark in Settings.
 */
@Composable
fun HidayahTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = DarkPalette.brand,
            onPrimary = EmeraldDark,
            primaryContainer = EmeraldDeep,
            onPrimaryContainer = Cream,
            secondary = Gold,
            onSecondary = EmeraldDark,
            secondaryContainer = GoldSoft,
            onSecondaryContainer = EmeraldDark,
            tertiary = GoldDeep,
            background = DarkPalette.background,
            onBackground = DarkPalette.ink,
            surface = DarkPalette.card,
            onSurface = DarkPalette.ink,
            surfaceVariant = DarkPalette.cardMuted,
            onSurfaceVariant = DarkPalette.sub,
            outline = DarkPalette.line,
            error = androidx.compose.ui.graphics.Color(0xFFFFB4AB)
        )
    } else {
        lightColorScheme(
            primary = Emerald,
            onPrimary = Cream,
            primaryContainer = EmeraldDeep,
            onPrimaryContainer = Cream,
            secondary = Gold,
            onSecondary = EmeraldDark,
            secondaryContainer = GoldSoft,
            onSecondaryContainer = EmeraldDark,
            tertiary = GoldDeep,
            background = CreamDim,
            onBackground = Ink,
            surface = Cream,
            onSurface = Ink,
            surfaceVariant = CreamDim,
            onSurfaceVariant = Sub,
            outline = Line,
            error = androidx.compose.ui.graphics.Color(0xFFB3261E)
        )
    }
    CompositionLocalProvider(LocalHidayahPalette provides if (darkTheme) DarkPalette else LightPalette) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
