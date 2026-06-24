package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KakarotColorScheme = darkColorScheme(
    primary = SaiyanGold,
    onPrimary = DeepSpaceBlack,
    secondary = SaiyanBlue,
    onSecondary = DeepSpaceBlack,
    tertiary = FeralCrimson,
    background = DeepSpaceBlack,
    onBackground = TextPrimary,
    surface = DarkSlateCard,
    onSurface = TextPrimary,
    outline = BorderDark,
    surfaceVariant = DarkSlateCard,
    onSurfaceVariant = TextSecondary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KakarotColorScheme,
        typography = Typography,
        content = content
    )
}
