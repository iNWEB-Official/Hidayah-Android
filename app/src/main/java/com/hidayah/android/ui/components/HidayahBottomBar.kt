package com.hidayah.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import com.hidayah.android.R
import com.hidayah.android.ui.theme.*

private val pal: HidayahPalette @Composable get() = LocalHidayahPalette.current

data class TabSpec(val route: String, @StringRes val labelRes: Int, val iconKind: IconKind)

enum class IconKind { Home, Quran, Qibla, Tasbih, Settings }

val hidayahTabs = listOf(
    TabSpec("home", R.string.tab_home, IconKind.Home),
    TabSpec("quran", R.string.tab_quran, IconKind.Quran),
    TabSpec("qibla", R.string.tab_qibla, IconKind.Qibla),
    TabSpec("tasbih", R.string.tab_tasbih, IconKind.Tasbih),
    TabSpec("settings", R.string.tab_settings, IconKind.Settings)
)

/** Bottom navigation from mockup 01-home: cream bar, emerald active tab with a short gold top indicator. */
@Composable
fun HidayahBottomBar(currentRoute: String?, onTab: (String) -> Unit) {
    Surface(color = pal.navBar, shadowElevation = 12.dp) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().height(62.dp)) {
                hidayahTabs.forEach { tab ->
                    val selected = currentRoute == tab.route
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onTab(tab.route) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // short gold indicator on the top edge of the active tab
                        Box(
                            Modifier
                                .height(3.dp)
                                .width(30.dp)
                                .clip(HidayahShapes.Pill)
                                .background(if (selected) Gold else Color.Transparent)
                        )
                        Spacer(Modifier.height(7.dp))
                        TabIcon(tab.iconKind, tint = if (selected) pal.brand else pal.dotInactive, size = 22)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            stringResource(tab.labelRes),
                            fontSize = 11.5.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selected) pal.brand else pal.dotInactive,
                            style = HidayahType.Caption
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabIcon(kind: IconKind, tint: Color, size: Int) {
    when (kind) {
        IconKind.Home -> Icon(Icons.Filled.Home, contentDescription = null, tint = tint, modifier = Modifier.size(size.dp))
        IconKind.Quran -> Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = tint, modifier = Modifier.size(size.dp))
        IconKind.Qibla -> Icon(Icons.Filled.Explore, contentDescription = null, tint = tint, modifier = Modifier.size(size.dp))
        IconKind.Tasbih -> com.hidayah.android.ui.screens.home.TasbihBeadsIcon(tint = tint, modifier = Modifier.size(size.dp))
        IconKind.Settings -> Icon(Icons.Filled.Settings, contentDescription = null, tint = tint, modifier = Modifier.size(size.dp))
    }
}
