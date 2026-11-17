package com.hidayah.android.ui.components

// ============================================================
// আজকের আয়াত কার্ড — হোম ও হিজরি ক্যালেন্ডার উভয় স্ক্রিনে ব্যবহৃত।
// (হোম থেকে এখানে সরানো হয়েছে ২০২৬-১৬-১১; লজিক/লুক অপরিবর্তিত।)
// ============================================================

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.hidayah.android.data.quran.AyahOfDay
import com.hidayah.android.ui.theme.*

private val pal: HidayahPalette @Composable get() = LocalHidayahPalette.current

/**
 * @param s স্কেল ফ্যাক্টর (হোমের ক্ষেত্রে); ক্যালেন্ডারের মতো ফিক্সড-লেআউটে 1f
 * @param fixedCardHeight null হলে কার্ডটি বাকি জায়গা পূরণ করে (weighted Column-এ);
 *                       স্ক্রলেবল কনটেক্সটে ফিক্সড উচ্চতা দিতে হবে (যেমন 250.dp)
 */
@Composable
fun AyahOfDayCard(
    ayah: AyahOfDay,
    s: Float,
    modifier: Modifier = Modifier,
    fixedCardHeight: Dp? = null
) {
    val sp: (Float) -> androidx.compose.ui.unit.TextUnit = { (it * s).sp }
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val dpv = { v: Float -> (v * s).dp }

    val shareText = remember(ayah) {
        "${ayah.arabic}\n\n${ayah.bengali}\n\n${ayah.reference}\n\n— হিদায়াহ (আজকের আয়াত)"
    }

    fun shareImage() {
        try {
            val file = AyahShareImage.create(context, ayah)
            val uri = FileProvider.getUriForFile(context, "com.hidayah.android.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                // শুধু ছবি — কোনো লেখা (EXTRA_TEXT) যাবে না
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "আয়াত শেয়ার করুন"))
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "শেয়ার করা যায়নি", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun copyText() {
        clipboard.setText(androidx.compose.ui.text.AnnotatedString(shareText))
        android.widget.Toast.makeText(context, "টেক্সট কপি হয়েছে", android.widget.Toast.LENGTH_SHORT).show()
    }

    // ---------- সেকশন হেডার — টগল/শেয়ার বোতাম কার্ডের বাইরে ----------
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = dpv(2f))
                .padding(bottom = dpv(6f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "আজকের আয়াত",
                style = HidayahType.TitleSans.copy(fontSize = sp(15f)),
                color = pal.ink,
                modifier = Modifier.weight(1f)
            )
            // টেক্সট কপি — নীরব আইকন বৃত্ত
            Box(
                Modifier
                    .size(dpv(30f))
                    .clip(CircleShape)
                    .border(1.dp, pal.line, CircleShape)
                    .clickable(onClick = ::copyText),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = "টেক্সট কপি", tint = pal.sub, modifier = Modifier.size(dpv(14f)))
            }
            Spacer(Modifier.width(dpv(8f)))
            // শেয়ার — সোনালি পিল (শুধু ছবি যাবে)
            Row(
                Modifier
                    .clip(HidayahShapes.Pill)
                    .background(Brush.linearGradient(listOf(Gold, Color(0xFFC9A227))))
                    .clickable(onClick = ::shareImage)
                    .padding(horizontal = dpv(13f), vertical = dpv(7f)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Share, contentDescription = "শেয়ার", tint = EmeraldDark, modifier = Modifier.size(dpv(14f)))
                Spacer(Modifier.width(dpv(6f)))
                Text(
                    "শেয়ার",
                    style = HidayahType.BodyMedium.copy(fontSize = sp(12f), fontWeight = FontWeight.Bold),
                    color = EmeraldDark
                )
            }
        }

        Card(
            shape = HidayahShapes.Large,
            colors = CardDefaults.cardColors(containerColor = EmeraldDeep),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = (if (fixedCardHeight != null) Modifier.height(fixedCardHeight) else Modifier.weight(1f)).fillMaxWidth()
        ) {
        Box(Modifier.fillMaxSize()) {
            AyahCardArtwork(Modifier.fillMaxSize())

            // ---------- content (artwork + kicker + verse + brand) ----------
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = dpv(18f), vertical = dpv(9f)),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "বিসমিল্লাহির রাহমানির রাহিম",
                    style = HidayahType.Caption.copy(fontSize = sp(11f), fontWeight = FontWeight.Medium),
                    color = Gold.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(dpv(4f)))
                Text(
                    ayah.arabic,
                    style = HidayahType.ArabicLarge.copy(fontSize = sp(22.5f), lineHeight = (34 * s).sp),
                    color = Cream,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(dpv(3f)))
                Text(
                    ayah.bengali,
                    style = HidayahType.Body.copy(
                        fontFamily = NotoSerifBengali,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = sp(13f),
                        lineHeight = (21 * s).sp
                    ),
                    color = Color(0xFFD7E7DA),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(dpv(5f)))
                Text(
                    ayah.reference,
                    style = HidayahType.Caption.copy(
                        fontFamily = NotoSerifBengali,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = sp(11.5f)
                    ),
                    color = GoldSoft,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(dpv(5f)))
                Text(
                    "হিদায়াহ",
                    style = HidayahType.TitleSans.copy(fontSize = sp(14.5f), fontWeight = FontWeight.SemiBold, fontFamily = NotoSerifBengali),
                    color = Gold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "H I D A Y A H",
                    style = HidayahType.Overline.copy(color = Gold.copy(alpha = 0.75f), fontSize = sp(7.5f), letterSpacing = 3.2.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(dpv(4f)))
                Text(
                    "© iNWEB Official",
                    style = HidayahType.Overline.copy(fontSize = sp(7f), letterSpacing = 1.6.sp, fontWeight = FontWeight.Medium),
                    color = Gold.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

        }
        }
    }
}

