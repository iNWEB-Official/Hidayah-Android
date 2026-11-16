package com.hidayah.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hidayah.android.ui.theme.*

private val pal: HidayahPalette @Composable get() = LocalHidayahPalette.current

/** Small caption + gold divider used above section content. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, style = HidayahType.Overline, color = pal.sub, modifier = modifier)
}

/** Rounded gold-outline chip for selections. */
@Composable
fun SelectableChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = HidayahShapes.Pill,
        color = if (selected) Emerald else pal.card,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Emerald else pal.line),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            label,
            style = HidayahType.Caption,
            color = if (selected) Cream else pal.ink,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun GoldDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(2.dp)
            .width(64.dp)
            .clip(HidayahShapes.Pill)
            .background(Gold)
    )
}

@Composable
fun CircleNumber(number: Int, size: Int = 40) {
    Box(
        Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(pal.brandOnSurface),
        contentAlignment = Alignment.Center
    ) {
        Text(
            com.hidayah.android.util.BnDigits.of(number),
            style = HidayahType.BodyMedium.copy(color = Gold),
        )
    }
}

@Composable
fun IconBadge(icon: ImageVector, tint: androidx.compose.ui.graphics.Color = Gold) {
    Box(
        Modifier.size(40.dp).clip(CircleShape).background(pal.brandOnSurface),
        contentAlignment = Alignment.Center
    ) { Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp)) }
}
