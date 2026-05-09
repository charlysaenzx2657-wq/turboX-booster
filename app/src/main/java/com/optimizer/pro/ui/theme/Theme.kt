package com.optimizer.pro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.sp

// ╔══════════════════════════════════════════════════════════════╗
// ║       TURBOX ULTRA THEME v3.0 — CYBERPUNK NEON DARK         ║
// ╚══════════════════════════════════════════════════════════════╝

object TurboXColors {
    val NeonPurple        = Color(0xFF9B5DE5)
    val NeonPurpleDeep    = Color(0xFF7C3AED)
    val NeonPurpleBright  = Color(0xFFBD7FF5)
    val NeonCyan          = Color(0xFF00F5FF)
    val NeonCyanDim       = Color(0xFF00D4FF)
    val NeonGreen         = Color(0xFF00FF87)
    val NeonGreenDim      = Color(0xFF16A34A)
    val NeonOrange        = Color(0xFFFF6B35)
    val NeonRed           = Color(0xFFFF3864)
    val NeonYellow        = Color(0xFFFFE600)
    val BgDeep            = Color(0xFF05050F)
    val BgBase            = Color(0xFF0A0A1A)
    val BgCard            = Color(0xFF0F0F1E)
    val BgCardElevated    = Color(0xFF141428)
    val BgSurface         = Color(0xFF1A1A2E)
    val BgSurfaceVariant  = Color(0xFF16213E)
    val TextPrimary       = Color(0xFFF0F0FF)
    val TextSecondary     = Color(0xFFB0B0CC)
    val TextTertiary      = Color(0xFF6060A0)
    val TextDisabled      = Color(0xFF303060)
    val GlowPurple        = Color(0x339B5DE5)
    val GlowCyan          = Color(0x3300F5FF)
    val GlowGreen         = Color(0x3300FF87)
    val GradStart         = Color(0xFF9B5DE5)
    val GradEnd           = Color(0xFF00F5FF)
}

private val UltraDarkColorScheme = darkColorScheme(
    primary              = TurboXColors.NeonPurple,
    onPrimary            = TurboXColors.BgDeep,
    primaryContainer     = TurboXColors.NeonPurple.copy(alpha = 0.15f),
    onPrimaryContainer   = TurboXColors.NeonPurpleBright,
    secondary            = TurboXColors.NeonCyan,
    onSecondary          = TurboXColors.BgDeep,
    secondaryContainer   = TurboXColors.NeonCyan.copy(alpha = 0.12f),
    onSecondaryContainer = TurboXColors.NeonCyan,
    tertiary             = TurboXColors.NeonGreen,
    onTertiary           = TurboXColors.BgDeep,
    tertiaryContainer    = TurboXColors.NeonGreen.copy(alpha = 0.12f),
    onTertiaryContainer  = TurboXColors.NeonGreen,
    error                = TurboXColors.NeonRed,
    onError              = Color.White,
    errorContainer       = TurboXColors.NeonRed.copy(alpha = 0.15f),
    onErrorContainer     = TurboXColors.NeonRed,
    background           = TurboXColors.BgDeep,
    onBackground         = TurboXColors.TextPrimary,
    surface              = TurboXColors.BgBase,
    onSurface            = TurboXColors.TextPrimary,
    surfaceVariant       = TurboXColors.BgSurfaceVariant,
    onSurfaceVariant     = TurboXColors.TextSecondary,
    outline              = TurboXColors.NeonPurple.copy(alpha = 0.25f),
    outlineVariant       = TurboXColors.TextTertiary.copy(alpha = 0.3f),
    inverseSurface       = TurboXColors.TextPrimary,
    inverseOnSurface     = TurboXColors.BgBase,
    inversePrimary       = TurboXColors.NeonPurpleDeep,
    surfaceTint          = TurboXColors.NeonPurple,
    scrim                = Color.Black.copy(alpha = 0.7f),
)

val TurboXTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.Black,     fontSize = 34.sp, letterSpacing = (-1.0).sp,  lineHeight = 40.sp),
    displayMedium = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, letterSpacing = (-0.5).sp,  lineHeight = 34.sp),
    displaySmall  = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, letterSpacing = (-0.25).sp, lineHeight = 30.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 22.sp, letterSpacing = 0.sp,       lineHeight = 28.sp),
    headlineMedium= TextStyle(fontWeight = FontWeight.Bold,      fontSize = 20.sp, letterSpacing = 0.sp,       lineHeight = 26.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 18.sp, letterSpacing = 0.sp,       lineHeight = 24.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 17.sp, letterSpacing = 0.15.sp,    lineHeight = 22.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 15.sp, letterSpacing = 0.1.sp,     lineHeight = 20.sp),
    titleSmall    = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 13.sp, letterSpacing = 0.1.sp,     lineHeight = 18.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 16.sp, letterSpacing = 0.5.sp,     lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 14.sp, letterSpacing = 0.25.sp,    lineHeight = 20.sp),
    bodySmall     = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 12.sp, letterSpacing = 0.4.sp,     lineHeight = 16.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 13.sp, letterSpacing = 0.1.sp,     lineHeight = 18.sp),
    labelMedium   = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 11.sp, letterSpacing = 0.5.sp,     lineHeight = 16.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 10.sp, letterSpacing = 0.5.sp,     lineHeight = 14.sp),
)

@Composable
fun TurboXTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = UltraDarkColorScheme,
        typography  = TurboXTypography,
        content     = content
    )
}
