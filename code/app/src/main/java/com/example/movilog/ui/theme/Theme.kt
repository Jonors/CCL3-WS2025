package com.example.movilog.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


// We define a dark palette to match your MoviLog design
private val DarkColorScheme = darkColorScheme(
    primary = Color.White,          // Icons and primary text
    secondary = MoviLogYellow,      // Accents
    background = DeepNavy,          // Main screen background
    surface = SurfaceNavy,          // Card and search bar background
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun MoviLogTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current

    // This part makes the Status Bar match your DeepNavy background
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DeepNavy.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }


    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography, // This uses your Oswald fonts from Type.kt
        content = content
    )
}