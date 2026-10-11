package com.creatorledger.mm.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    val Navy = Color(0xFF0B1F3A)
    val NavyLight = Color(0xFF12294A)
    val Teal = Color(0xFF14B8A6)
    val TealDark = Color(0xFF0F766E)
    val TextSecondary = Color(0xFF9FB3C8)
    val Danger = Color(0xFFF87171)
    val Warning = Color(0xFFFBBF24)
}

private val AppDarkColors = darkColorScheme(
    primary = AppColors.Teal,
    onPrimary = AppColors.Navy,
    background = AppColors.Navy,
    onBackground = Color.White,
    surface = AppColors.NavyLight,
    onSurface = Color.White,
    secondaryContainer = AppColors.TealDark,
    onSecondaryContainer = Color.White
)

@Composable
fun CreatorLedgerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppDarkColors, content = content)
}
