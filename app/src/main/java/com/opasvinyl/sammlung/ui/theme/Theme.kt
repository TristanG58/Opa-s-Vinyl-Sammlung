package com.opasvinyl.sammlung.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val VinylColorScheme = lightColorScheme(
    primary = VinylBrown,
    onPrimary = VinylWhite,
    primaryContainer = VinylBrownLight,
    onPrimaryContainer = VinylWhite,
    secondary = VinylGold,
    onSecondary = VinylBlack,
    secondaryContainer = VinylGoldLight,
    onSecondaryContainer = VinylBrownDark,
    tertiary = VinylRed,
    background = VinylCream,
    onBackground = VinylBlack,
    surface = VinylWhite,
    onSurface = VinylBlack,
    surfaceVariant = VinylCreamDark,
    onSurfaceVariant = VinylBrown,
    outline = VinylBrownLight
)

@Composable
fun OpasVinylTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VinylColorScheme,
        typography = Typography,
        content = content
    )
}
