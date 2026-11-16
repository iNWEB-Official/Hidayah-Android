package com.hidayah.android.ui.screens.tasbih

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hidayah.android.state.HidayahViewModel
import com.hidayah.android.ui.components.*
import com.hidayah.android.ui.theme.*
import com.hidayah.android.util.BnDigits

private val pal: HidayahPalette @Composable get() = LocalHidayahPalette.current

private val dhikrList = listOf(
    "সুবহানাল্লাহ", "আলহামদুলিল্লাহ", "আল্লাহু আকবার", "লা ইলাহা ইল্লাল্লাহ",
    "আস্তাগফিরুল্লাহ", "সুবহানাল্লাহি ওয়াবিহামদিহি", "সুবহানাল্লাহিল আযীম", "লা হাওলা ওয়ালা কুওয়াতা ইল্লা বিল্লাহ"
)

private val targetOptions = listOf(33, 100, 1000)

@Composable
fun TasbihScreen(viewModel: HidayahViewModel) {
    val settings by viewModel.settings.collectAsState()
    val haptics = LocalHapticFeedback.current
    var showTargetSheet by remember { mutableStateOf(false) }
    var completedDhikr by remember { mutableStateOf<String?>(null) }

    val progress = if (settings.tasbihTarget > 0)
        (settings.tasbihCount.toFloat() / settings.tasbihTarget).coerceIn(0f, 1f)
    else 0f

    Column(Modifier.fillMaxSize().background(pal.background)) {
        ScreenTitle(
            title = "তসবিহ",
            subtitle = "আঙুলের ছোঁয়ায় যিকির গণনা",
            centered = true,
            modifier = Modifier.statusBarsPadding().padding(top = 10.dp, bottom = 4.dp)
        )
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---------- Selection chips ----------
            SectionLabel("যিকির নির্বাচন করুন", Modifier.align(Alignment.Start))
            Spacer(Modifier.height(8.dp))
            FlowRow(dhikrList, selected = settings.tasbihSelected, onSelect = viewModel::selectTasbih)
            Spacer(Modifier.height(18.dp))

            // ---------- Big counter ----------
            Box(
                Modifier
                    .size(264.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Brush.radialGradient(listOf(pal.brandOnSurface, EmeraldDark)))
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        val newCount = settings.tasbihCount + 1
                        viewModel.incrementTasbih()
                        if (newCount == settings.tasbihTarget) completedDhikr = settings.tasbihSelected
                    },
                contentAlignment = Alignment.Center
            ) {
                // progress ring
                val trackColor = pal.brandMist
                androidx.compose.foundation.Canvas(Modifier.size(264.dp)) {
                    val stroke = 14f
                    val arcSize = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
                    val startAngle = -90f
                    // track
                    drawArc(
                        color = trackColor,
                        startAngle = 0f, sweepAngle = 360f, useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2),
                        size = arcSize, style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
                    )
                    // progress
                    drawArc(
                        color = Gold,
                        startAngle = startAngle, sweepAngle = progress * 360f, useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2),
                        size = arcSize, style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(settings.tasbihSelected, style = HidayahType.TitleSans, color = GoldSoft, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        BnDigits.of(settings.tasbihCount),
                        style = HidayahType.Display.copy(fontSize = 58.sp, lineHeight = 64.sp),
                        color = Cream
                    )
                    Text("/ ${BnDigits.of(settings.tasbihTarget)}", style = HidayahType.Caption, color = OnEmMuted)
                    Spacer(Modifier.height(8.dp))
                    Text("ট্যাপ করুন", style = HidayahType.Caption, color = Gold)
                }
            }
            Spacer(Modifier.height(18.dp))

            // ---------- Controls ----------
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalIconButton(onClick = { viewModel.resetTasbihCount() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "রিসেট", tint = pal.brand)
                }
                Spacer(Modifier.width(18.dp))
                Button(
                    onClick = { showTargetSheet = true },
                    shape = HidayahShapes.Pill,
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = Cream)
                ) {
                    Icon(Icons.Filled.Flag, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("লক্ষ্য: ${BnDigits.of(settings.tasbihTarget)}", style = HidayahType.BodyMedium)
                }
                Spacer(Modifier.width(18.dp))
                FilledTonalIconButton(onClick = { showTargetSheet = true }) {
                    Icon(Icons.Filled.Tune, contentDescription = "লক্ষ্য পরিবর্তন", tint = pal.brand)
                }
            }
            Spacer(Modifier.height(20.dp))

            // ---------- Today's total ----------
            Card(shape = HidayahShapes.Medium, colors = CardDefaults.cardColors(containerColor = pal.card)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.Filled.Today)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("আজকের মোট যিকির", style = HidayahType.Caption, color = pal.sub)
                        Text(BnDigits.of(settings.tasbihTodayTotal), style = HidayahType.H1, color = pal.brand)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    // ---------- Target selection sheet ----------
    if (showTargetSheet) {
        TargetSheet(
            currentTarget = settings.tasbihTarget,
            onSelect = { viewModel.setTasbihTarget(it); showTargetSheet = false },
            onDismiss = { showTargetSheet = false }
        )
    }

    // ---------- Completion dialog ----------
    completedDhikr?.let { dhikr ->
        AlertDialog(
            onDismissRequest = { completedDhikr = null; viewModel.resetTasbihCount() },
            containerColor = pal.card,
            title = { Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = GoldDeep)
                Spacer(Modifier.width(8.dp))
                Text("মাশাআল্লাহ!", style = HidayahType.H2, color = pal.brand)
            } },
            text = {
                Text(
                    "আপনি ${BnDigits.of(settings.tasbihTarget)} বার ‘$dhikr’ পড়েছেন। আল্লাহ আপনার যিকির কবুল করুন।",
                    style = HidayahType.Body, color = pal.ink
                )
            },
            confirmButton = {
                Button(
                    onClick = { completedDhikr = null; viewModel.resetTasbihCount() },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald, contentColor = Cream)
                ) { Text("নতুন সেট শুরু করি") }
            }
        )
    }
}

