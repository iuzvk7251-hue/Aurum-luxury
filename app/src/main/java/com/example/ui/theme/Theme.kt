package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val AurumLightColorScheme = lightColorScheme(
    primary = ObsidianBlack,
    onPrimary = PureWhite,
    primaryContainer = GoldSoftTint,
    onPrimaryContainer = ObsidianBlack,
    secondary = GoldPrimary,
    onSecondary = ObsidianBlack,
    secondaryContainer = WarmIvory,
    onSecondaryContainer = ObsidianBlack,
    tertiary = GoldDeepAccessible,
    onTertiary = PureWhite,
    background = AlabasterWhite,
    onBackground = ObsidianBlack,
    surface = PureWhite,
    onSurface = ObsidianBlack,
    surfaceVariant = WarmIvory,
    onSurfaceVariant = MutedSlate,
    outline = SoftStoneBorder,
    error = DisconnectedRed,
    onError = PureWhite
)

private val AurumDarkColorScheme = darkColorScheme(
    primary = GoldBright,
    onPrimary = ObsidianBlack,
    primaryContainer = GoldDarkTint,
    onPrimaryContainer = GoldBright,
    secondary = PureWhite,
    onSecondary = ObsidianBlack,
    secondaryContainer = ElevatedDarkSurface,
    onSecondaryContainer = PureWhite,
    tertiary = GoldPrimary,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = PureWhite,
    surface = CharcoalSurface,
    onSurface = PureWhite,
    surfaceVariant = ElevatedDarkSurface,
    onSurfaceVariant = LightMutedSlate,
    outline = DarkStoneBorder,
    error = DisconnectedRed,
    onError = PureWhite
)

val AurumShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AurumLuxeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AurumDarkColorScheme else AurumLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AurumTypography,
        shapes = AurumShapes,
        content = content
    )
}
