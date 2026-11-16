package com.hidayah.android.ui.screens.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hidayah.android.ui.components.HidayahBackground
import com.hidayah.android.ui.components.HidayahEmblem
import com.hidayah.android.ui.theme.*

private val pal: HidayahPalette @Composable get() = LocalHidayahPalette.current

/**
 * Screen 00-B — the full Hidayah Design System sheet from mockup 00
 * (00-cover-design-system.svg), pixel-faithful: cover, palette, typography, screen index.
 * Reachable from Settings → ডিজাইন সিস্টেম.
 */
@Composable
fun DesignSystemScreen(onBack: () -> Unit) {
    HidayahBackground {
        Box(Modifier.fillMaxSize()) {
            IconButton(onClick = onBack, modifier = Modifier.statusBarsPadding().padding(8.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "পিছনে", tint = Gold)
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .padding(horizontal = 22.dp)
                    .padding(top = 44.dp, bottom = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HidayahEmblem(Modifier.size(108.dp))
                Spacer(Modifier.height(16.dp))
                Text("হিদায়াহ", style = HidayahType.Display.copy(fontSize = 42.sp, lineHeight = 52.sp), color = Gold)
                Spacer(Modifier.height(4.dp))
                Text("H I D A Y A H", style = HidayahType.Overline.copy(letterSpacing = 8.sp, fontSize = 12.sp), color = GoldSoft)
                Spacer(Modifier.height(8.dp))
                Text("হিদায়াতের পথে আপনার সঙ্গী", style = HidayahType.Caption.copy(fontSize = 14.sp), color = Cream)
                Spacer(Modifier.height(14.dp))
                UiMockupPill()
                Spacer(Modifier.height(18.dp))
                StarDivider(width = 300.dp)
                Spacer(Modifier.height(18.dp))

                SectionTitle("রঙ প্যালেট — COLORS")
                Spacer(Modifier.height(14.dp))
                ColorPaletteRow()
                Spacer(Modifier.height(18.dp))
                StarDivider(width = 330.dp, alpha = 0.7f)
                Spacer(Modifier.height(18.dp))

                SectionTitle("অক্ষর — TYPOGRAPHY")
                Spacer(Modifier.height(14.dp))
                TypographyRows()
                Spacer(Modifier.height(18.dp))
                StarDivider(width = 330.dp, alpha = 0.7f)
                Spacer(Modifier.height(18.dp))

                SectionTitle("স্ক্রিন — SCREENS (৮টি)")
                Spacer(Modifier.height(12.dp))
                ScreensIndex()
                Spacer(Modifier.height(26.dp))
                Text(
                    "HIDAYAH DESIGN SYSTEM •  v1.0",
                    style = HidayahType.Overline.copy(letterSpacing = 2.5.sp, fontSize = 10.sp),
                    color = Color(0xFF8FA8A0)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = HidayahType.Overline.copy(letterSpacing = 2.sp, fontSize = 11.sp),
        color = Gold,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun UiMockupPill() {
    androidx.compose.material3.Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(percent = 50),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.4.dp, Gold)
    ) {
        Text(
            "UI MOCKUP",
            style = HidayahType.Overline.copy(letterSpacing = 3.5.sp, fontSize = 11.sp),
            color = Gold,
            modifier = Modifier.padding(horizontal = 26.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun StarDivider(width: androidx.compose.ui.unit.Dp, alpha: Float = 1f) {
    Row(Modifier.width(width), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).height(1.2.dp).background(Gold.copy(alpha = 0.85f * alpha)))
        Spacer(Modifier.width(10.dp))
        androidx.compose.foundation.Canvas(Modifier.size(13.dp)) {
            val c = center
            val path = Path().apply {
                moveTo(c.x, c.y - 6.5f)
                lineTo(c.x + 1.6f, c.y - 1.6f)
                lineTo(c.x + 6.5f, c.y)
                lineTo(c.x + 1.6f, c.y + 1.6f)
                lineTo(c.x, c.y + 6.5f)
                lineTo(c.x - 1.6f, c.y + 1.6f)
                lineTo(c.x - 6.5f, c.y)
                lineTo(c.x - 1.6f, c.y - 1.6f)
                close()
            }
            drawPath(path, Color(0xFFD4AF37).copy(alpha = alpha))
        }
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f).height(1.2.dp).background(Gold.copy(alpha = 0.85f * alpha)))
    }
}

@Composable
private fun ColorPaletteRow() {
    val swatches = listOf(
        Triple("এমারেল্ড", "#0E4D3C", Color(0xFF0E4D3C)),
        Triple("গাঢ় সবুজ", "#062A20", Color(0xFF062A20)),
        Triple("সোনা", "#D4AF37", Color(0xFFD4AF37)),
        Triple("ক্রিম", "#FBF8ED", Color(0xFFFBF8ED)),
        Triple("কালি", "#1E2A23", Color(0xFF1E2A23)),
        Triple("ধূসর", "#67766D", Color(0xFF67766D)),
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        swatches.forEach { (label, hex, color) ->
            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(color)
                )
                Spacer(Modifier.height(6.dp))
                Text(label, style = HidayahType.Caption.copy(fontSize = 9.5.sp, lineHeight = 13.sp), color = Cream, textAlign = TextAlign.Center)
                Text(hex, style = HidayahType.Caption.copy(fontSize = 8.5.sp, lineHeight = 11.sp), color = Color(0xFF8FA8A0), textAlign = TextAlign.Center)
            }
        }
    }
}

private data class TypeRow(
    val sample: String,
    val sampleFamily: androidx.compose.ui.text.font.FontFamily,
    val fontName: String,
    val description: String,
    val sampleSize: Float
)

@Composable
private fun TypographyRows() {
    val rows = listOf(
        TypeRow("যোহরের ওয়াক্ত", NotoSerifBengali, "Noto Serif Bengali", "শিরোনাম ও নামাজের ওয়াক্ত", 20f),
        TypeRow("আসসালামু আলাইকুম", NotoSansBengali, "Noto Sans Bengali", "বডি টেক্সট ও লেবেল", 20f),
        TypeRow("فَإِنَّ مَعَ الْعُسْرِ يُسْرًا", Amiri, "Amiri — Naskh", "কুরআন ও দোয়ার আরবি", 23f),
    )
    Column(Modifier.fillMaxWidth()) {
        rows.forEach { row ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    row.sample,
                    style = HidayahType.H2.copy(fontFamily = row.sampleFamily, fontWeight = FontWeight.SemiBold, fontSize = row.sampleSize.sp, lineHeight = 34.sp),
                    color = Color(0xFFE8EFE9),
                    modifier = Modifier.weight(1.15f)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(row.fontName, style = HidayahType.Caption.copy(fontSize = 12.sp), color = Cream)
                    Text(row.description, style = HidayahType.Caption.copy(fontSize = 10.sp, lineHeight = 15.sp), color = Color(0xFF8FA8A0))
                }
            }
        }
    }
}

@Composable
private fun ScreensIndex() {
    val items = listOf(
        "01" to "হোম — নামাজের সময়",
        "02" to "কুরআন — সূরা তালিকা",
        "03" to "কুরআন — তিলাওয়াত",
        "04" to "কিবলা কম্পাস",
        "05" to "তসবিহ কাউন্টার",
        "06" to "দোয়া ও যিকির",
        "07" to "হিজরি ক্যালেন্ডার",
        "08" to "সেটিংস",
    )
    Column(Modifier.fillMaxWidth()) {
        items.chunked(2).forEach { rowItems ->
            Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                rowItems.forEach { (num, label) ->
                    Row(
                        Modifier.weight(1f).padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(num, style = HidayahType.TitleSans.copy(fontSize = 13.sp), color = Gold)
                        Spacer(Modifier.width(8.dp))
                        Text(label, style = HidayahType.Body.copy(fontSize = 12.5.sp, lineHeight = 17.sp), color = Cream)
                    }
                }
                // pad if odd item count
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