@Composable
private fun FlowRow(items: List<String>, selected: String, onSelect: (String) -> Unit) {
    // Simple wrapping rows of chips (no experimental foundation FlowRow dependency).
    Column(Modifier.fillMaxWidth()) {
        val rows = mutableListOf<MutableList<String>>()
        var current = mutableListOf<String>()
        var width = 0
        val maxUnits = 34 // rough character budget per row at this width
        for (item in items) {
            val cost = item.length + 4
            if (width + cost > maxUnits && current.isNotEmpty()) {
                rows.add(current); current = mutableListOf(); width = 0
            }
            current.add(item); width += cost
        }
        if (current.isNotEmpty()) rows.add(current)
        rows.forEach { row ->
            Row(Modifier.padding(vertical = 4.dp)) {
                row.forEach { item ->
                    SelectableChip(label = item, selected = item == selected, onClick = { onSelect(item) })
                    Spacer(Modifier.width(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TargetSheet(currentTarget: Int, onSelect: (Int) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = pal.card
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text("লক্ষ্য নির্ধারণ করুন", style = HidayahType.H2, color = pal.ink)
            Spacer(Modifier.height(4.dp))
            Text("প্রতি সেটের জন্য যিকিরের সংখ্যা", style = HidayahType.Caption, color = pal.sub)
            Spacer(Modifier.height(16.dp))
            targetOptions.forEach { target ->
                val selected = target == currentTarget
                Surface(
                    shape = HidayahShapes.Medium,
                    color = if (selected) Emerald else pal.background,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) Emerald else pal.line),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable { onSelect(target) }
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${BnDigits.of(target)} বার",
                            style = HidayahType.BodyMedium,
                            color = if (selected) Cream else pal.ink,
                            modifier = Modifier.weight(1f)
                        )
                        if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Gold)
                    }
                }
            }
        }
    }
}