/** Vector artwork for the ayah card — mirrors iNWEB-DOCS/Hidayah-Android → design/ayah-card-background.svg (no raster images). */
@Composable
private fun AyahCardArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        // deep-emerald vertical gradient
        drawRect(Brush.verticalGradient(listOf(Color(0xFF123F31), Color(0xFF0E3325), Color(0xFF08251B))))
        // soft radial gold glow, top-center
        val glowR = size.width * 0.75f
        drawCircle(
            Brush.radialGradient(
                colors = listOf(Color(0x33D4AF37), Color(0x00000000)),
                center = Offset(size.width / 2f, -size.height * 0.35f),
                radius = glowR
            ),
            radius = glowR,
            center = Offset(size.width / 2f, -size.height * 0.35f)
        )
        // faint 8-pointed star lattice
        val cell = 56f * density
        val starR = 9f * density
        val starColor = Color(0xFFD4AF37).copy(alpha = 0.055f)
        var y = cell / 2
        var row = 0
        while (y < size.height) {
            val xOffset = if (row % 2 == 0) 0f else cell / 2f
            var x = xOffset
            while (x < size.width) {
                eightPointedStar(Offset(x, y), starR, starColor)
                x += cell
            }
            y += cell * 0.86f
            row++
        }
        // large central medallion star (very faint)
        eightPointedStar(Offset(size.width / 2f, size.height / 2f), size.height * 0.62f, Color(0xFFD4AF37).copy(alpha = 0.05f))
        // double gold frame
        val frame = Color(0xFFD4AF37)
        drawRoundRect(frame, topLeft = Offset(7f * density, 7f * density), size = Size(size.width - 14f * density, size.height - 14f * density), cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f * density), style = Stroke(1.6f * density))
        drawRoundRect(frame.copy(alpha = 0.65f), topLeft = Offset(12.5f * density, 12.5f * density), size = Size(size.width - 25f * density, size.height - 25f * density), cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f * density), style = Stroke(0.8f * density))
        // corner ornaments — small stars
        val corners = listOf(
            Offset(21f * density, 21f * density),
            Offset(size.width - 21f * density, 21f * density),
            Offset(21f * density, size.height - 21f * density),
            Offset(size.width - 21f * density, size.height - 21f * density)
        )
        corners.forEach { c -> eightPointedStar(c, 4.6f * density, frame.copy(alpha = 0.9f)) }
    }
}
