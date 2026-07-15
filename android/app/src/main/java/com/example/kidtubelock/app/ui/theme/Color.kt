package com.example.kidtubelock.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val BrandBlue = Color(0xFF1E88E5)
val BrandBlueDark = Color(0xFF0D47A1)
val BrandYellow = Color(0xFFFFD54F)
val BrandGreen = Color(0xFF4CAF50)
val BrandSky = Color(0xFF81D4FA)
val BrandNavy = Color(0xFF07162F)
val BrandNavyDeep = Color(0xFF041022)
val BrandCard = Color(0xFF0F2D60)
val BrandCardAlt = Color(0xFF153B77)
val BrandOutline = Color(0xFF4D72B7)
val BrandTextMuted = Color(0xFFC2D7FF)

fun parentBackgroundBrush(): Brush = Brush.verticalGradient(
    colors = listOf(
        BrandNavyDeep,
        BrandNavy,
        BrandBlueDark,
    ),
)
