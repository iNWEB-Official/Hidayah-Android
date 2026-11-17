package com.hidayah.android.ui.screens.menu

// ============================================================
// মেনু পেজ — নাভবারের ৫ম ট্যাব (সেটিংসের জায়গায়)।
// এই মুহূর্তে শুধু মুল ডিজাইন: পেজগুলো ধাপে ধাপে সম্পূর্ণ করা হবে।
// তৈরি হয়নি এমন ফিচারে 🔒 লক; ট্যাপ করলে 🔒 টোস্ট।
// ব্যাকগ্রাউন্ড: হোমের মতো এম্যারাল্ড গ্রেডিয়েন্ট + গোল্ড তারার জাল — সব থিমে ব্র্যান্ড-লকড।
// ============================================================

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hidayah.android.ui.components.hidayahStarLattice
import com.hidayah.android.ui.theme.*

private data class MenuItem(
    val label: String,
    val locked: Boolean,
    val onClick: () -> Unit,
    val icon: @Composable (tint: Color) -> Unit
)

@Composable
fun MenuScreen(
    onOpenSettings: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenDuas: () -> Unit,
    onOpenProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    // তৈরি হয়নি — শুধু 🔒 টোস্ট (হোমের coming-soon বাটনের মতো)
    val lockedClick: () -> Unit = {
        android.widget.Toast.makeText(context, "🔒", android.widget.Toast.LENGTH_SHORT).show()
    }

    val appItems = listOf(
        MenuItem("সেটিংস", locked = false, onClick = onOpenSettings) { tint ->
            Icon(Icons.Filled.Settings, contentDescription = null, tint = tint, modifier = Modifier.size(30.dp))
        },
        MenuItem("হিজরি ক্যালেন্ডার", locked = false, onClick = onOpenCalendar) { tint ->
            Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = tint, modifier = Modifier.size(30.dp))
        },
        MenuItem("দোয়া", locked = false, onClick = onOpenDuas) { tint ->
            Text("دُعَا", style = HidayahType.Arabic.copy(fontSize = 22.sp, color = tint), textAlign = TextAlign.Center)
        },
        MenuItem("প্রোফাইল", locked = false, onClick = onOpenProfile) { tint ->
            Icon(Icons.Filled.Person, contentDescription = null, tint = tint, modifier = Modifier.size(30.dp))
        }
    )

    val lockedItems = listOf(
        MenuItem("যাকাত", locked = true, onClick = lockedClick) { tint ->
            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
        },
        MenuItem("তাফসীর", locked = true, onClick = lockedClick) { tint ->
            Icon(Icons.Filled.AutoStories, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
        },
        MenuItem("প্রশ্নোত্তর", locked = true, onClick = lockedClick) { tint ->
            Icon(Icons.Filled.HelpOutline, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
        },
        MenuItem("হজ-উমরা", locked = true, onClick = lockedClick) { tint ->
            KaabaGlyph(Modifier.size(28.dp), tint = tint)
        },
        MenuItem("মাসজিদ", locked = true, onClick = lockedClick) { tint ->
            MosqueGlyph(Modifier.size(28.dp), tint = tint, doorAlpha = 0.3f)
        }
    )

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
                .padding(top = 6.dp, bottom = 18.dp)
        ) {
            // ---------- হেডার ----------
            Text(
                "মেনু",
                style = HidayahType.Display.copy(fontSize = 26.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
                color = Cream
            )
            Box(
                Modifier
                    .padding(top = 8.dp)
                    .height(2.dp)
                    .width(56.dp)
                    .clip(HidayahShapes.Pill)
                    .background(Gold)
            )

            Spacer(Modifier.height(16.dp))

            // ---------- সেকশন: অ্যাপ ----------
            SectionOverline("অ্যাপ")
            Spacer(Modifier.height(10.dp))
            MenuGrid(appItems)

            Spacer(Modifier.height(20.dp))

            // ---------- সেকশন: শীঘ্রই আসছে ----------
            SectionOverline("শীঘ্রই আসছে")
            Spacer(Modifier.height(10.dp))
            MenuGrid(lockedItems)
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

/** ২-কলাম গ্রিড — হোমের কুইক-অ্যাকশন কার্ডগুলির পূর্ণ-পেজ সংস্করণ। */
@Composable
private fun MenuGrid(items: List<MenuItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        items.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                row.forEach { item ->
                    MenuGridCell(item, Modifier.weight(1f))
                }
                // বিজোড় আইটেম থাকলে শূন্যস্থান দিয়ে সারি পূর্ণ
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MenuGridCell(item: MenuItem, modifier: Modifier = Modifier) {
    val locked = item.locked
    // লকড: ম্লান; উন্মুক্ত: গোল্ড আউটলাইন বৃত্ত + উজ্জ্বল লেবেল
    val cellFill = Color(0xFFFFFFFF).copy(alpha = if (locked) 0.05f else 0.07f)
    val cellLine = Color(0xFFFFFFFF).copy(alpha = if (locked) 0.09f else 0.11f)
    val circleLine = if (locked) Color(0xFFFFFFFF).copy(alpha = 0.12f) else Gold.copy(alpha = 0.45f)
    val iconTint = if (locked) OnEmMuted.copy(alpha = 0.55f) else Gold
    val labelColor = if (locked) OnEmMuted.copy(alpha = 0.6f) else Cream

    Surface(
        shape = HidayahShapes.Large,
        color = cellFill,
        border = BorderStroke(1.dp, cellLine),
        modifier = modifier
            .height(126.dp)
            .clip(HidayahShapes.Large)
            .clickable(onClick = item.onClick)
    ) {
        Box(Modifier.fillMaxSize().padding(vertical = 13.dp, horizontal = 6.dp)) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // আইকন বৃত্ত (+ লকড হলে গোল্ড 🔒 ব্যাজ)
                Box {
                    Box(
                        Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        if (locked) Color(0xFFFFFFFF).copy(alpha = 0.06f) else Color(0xFFFFFFFF).copy(alpha = 0.14f),
                                        if (locked) Color(0xFFFFFFFF).copy(alpha = 0.03f) else Color(0xFFFFFFFF).copy(alpha = 0.06f)
                                    )
                                )
                            )
                            .border(1.dp, circleLine, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        item.icon(iconTint)
                    }
                    if (locked) {
                        // হোমের আসন্ন-ফিচার ব্যাজের মতো — গোল্ড গোল + গাঢ় লক
                        Box(
                            Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 3.dp, y = (-3).dp)
                                .size(17.dp)
                                .clip(CircleShape)
                                .background(Gold)
                                .border(1.dp, EmeraldDark.copy(alpha = 0.45f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = "শীঘ্রই আসছে",
                                tint = EmeraldDark,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(9.dp))
                Text(
                    item.label,
                    style = HidayahType.BodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                    color = labelColor,
                    maxLines = 1
                )
            }
        }
    }
}

// ---------------------------------------------------------------
// কাস্টম-আঁকা glyph (হোম স্ক্রিনের সাথে একই পাথ; আমদানযোগ্য নয় বলে অনুরূপ অনুলিপি)
// ---------------------------------------------------------------

@Composable
private fun KaabaGlyph(modifier: Modifier = Modifier, tint: Color = Gold) {
    Canvas(modifier) {
        val u = size.minDimension / 24f
        drawRoundRect(
            tint,
            topLeft = Offset(4.2f * u, 6.0f * u),
            size = Size(15.6f * u, 12.8f * u),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f * u)
        )
        // কিসওয়া ব্যান্ড (গাঢ় পটভূমি-রঙের পট্টি)
        drawRect(
            Color(0xFF062A20),
            topLeft = Offset(4.2f * u, 10.7f * u),
            size = Size(15.6f * u, 2.6f * u)
        )
    }
}

@Composable
private fun MosqueGlyph(modifier: Modifier = Modifier, tint: Color = Gold, doorAlpha: Float = 0.9f) {
    Canvas(modifier) {
        val u = size.minDimension / 24f
        val cx = size.width / 2f
        val baseY = 19f * u
        val deep = tint
        drawLine(deep, Offset(cx, 2.2f * u), Offset(cx, 5.6f * u), 1.35f * u, cap = StrokeCap.Round)
        drawCircle(deep, 0.85f * u, Offset(cx, 2.0f * u))
        drawArc(deep, 180f, 180f, true, topLeft = Offset(cx - 6.2f * u, 11.5f * u - 6.2f * u), size = Size(12.4f * u, 12.4f * u))
        drawRoundRect(deep, topLeft = Offset(cx - 4.9f * u, 9.4f * u), size = Size(9.8f * u, 3.2f * u), cornerRadius = androidx.compose.ui.geometry.CornerRadius(0.6f * u))
        drawRoundRect(deep, topLeft = Offset(cx - 6.3f * u, 12.0f * u), size = Size(12.6f * u, baseY - 12.0f * u), cornerRadius = androidx.compose.ui.geometry.CornerRadius(0.9f * u))
        listOf(cx - 8.6f * u, cx + 6.9f * u).forEach { mx ->
            drawRoundRect(deep, topLeft = Offset(mx, 10.0f * u), size = Size(1.7f * u, baseY - 10.0f * u), cornerRadius = androidx.compose.ui.geometry.CornerRadius(0.5f * u))
            drawArc(deep, 180f, 180f, true, topLeft = Offset(mx - 0.4f * u, 10.0f * u - 2.1f * u), size = Size(2.5f * u, 2.5f * u))
        }
        // প্রবেশদ্বার — ক্রীম আর্ক (লকড অবস্থায় ম্লান)
        drawArc(Color(0xFFFBF8ED).copy(alpha = doorAlpha), 180f, 180f, true, topLeft = Offset(cx - 2.2f * u, baseY - 6.0f * u - 2.2f * u), size = Size(4.4f * u, 4.4f * u))
        drawRoundRect(Color(0xFFFBF8ED).copy(alpha = doorAlpha), topLeft = Offset(cx - 2.2f * u, baseY - 6.0f * u), size = Size(4.4f * u, 6.0f * u), cornerRadius = androidx.compose.ui.geometry.CornerRadius(0.4f * u))
    }
}
