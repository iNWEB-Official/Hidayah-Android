package com.hidayah.android.ui.screens.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hidayah.android.state.HidayahViewModel
import com.hidayah.android.ui.components.*
import com.hidayah.android.ui.theme.*
import com.hidayah.android.util.BnDigits

private val pal: HidayahPalette @Composable get() = LocalHidayahPalette.current

@Composable
fun QuranScreen(viewModel: HidayahViewModel, onOpenSurah: (Int) -> Unit) {
    val settings by viewModel.settings.collectAsState()
    var query by remember { mutableStateOf("") }

    val surahs = remember(viewModel) { viewModel.surahs }
    val filtered = remember(surahs, query) {
        val q = query.trim()
        if (q.isEmpty()) surahs
        else surahs.filter {
            it.nameBn.contains(q) || it.nameTransliteration.contains(q, ignoreCase = true) ||
                it.nameArabic.contains(q) || it.number.toString() == q
        }
    }

    val lastRead = surahs.firstOrNull { it.number == settings.lastReadSurah }

    Column(Modifier.fillMaxSize().background(pal.background)) {
        ScreenTitle(
            title = "আল-কুরআন",
            subtitle = "৩০ পারা • ১১৪ সূরা",
            modifier = Modifier.statusBarsPadding().padding(horizontal = 20.dp).padding(top = 12.dp, bottom = 6.dp),
            trailing = {
                if (lastRead != null) {
                    Surface(
                        shape = CircleShape,
                        color = pal.card,
                        border = androidx.compose.foundation.BorderStroke(1.dp, pal.line),
                        modifier = Modifier.size(44.dp).clickable { onOpenSurah(lastRead.number) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Bookmark, contentDescription = "সর্বশেষ পঠিত সূরা", tint = pal.brand, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        )

        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 28.dp)
        ) {
            // ---------- Continue reading card ----------
            if (lastRead != null) {
                item {
                    Card(
                        shape = HidayahShapes.Medium,
                        colors = CardDefaults.cardColors(containerColor = Emerald),
                        modifier = Modifier.clickable { onOpenSurah(lastRead.number) }
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AutoStories, contentDescription = null, tint = Gold, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("যেখান থেকে পড়তে ছিলেন", style = HidayahType.Overline, color = Gold)
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(lastRead.nameBn, style = HidayahType.H2, color = Cream)
                                    Text(
                                        "${BnDigits.of(settings.lastReadAyahStart)}–${BnDigits.of(settings.lastReadAyahEnd)} আয়াত • ${BnDigits.of(lastRead.ayahCount)} আয়াতের মধ্যে",
                                        style = HidayahType.Caption, color = OnEmMuted
                                    )
                                }
                                Icon(Icons.Filled.PlayCircle, contentDescription = "চালিয়ে যান", tint = Gold, modifier = Modifier.size(44.dp))
                            }
                            Spacer(Modifier.height(12.dp))
                            val progress = (settings.lastReadAyahEnd.toFloat() / lastRead.ayahCount).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(5.dp).clip(HidayahShapes.Pill),
                                color = Gold,
                                trackColor = EmeraldDark
                            )
                            Spacer(Modifier.height(6.dp))
                            Text("${BnDigits.of((progress * 100).toInt())}% সম্পন্ন", style = HidayahType.Caption, color = GoldSoft)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }
            }

            // ---------- Search ----------
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("সূরা খুঁজুন (নাম বা নম্বর)…", style = HidayahType.Body, color = pal.sub) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = pal.sub) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "মুছুন", tint = pal.sub)
                            }
                        }
                    },
                    singleLine = true,
                    shape = HidayahShapes.Medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = pal.line,
                        focusedContainerColor = pal.card,
                        unfocusedContainerColor = pal.card
                    )
                )
                Spacer(Modifier.height(12.dp))
            }

            item {
                SectionLabel("সকল ${BnDigits.of(surahs.size)}টি সূরা")
                Spacer(Modifier.height(8.dp))
            }

            items(filtered, key = { it.number }) { surah ->
                SurahRow(surah.number, surah.nameBn, surah.nameTransliteration, surah.nameArabic, surah.ayahCount, surah.revelationPlace) {
                    onOpenSurah(surah.number)
                }
                HorizontalDivider(color = pal.line.copy(alpha = 0.8f), thickness = 0.6.dp)
            }

            if (filtered.isEmpty()) {
                item {
                    Text(
                        "কোনো সূরা পাওয়া যায়নি",
                        style = HidayahType.Body, color = pal.sub,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SurahRow(
    number: Int,
    nameBn: String,
    nameTransliteration: String,
    nameArabic: String,
    ayahCount: Int,
    place: String,
    onClick: () -> Unit
) {
    Surface(color = pal.background) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(HidayahShapes.Small)
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleNumber(number, 42)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(nameBn, style = HidayahType.BodyMedium, color = pal.ink)
                Text("$nameTransliteration • $place • ${BnDigits.of(ayahCount)} আয়াত", style = HidayahType.Caption.copy(fontSize = 10.sp), color = pal.sub)
            }
            Text(nameArabic, style = HidayahType.Arabic.copy(fontSize = 20.sp, lineHeight = 28.sp), color = pal.brand)
        }
    }
}
