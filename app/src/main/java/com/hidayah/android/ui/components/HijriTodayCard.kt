package com.hidayah.android.ui.components

// ============================================================
// আজকের পঞ্জিকার কার্ড — হোম (হেডার) ও হিজরি ক্যালেন্ডার উভয় স্ক্রিনে ব্যবহৃত,
// হুবহু একই শেয়ারযোগ্য কার্ড; তেমনি share হলে CalendarShareImage-এর ছবি যাবে।
// ============================================================

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.hidayah.android.data.bengali.BnCalendar
import com.hidayah.android.data.hijri.HijriDates
import com.hidayah.android.data.hijri.IslamicSpecialDays
import com.hidayah.android.state.HidayahViewModel
import com.hidayah.android.ui.theme.*
import com.hidayah.android.util.BnDigits
import java.time.LocalDate

/**
 * @param s স্কেল-ফ্যাক্টর (হোমের হেডার; ক্যালেন্ডারে 1f)
 * @param onTodayClick null হলে কার্ড ক্লিকযোগ্য নয় (ক্যালেন্ডার স্ক্রিনে নিজেই আছে সেখানে)
 * @param onDark true হলে হোমের মতো স্বচ্ছ সাদা-গ্লাসি স্টাইল; false হলে ক্রিম পৃষ্ঠার ওপর
 *               গভীর-এম্যারাল্ড কার্ড (ক্যালেন্ডার স্ক্রিনে)
 */
@Composable
fun HijriTodayCard(
    viewModel: HidayahViewModel,
    s: Float = 1f,
    onTodayClick: (() -> Unit)? = null,
    onDark: Boolean = true,
    modifier: Modifier = Modifier
) {
    val dp: (Float) -> Dp = { (it * s).dp }
    val sp: (Float) -> androidx.compose.ui.unit.TextUnit = { (it * s).sp }
    val localContext = LocalContext.current
    val cardColor = if (onDark) Color(0xFFFFFFFF).copy(alpha = 0.07f) else EmeraldDeep
    val cardBorder = if (onDark) Color(0xFFFFFFFF).copy(alpha = 0.10f) else Gold.copy(alpha = 0.35f)
    Surface(
        shape = HidayahShapes.Large,
        color = cardColor,
        border = BorderStroke(1.dp, cardBorder),
        modifier = modifier.then(if (onTodayClick != null) Modifier.clickable(onClick = onTodayClick) else Modifier)
    ) {
        val cardToday = LocalDate.now()
        val ramadanStatus = remember { IslamicSpecialDays.ramadanStatus(cardToday) }
        val (specialDay, specialDaysLeft) = remember { IslamicSpecialDays.nearestSpecialDay(cardToday) }
        val isFirstRamadan = run {
            val (_, hhmm, _) = HijriDates.gregorianToHijri(cardToday)
            hhmm == 9 && HijriDates.gregorianToHijri(cardToday).third == 1
        }
        val ramadanPrefix = if (ramadanStatus.inRamadan) "ঈদ আসতে আর মাত্র " else "রমজান শুরু হতে আর মাত্র "
        val ramadanLine = withBigDigits(ramadanPrefix + BnDigits.of(ramadanStatus.days.toInt()) + " দিন বাকি আছে।", 11f, Gold, 17f, GoldSoft)
        val specialLine = if (specialDaysLeft == 0L) {
            withBigDigits("নিকটতম বিশেষ দিন: ${specialDay.label} — আজ!", 11f, Cream.copy(alpha = 0.92f), 11f, GoldSoft)
        } else {
            withBigDigits("নিকটতম বিশেষ দিন: ${specialDay.label} — আর মাত্র ${BnDigits.of(specialDaysLeft.toInt())} দিন পরে।", 11f, Cream.copy(alpha = 0.92f), 16f, GoldSoft)
        }
        fun shareCard() {
            try {
                val file = com.hidayah.android.ui.components.CalendarShareImage.create(localContext, cardToday)
                val uri = androidx.core.content.FileProvider.getUriForFile(localContext, "com.hidayah.android.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    // শুধু ছবি — কোনো লেখা যাবে না
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                localContext.startActivity(Intent.createChooser(intent, "পঞ্জিকার কার্ড শেয়ার করুন"))
            } catch (e: Exception) {
                android.widget.Toast.makeText(localContext, "শেয়ার করা যায়নি", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = dp(14f), vertical = dp(9f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("বিসমিল্লাহির রাহমানির রাহিম", style = HidayahType.Caption.copy(fontSize = sp(11.5f), fontWeight = FontWeight.Medium), color = Gold.copy(alpha = 0.9f))
                Text(withBigDigits(HijriDates.gregLabelBn(cardToday), 14.5f, Cream, 19f, GoldSoft), style = HidayahType.BodyMedium, textAlign = TextAlign.Center)
                Text(withBigDigits(viewModel.hijriTodayLabel(), 12f, Gold, 16.5f, GoldSoft), style = HidayahType.Caption, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 2.dp))
                Text(withBigDigits(BnCalendar.labelBn(cardToday), 12f, Gold.copy(alpha = 0.85f), 16.5f, GoldSoft), style = HidayahType.Caption, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 1.dp))
                if (isFirstRamadan) {
                    // রমজানের প্রথম দিন — বড় সবুজ হাইলাইট
                    Text("রমাদান মোবারক", style = HidayahType.TitleSans.copy(fontSize = sp(22f), fontWeight = FontWeight.Bold), color = Color(0xFF4ADE80), textAlign = TextAlign.Center, modifier = Modifier.padding(top = dp(7f)))
                } else {
                    Row(
                        Modifier.padding(top = dp(7f)),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CrescentGlyph(modifier = Modifier.size(dp(12f)))
                        Spacer(Modifier.width(dp(5f)))
                        Text(ramadanLine, style = HidayahType.Caption, textAlign = TextAlign.Center)
                    }
                }
                Row(
                    Modifier.padding(top = dp(3f)),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StarGlyph(modifier = Modifier.size(dp(10.5f)))
                    Spacer(Modifier.width(dp(5f)))
                    Text(specialLine, style = HidayahType.Caption, textAlign = TextAlign.Center, maxLines = 2)
                }
            }
            Spacer(Modifier.width(dp(8f)))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                GoldSparkle(Modifier.size(dp(19f)))
                Spacer(Modifier.height(dp(5f)))
                // শেয়ার — শুধু ছবি
                Icon(
                    Icons.Filled.Share,
                    contentDescription = "কার্ড শেয়ার করুন",
                    tint = EmeraldDark,
                    modifier = Modifier
                        .size(dp(21f))
                        .clip(CircleShape)
                        .background(Gold)
                        .clickable(onClick = ::shareCard)
                        .padding(dp(3.5f))
                )
            }
        }
    }
}

