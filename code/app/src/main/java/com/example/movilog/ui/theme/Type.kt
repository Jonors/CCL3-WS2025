package com.example.movilog.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.movilog.R

// Define the Oswald Font Family
val Oswald = FontFamily(
    Font(R.font.oswald_extralight, FontWeight.ExtraLight),
    Font(R.font.oswald_light, FontWeight.Light),
    Font(R.font.oswald_regular, FontWeight.Normal),
    Font(R.font.oswald_medium, FontWeight.Medium),
    Font(R.font.oswald_semibold, FontWeight.SemiBold),
    Font(R.font.oswald_bold, FontWeight.Bold)
)

val Typography = Typography(
    // Used for the "MoviLog" Logo text
    headlineMedium = TextStyle(
        fontFamily = Oswald,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.5.sp
    ),
    // Used for Section Titles (Popular, Kids, etc.)
    titleMedium = TextStyle(
        fontFamily = Oswald,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, // Slightly increased for better hierarchy
        lineHeight = 28.sp,
        letterSpacing = 0.15.sp
    ),
    // Used for Movie Titles in lists/cards
    bodyLarge = TextStyle(
        fontFamily = Oswald,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.25.sp
    ),
    // Used for Search Bar placeholder & general labels
    labelLarge = TextStyle(
        fontFamily = Oswald,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // SPECIFIC FOR WATCHLIST: Small details like release year or rating
    labelSmall = TextStyle(
        fontFamily = Oswald,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    // Used for Movie Overviews/Descriptions
    bodyMedium = TextStyle(
        fontFamily = Oswald,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )
)