package com.hidayah.android.ui.screens.about

// ============================================================
// অ্যাপ সম্পর্কে — মেনুর ঐচ্ছিক এন্ট্রি।
// উন্মুক্ত: উৎস ও হিসাব / সংস্করণ। লকড (🔒): প্রাইভেসি পলিসি, লাইসেন্স,
// সমস্যা রিপোর্ট, GitHub — একে একে সম্পূর্ণ করা হবে।
// ডিজাইন: হোম/মেনুর মতো ব্র্যান্ড এম্যারাল্ড + গোল্ড — সব থিমে।
// ============================================================

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hidayah.android.BuildConfig
import com.hidayah.android.ui.components.hidayahStarLattice
import com.hidayah.android.ui.theme.*

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    // তৈরি হয়নি — শুধু 🔒 টোস্ট
    val lockedClick: () -> Unit = {
        android.widget.Toast.makeText(context, "🔒", android.widget.Toast.LENGTH_SHORT).show()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF11493A), Color(0xFF0D3A2C), Color(0xFF082A20), Color(0xFF041A12))
                )
            )
            .hidayahStarLattice()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 4.dp, bottom = 18.dp)
        ) {
            // ---------- টপ বার: ব্যাক + শিরোনাম ----------
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "ফিরে যান",
                        tint = Cream
                    )
                }
                Text(
                    "অ্যাপ সম্পর্কে",
                    style = HidayahType.Display.copy(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),
                    color = Cream
                )
            }
            Box(
                Modifier
                    .padding(start = 56.dp, top = 2.dp)
                    .height(2.dp)
                    .width(56.dp)
                    .clip(HidayahShapes.Pill)
                    .background(Gold)
            )

            Spacer(Modifier.height(20.dp))

            // ---------- হিরো: অ্যাপ আইকন + নাম + সংস্করণ ----------
            Row(verticalAlignment = Alignment.CenterVertically) {
                // অ্যাপ-আইকন টাইল — ক্রীম বর্গ + গোল্ড চন্দ্র-তারা
                Box(
                    Modifier
                        .size(84.dp)
                        .clip(HidayahShapes.Medium)
                        .background(Brush.linearGradient(listOf(Color(0xFFFFFFFF).copy(alpha = 0.14f), Color(0xFFFFFFFF).copy(alpha = 0.05f))))
                        .border(1.5.dp, Gold.copy(alpha = 0.55f), HidayahShapes.Medium),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.size(52.dp)) {
                        val r = size.minDimension / 2f
                        val c = Offset(size.width / 2f, size.height / 2f)
                        val outer = Path().apply { addOval(Rect(c.x - r, c.y - r, c.x + r, c.y + r)) }
                        val cut = Path().apply { addOval(Rect(c.x - r + r * 0.42f, c.y - r * 0.82f, c.x + r + r * 0.42f, c.y + r * 0.82f)) }
                        drawPath(Path().apply { op(outer, cut, PathOperation.Difference) }, Gold)
                        // তারা
                        val sr = r * 0.24f
                        val star = Path()
                        for (i in 0 until 16) {
                            val angle = (Math.PI / 8) * i - Math.PI / 2
                            val rr = if (i % 2 == 0) sr else sr * 0.42f
                            val px = c.x + r * 0.52f + (rr * kotlin.math.cos(angle)).toFloat()
                            val py = c.y - r * 0.05f + (rr * kotlin.math.sin(angle)).toFloat()
                            if (i == 0) star.moveTo(px, py) else star.lineTo(px, py)
                        }
                        star.close()
                        drawPath(star, Gold)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "হিদায়াহ",
                        style = HidayahType.Display.copy(fontSize = 30.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold),
                        color = Cream
                    )
                    Text(
                        "সংস্করণ ${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})",
                        style = HidayahType.Caption,
                        color = Gold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "নামাজ • কুরআন • কিবলা • তসবিহ • দুয়া",
                        style = HidayahType.Caption.copy(fontSize = 11.sp),
                        color = OnEmMuted
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            // ---------- গ্রুপ ১: উৎস ও হিসাব (উন্মুক্ত) ----------
            SectionOverline("উৎস ও হিসাব")
            Spacer(Modifier.height(8.dp))
            AboutRow(
                icon = Icons.Filled.VerifiedUser,
                title = "উৎস ও সঠিকতা",
                subtitle = "নামাজের সময় MWL হিসাব (ফজর ১৮° / এশা ১৭°); হিজরি তারিখ উম্মুল-ক্বুরা ক্যালেন্ডার",
                locked = false
            )
            Spacer(Modifier.height(8.dp))
            AboutRow(
                icon = Icons.Filled.Info,
                title = "সংস্করণ ও বিল্ড",
                subtitle = "হিদায়াহ ${BuildConfig.VERSION_NAME} — build ${BuildConfig.VERSION_CODE}; © iNWEB Official",
                locked = false
            )

            Spacer(Modifier.height(20.dp))

            // ---------- গ্রুপ ২: আইনি ও গোপনীয়তা (🔒 অসম্পূর্ণ) ----------
            SectionOverline("আইনি ও গোপনীয়তা")
            Spacer(Modifier.height(8.dp))
            AboutRow(
                icon = Icons.Filled.PrivacyTip,
                title = "প্রাইভেসি পলিসি",
                subtitle = "কোন ডেটা নেই, কোথায় থাকে — শিগগিরই খুলবে",
                locked = true,
                onClick = lockedClick
            )
            Spacer(Modifier.height(8.dp))
            AboutRow(
                icon = Icons.Filled.Gavel,
                title = "ওপেন সোর্স লাইসেন্স",
                subtitle = "Amiri, Noto, Material Icons ও অন্যান্য",
                locked = true,
                onClick = lockedClick
            )

            Spacer(Modifier.height(20.dp))

            // ---------- গ্রুপ ৩: সহায়তা ও কমিউনিটি (🔒 অসম্পূর্ণ) ----------
            SectionOverline("সহায়তা ও কমিউনিটি")
            Spacer(Modifier.height(8.dp))
            AboutRow(
                icon = Icons.Filled.BugReport,
                title = "সমস্যা রিপোর্ট করুন",
                subtitle = "কি ভাঙল জানান — শিগগিরই খুলবে",
                locked = true,
                onClick = lockedClick
            )
            Spacer(Modifier.height(8.dp))
            AboutRow(
                icon = Icons.Filled.Code,
                title = "GitHub — সোর্স কোড",
                subtitle = "iNWEB-Official/Hidayah-Android",
                locked = true,
                onClick = lockedClick
            )

            Spacer(Modifier.height(26.dp))

            // ---------- ফুটার ----------
            Text(
                "بِسْمِ ٱللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                style = HidayahType.Arabic.copy(fontSize = 15.sp, lineHeight = 24.sp),
                color = Cream.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "© iNWEB Official — সর্বস্বত্ব সংরক্ষিত",
                style = HidayahType.Overline.copy(color = Gold.copy(alpha = 0.7f), fontSize = 10.sp, letterSpacing = 1.5.sp),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ---------------------------------------------------------------

@Composable
private fun SectionOverline(text: String) {
    Text(
        text,
        style = HidayahType.Overline.copy(color = Gold, fontSize = 11.sp, letterSpacing = 2.2.sp, fontWeight = FontWeight.SemiBold)
    )
}

@Composable
private fun AboutRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    locked: Boolean,
    onClick: (() -> Unit)? = null
) {
    val rowFill = Color(0xFFFFFFFF).copy(alpha = if (locked) 0.05f else 0.07f)
    val rowLine = Color(0xFFFFFFFF).copy(alpha = if (locked) 0.09f else 0.11f)
    val iconTint = if (locked) OnEmMuted.copy(alpha = 0.55f) else Gold
    val titleColor = if (locked) OnEmMuted.copy(alpha = 0.75f) else Cream
    val subColor = OnEmMuted.copy(alpha = if (locked) 0.5f else 0.85f)

    Surface(
        shape = HidayahShapes.Large,
        color = rowFill,
        border = BorderStroke(1.dp, rowLine),
        modifier = Modifier
            .fillMaxWidth()
            .clip(HidayahShapes.Large)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFFFFFFF).copy(alpha = 0.09f), Color(0xFFFFFFFF).copy(alpha = 0.03f))))
                    .border(1.dp, Gold.copy(alpha = if (locked) 0.18f else 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // লকড হলে সোজা গোল্ড লক, আনলক হলে ফিচার আইকন
                if (locked) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = "শীঘ্রই আসছে",
                        tint = Gold.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = HidayahType.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = titleColor
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = HidayahType.Caption.copy(fontSize = 11.sp, lineHeight = 15.sp),
                    color = subColor
                )
            }
        }
    }
}
