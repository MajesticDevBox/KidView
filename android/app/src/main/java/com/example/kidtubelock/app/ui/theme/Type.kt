package com.example.kidtubelock.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.kidtubelock.R

private val NunitoFamily = FontFamily(
    Font(
        resId = R.font.nunito_variable,
        weight = FontWeight.Normal,
    ),
    Font(
        resId = R.font.nunito_variable,
        weight = FontWeight.Medium,
    ),
    Font(
        resId = R.font.nunito_variable,
        weight = FontWeight.SemiBold,
    ),
    Font(
        resId = R.font.nunito_variable,
        weight = FontWeight.Bold,
    ),
)

val AppTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = NunitoFamily,
        fontSize = 38.sp,
        lineHeight = 42.sp,
        fontWeight = FontWeight.ExtraBold,
    ),
    headlineLarge = TextStyle(
        fontFamily = NunitoFamily,
        fontSize = 34.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.ExtraBold,
    ),
    headlineSmall = TextStyle(
        fontFamily = NunitoFamily,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.Bold,
    ),
    headlineMedium = TextStyle(
        fontFamily = NunitoFamily,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.ExtraBold,
    ),
    titleLarge = TextStyle(
        fontFamily = NunitoFamily,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Bold,
    ),
    titleMedium = TextStyle(
        fontFamily = NunitoFamily,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.Bold,
    ),
    titleSmall = TextStyle(
        fontFamily = NunitoFamily,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    bodyLarge = TextStyle(
        fontFamily = NunitoFamily,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = NunitoFamily,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = NunitoFamily,
        fontSize = 12.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = NunitoFamily,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Bold,
    ),
    labelMedium = TextStyle(
        fontFamily = NunitoFamily,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
    ),
)