/** কার্ডের সকল সংখ্যা-রান বড় ও হাইলাইট সোনালি (GoldSoft); বাকি লেখা স্বাভাবিক রঙে। */
private fun withBigDigits(text: String, baseSize: Float, baseColor: Color, bigSize: Float, bigColor: Color): androidx.compose.ui.text.AnnotatedString {
    val digitRe = Regex("[০-৯]+|[0-9]+")
    return androidx.compose.ui.text.buildAnnotatedString {
        var last = 0
        for (m in digitRe.findAll(text)) {
            if (m.range.first > last) {
                withStyle(SpanStyle(color = baseColor, fontSize = (baseSize * 1f).sp)) { append(text.substring(last, m.range.first)) }
            }
            withStyle(SpanStyle(color = bigColor, fontSize = bigSize.sp, fontWeight = FontWeight.Bold)) { append(m.value) }
            last = m.range.last + 1
        }
        if (last < text.length) {
            withStyle(SpanStyle(color = baseColor, fontSize = baseSize.sp)) { append(text.substring(last)) }
        }
    }
}

@Composable
private fun GoldSparkle(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension * 0.42f
        val inner = r * 0.34f
        val path = Path()
        for (i in 0 until 16) {
            val angle = (Math.PI / 8) * i - Math.PI / 2
            val rr = if (i % 2 == 0) r else inner
            val px = c.x + (rr * kotlin.math.cos(angle)).toFloat()
            val py = c.y + (rr * kotlin.math.sin(angle)).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        drawPath(path, color = Gold)
        drawCircle(color = Color(0xFF093A2C), radius = r * 0.18f, center = c)
    }
}

@Composable
private fun CrescentGlyph(modifier: Modifier = Modifier, tint: Color = Gold) {
    Canvas(modifier) {
        val r = size.minDimension / 2f
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outer = Path().apply { addOval(androidx.compose.ui.geometry.Rect(cx - r, cy - r, cx + r, cy + r)) }
        val cut = Path().apply { addOval(androidx.compose.ui.geometry.Rect(cx - r + r * 0.42f, cy - r * 0.82f, cx + r + r * 0.42f, cy + r * 0.82f)) }
        drawPath(Path().apply { op(outer, cut, PathOperation.Difference) }, tint)
    }
}

@Composable
private fun StarGlyph(modifier: Modifier = Modifier, tint: Color = Gold) {
    Canvas(modifier) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension * 0.5f
        val inner = r * 0.44f
        val path = Path()
        for (i in 0 until 16) {
            val angle = (Math.PI / 8.0) * i - Math.PI / 2.0
            val rr = if (i % 2 == 0) r else inner
            val px = c.x + (rr * kotlin.math.cos(angle)).toFloat()
            val py = c.y + (rr * kotlin.math.sin(angle)).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        drawPath(path, tint)
    }
}

