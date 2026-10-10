package com.hoaxdetector.app.ui.theme

import androidx.compose.ui.graphics.Color

// ==============================================================================
// STITCH REDESIGN EDITORIAL PALETTE (HOAX DETECTOR)
// ==============================================================================

// Primary - Deep Teal
val StitchPrimary = Color(0xFF00333A)
val StitchPrimaryContainer = Color(0xFF174A52)
val StitchPrimaryFixed = Color(0xFFBAEBF4)
val StitchOnPrimary = Color(0xFFFFFFFF)
val StitchOnPrimaryContainer = Color(0xFF89B9C2)

// Secondary - Royal Blue
val StitchSecondary = Color(0xFF3159B8)
val StitchSecondaryContainer = Color(0xFF769AFD)
val StitchOnSecondary = Color(0xFFFFFFFF)
val StitchOnSecondaryContainer = Color(0xFF002F7F)

// Tertiary - Forest Green
val StitchTertiary = Color(0xFF003522)
val StitchTertiaryContainer = Color(0xFF004E33)
val StitchTertiaryFixed = Color(0xFFA6F3CA)
val StitchOnTertiaryFixed = Color(0xFF002113)

// Surfaces & Backgrounds
val StitchSurface = Color(0xFFF8F9FF)
val StitchSurfaceContainer = Color(0xFFEBEEF6)
val StitchSurfaceContainerLow = Color(0xFFF1F3FC)
val StitchSurfaceContainerHigh = Color(0xFFE5E8F0)
val StitchSurfaceContainerHighest = Color(0xFFDFE2EB)
val StitchSurfaceContainerLowest = Color(0xFFFFFFFF)
val StitchSurfaceDim = Color(0xFFD7DAE2)

// Content & Text
val StitchOnSurface = Color(0xFF181C22)
val StitchOnSurfaceVariant = Color(0xFF40484A)
val StitchOutline = Color(0xFF70787A)
val StitchOutlineVariant = Color(0xFFC0C8CA)

// Status Colors
val StatusGreen = Color(0xFF005236)
val StatusGreenContainer = Color(0xFFA6F3CA)
val StatusGreenDim = Color(0xFFA6F3CA)
val StatusGreenText = Color(0xFF002113)

val StatusYellow = Color(0xFF8C5000)
val StatusYellowContainer = Color(0xFFFFDDB3)
val StatusYellowDim = Color(0xFFFFDDB3)
val StatusYellowText = Color(0xFF2C1600)

val StatusRed = Color(0xFFBA1A1A)
val StatusRedContainer = Color(0xFFFFDAD6)
val StatusRedDim = Color(0xFFFFDAD6)
val StatusRedText = Color(0xFF410002)

val StatusGray = Color(0xFF70787A)
val StatusGrayContainer = Color(0xFFEBEEF6)
val StatusGrayDim = Color(0xFFEBEEF6)

// Stitch Error Tokens
val StitchError = Color(0xFFBA1A1A)
val StitchErrorContainer = Color(0xFFFFDAD6)
val StitchOnError = Color(0xFFFFFFFF)
val StitchOnErrorContainer = Color(0xFF93000A)
val StitchSecondaryFixed = Color(0xFFDBE1FF)

// ==============================================================================
// COMPATIBILITY ALIASES
// ==============================================================================
val NavyDeep = StitchSurface
val NavyMedium = StitchSurfaceContainerLowest
val NavyLight = StitchSurfaceContainerLowest
val NavySurface = StitchSurfaceContainerLow

val ElectricBlue = StitchPrimaryContainer
val ElectricBlueLight = StitchSecondary
val ElectricBlueDark = StitchPrimary
val ElectricBlueDim = StitchSecondaryContainer.copy(alpha = 0.2f)

val TextPrimary = StitchOnSurface
val TextSecondary = StitchOnSurfaceVariant
val TextMuted = StitchOutline

val BorderColor = StitchOutlineVariant.copy(alpha = 0.5f)
val DividerColor = StitchOutlineVariant.copy(alpha = 0.3f)

val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)

