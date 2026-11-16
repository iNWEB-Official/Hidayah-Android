package com.hidayah.android.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic colors for the two Hidayah themes.
 * Brand emerald/gold stay constant (part of the identity); the surfaces and
 * text colors flip with the user-selected theme (system / light / dark).
 */
data class HidayahPalette(
    val isDark: Boolean,
    val background: Color,
    val card: Color,
    val cardMuted: Color,
    val panelHighlight: Color,
    val ink: Color,
    val sub: Color,
    val line: Color,
    val brand: Color,
    val brandOnSurface: Color,
    val brandMist: Color,
    val topBar: Color,
    val navBar: Color,
    val dotInactive: Color
)

val LightPalette = HidayahPalette(
    isDark = false,
    background = CreamDim,
    card = Cream,
    cardMuted = Color(0xFFF1ECD8),
    panelHighlight = Color(0xFFE3EDE2),
    ink = Ink,
    sub = Sub,
    line = Line,
    brand = Emerald,
    brandOnSurface = EmeraldDeep,
    brandMist = EmeraldMist,
    topBar = Emerald,
    navBar = Cream,
    dotInactive = Color(0xFFA6AB9A)
)

val DarkPalette = HidayahPalette(
    isDark = true,
    background = Color(0xFF0F1B15),
    card = Color(0xFF17271F),
    cardMuted = Color(0xFF1F3027),
    panelHighlight = Color(0xFF21362C),
    ink = Color(0xFFE8ECE2),
    sub = Color(0xFF92A693),
    line = Color(0xFF283A30),
    brand = Color(0xFF7CC19C),
    brandOnSurface = Color(0xFFDDE9DD),
    brandMist = Color(0xFF1F3A2F),
    topBar = Color(0xFF0E4D3C),
    navBar = Color(0xFF17271F),
    dotInactive = Color(0xFF55685C)
)

val LocalHidayahPalette = staticCompositionLocalOf { LightPalette }
