package com.hidayah.android.ui.screens.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hidayah.android.data.quran.FatihaAyah
import com.hidayah.android.state.HidayahViewModel
import com.hidayah.android.ui.components.*
import com.hidayah.android.ui.theme.*
import com.hidayah.android.util.BnDigits

private val pal: HidayahPalette @Composable get() = LocalHidayahPalette.current

/**
 * Reader screen. Full text of Al-Fatiha ships in assets (fatiha.json) — every other
 * surah shows its ayah-by-ayah Arabic placeholder until the full text bundle is added,
 * clearly marked for the reader.
 */
@Composable
fun ReaderScreen(viewModel: HidayahViewModel, surahNumber: Int, onBack: () -> Unit, onOpenSurah: (Int) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val surah = remember(surahNumber) { viewModel.surahByNumber(surahNumber) }
    var arabicFontSize by remember { mutableFloatStateOf(26f) }
    var showTranslation by remember { mutableStateOf(true) }
    val settings by viewModel.settings.collectAsState()

    LaunchedEffect(surahNumber) {
        if (settings.lastReadSurah != surahNumber) viewModel.markRead(surahNumber, 1, 1)
    }

    val fatihaAyahs = remember(surahNumber) {
        if (surahNumber == 1) viewModel.fatihaAyahs else emptyList()
    }

    Column(Modifier.fillMaxSize().background(pal.card)) {
        // ---------- Top bar ----------
        Row(
            Modifier
                .fillMaxWidth()
                .background(Emerald)
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "পিছনে", tint = Cream)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(surah.nameBn, style = HidayahType.H2, color = Cream)
                Text(
                    "${surah.nameTransliteration} • ${BnDigits.of(surah.ayahCount)} আয়াত • ${surah.revelationPlace}",
                    style = HidayahType.Caption, color = OnEmMuted
                )
            }
            IconButton(onClick = { showTranslation = !showTranslation }) {
                Icon(
                    Icons.Filled.Translate,
                    contentDescription = if (showTranslation) "অনুবাদ লুকান" else "অনুবাদ দেখান",
                    tint = if (showTranslation) Gold else Cream
                )
            }
            IconButton(onClick = {
                viewModel.markRead(surahNumber, 1, surah.ayahCount)
                android.widget.Toast.makeText(context, "বুকমার্ক করা হয়েছে", android.widget.Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Filled.BookmarkAdd, contentDescription = "বুকমার্ক", tint = Cream)
            }
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            // ---------- Settings row: font size + reciter ----------
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = HidayahShapes.Pill, color = pal.background, border = androidx.compose.foundation.BorderStroke(1.dp, pal.line)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                        IconButton(onClick = { arabicFontSize = (arabicFontSize - 2f).coerceAtLeast(18f) }, modifier = Modifier.size(34.dp)) {
                            Text("অ-", style = HidayahType.BodyMedium, color = pal.ink)
                        }
                        Text("${BnDigits.of(arabicFontSize.toInt())}", style = HidayahType.Caption, color = pal.sub)
                        IconButton(onClick = { arabicFontSize = (arabicFontSize + 2f).coerceAtMost(40f) }, modifier = Modifier.size(34.dp)) {
                            Text("অ+", style = HidayahType.BodyMedium, color = pal.ink)
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Surface(shape = HidayahShapes.Pill, color = EmeraldDeep) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, tint = Gold, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("মিশারি আল-আফাসি", style = HidayahType.Caption, color = Cream)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))

            // ---------- Bismillah (not shown for at-Tawbah) ----------
            if (surahNumber != 9) {
                Text(
                    "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                    style = HidayahType.Arabic.copy(fontSize = (arabicFontSize * 0.9f).sp, lineHeight = (arabicFontSize * 1.7f).sp),
                    color = pal.brand,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                )
                GoldDivider(Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(10.dp))
            }

            // ---------- Verses ----------
            if (fatihaAyahs.isNotEmpty()) {
                fatihaAyahs.forEachIndexed { index, ayah ->
                    AyahBlock(
                        ayah = ayah,
                        arabicFontSize = arabicFontSize,
                        showTranslation = showTranslation
                    )
                    if (index < fatihaAyahs.lastIndex) GoldDivider(Modifier.padding(vertical = 4.dp).align(Alignment.CenterHorizontally))
                }
            } else {
                FullTextMissingNotice(surahName = surah.nameBn)
            }

            Spacer(Modifier.height(18.dp))
            // ---------- Prev / Next surah navigation ----------
            Row(Modifier.fillMaxWidth()) {
                if (surahNumber > 1) {
                    TextButton(onClick = { onOpenSurah(surahNumber - 1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = pal.sub)
                        Text("পূর্ববর্তী সূরা", style = HidayahType.Caption, color = pal.sub)
                    }
                }
                Spacer(Modifier.weight(1f))
                if (surahNumber < 114) {
                    TextButton(onClick = { onOpenSurah(surahNumber + 1) }) {
                        Text("পরবর্তী সূরা", style = HidayahType.Caption, color = pal.sub)
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = pal.sub)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AyahBlock(ayah: FatihaAyah, arabicFontSize: Float, showTranslation: Boolean) {
    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(
            ayah.arabic,
            style = HidayahType.Arabic.copy(fontSize = arabicFontSize.sp, lineHeight = (arabicFontSize * 1.75f).sp),
            color = pal.ink,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(26.dp).clip(CircleShape).background(GoldSoft.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Text(BnDigits.of(ayah.number), style = HidayahType.Caption.copy(fontSize = 11.sp), color = GoldDeep)
            }
            Spacer(Modifier.width(10.dp))
            if (showTranslation) {
                Text(ayah.bengali, style = HidayahType.Body, color = pal.sub, modifier = Modifier.weight(1f))
            } else {
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FullTextMissingNotice(surahName: String) {
    Card(
        shape = HidayahShapes.Medium,
        colors = CardDefaults.cardColors(containerColor = pal.background),
        border = androidx.compose.foundation.BorderStroke(1.dp, pal.line)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = GoldDeep)
                Spacer(Modifier.width(8.dp))
                Text("পূর্ণ টেক্সট যোগ হবে", style = HidayahType.TitleSans, color = pal.ink)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${surahName}-এর সম্পূর্ণ আরবি টেক্সট ও অনুবাদ অ্যাপের পরবর্তী ভার্সন থেকে অফলাইনে পাওয়া যাবে। এই ডেমো বিল্ডে শুধু সূরা আল-ফাতিহার সম্পূর্ণ টেক্সট অন্তর্ভুক্ত আছে।",
                style = HidayahType.Body, color = pal.sub
            )
        }
    }
}
