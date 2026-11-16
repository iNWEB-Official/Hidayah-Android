package com.hidayah.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hidayah.android.ui.theme.*

/**
 * Mockup-style in-content title for the bottom-nav tab screens (mockups 01–08):
 * big emerald serif title + muted caption, transparent background — NOT an action bar.
 * Edge-to-edge: caller should pass [Modifier.statusBarsPadding].
 */
@Composable
fun ScreenTitle(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    centered: Boolean = false,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    val pal = LocalHidayahPalette.current
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (centered) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, style = HidayahType.Display, color = pal.brand)
                subtitle?.let { Text(it, style = HidayahType.Caption, color = pal.sub) }
            }
        } else {
            Column(Modifier.weight(1f)) {
                Text(title, style = HidayahType.Display, color = pal.brand)
                subtitle?.let { Text(it, style = HidayahType.Caption, color = pal.sub) }
            }
            Row(verticalAlignment = Alignment.CenterVertically) { trailing() }
        }
    }
}

/**
 * Emerald star-lattice hero header for pushed sub-pages (mockups 06 "দোয়া ও যিকির"
 * and 07 "হিজরী ক্যালেন্ডার"): rounded bottom corners, back arrow, Cream title +
 * muted-gold caption, optional trailing content (e.g. "আজ" pill / favorites toggle).
 */
@Composable
fun EmeraldHeroHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF11493A), Color(0xFF0D3A2C), Color(0xFF082A20))
                )
            )
            .hidayahStarLattice()
            .statusBarsPadding()
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "পিছনে", tint = Cream)
                }
            } else {
                Spacer(Modifier.width(10.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = HidayahType.H1, color = Cream)
                subtitle?.let { Text(it, style = HidayahType.Caption, color = OnEmMuted) }
            }
            Row(
                Modifier.padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) { trailing() }
        }
    }
}
