package com.hidayah.android.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hidayah.android.data.hijri.HijriDates
import com.hidayah.android.data.bengali.BnCalendar
import com.hidayah.android.ui.components.*
import com.hidayah.android.ui.theme.*
import com.hidayah.android.util.BnDigits
import java.time.LocalDate

private val pal: HidayahPalette @Composable get() = LocalHidayahPalette.current

private data class SpecialDay(val month: Int, val day: Int, val label: String)

private val specialDays = listOf(
    SpecialDay(1, 1, "পহেলা মুহাররম (ইসলামি নববর্ষ)"),
    SpecialDay(1, 10, "আশুরা"),
    SpecialDay(3, 12, "ঈদে মিলাদুন্নবী (প্রচলিত)"),
    SpecialDay(9, 1, "রমজানের প্রথম দিন"),
    SpecialDay(9, 27, "লাইলাতুল কদর (সম্ভাব্য)"),
    SpecialDay(10, 1, "ঈদুল ফিতর"),
    SpecialDay(12, 9, "আরাফার দিন"),
    SpecialDay(12, 10, "ঈদুল আযহা")
)

@Composable
fun CalendarScreen(onBack: () -> Unit, ayahOfDay: com.hidayah.android.data.quran.AyahOfDay) {
    val today = LocalDate.now()
    val (todayHYear, todayHMonth, todayHDay) = remember { HijriDates.gregorianToHijri(today) }

    var viewYear by remember { mutableIntStateOf(todayHYear) }
    var viewMonth by remember { mutableIntStateOf(todayHMonth) }
    var selectedDay by remember { mutableIntStateOf(todayHDay) }

    val monthLength = remember(viewYear, viewMonth) { HijriDates.hijriMonthLength(viewYear, viewMonth) }
    val firstColumn = remember(viewYear, viewMonth) { HijriDates.weekdayColumnOfFirst(viewYear, viewMonth) }
    val isCurrentMonth = viewYear == todayHYear && viewMonth == todayHMonth

    Column(Modifier.fillMaxSize().background(pal.background)) {
        EmeraldHeroHeader(
            title = "হিজরী ক্যালেন্ডার",
            subtitle = "তিন পঞ্জিকা — হিজরি • গ্রেগরিয়ান • বঙ্গাব্দ",
            onBack = onBack
        ) {
            // "আজ" pill — jump back to the current hijri month
            Surface(
                shape = HidayahShapes.Pill,
                color = Gold,
                modifier = Modifier.clickable {
                    viewYear = todayHYear
                    viewMonth = todayHMonth
                    selectedDay = todayHDay
                }
            ) {
                Text(
                    "আজ",
                    style = HidayahType.BodyMedium,
                    color = EmeraldDark,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(14.dp))

            // ---------- Month header ----------
            Card(shape = HidayahShapes.Medium, colors = CardDefaults.cardColors(containerColor = Emerald)) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (viewMonth == 1) { viewMonth = 12; viewYear -= 1 } else viewMonth -= 1
                        selectedDay = 1
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "পূর্ববর্তী মাস", tint = Gold)
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${HijriDates.monthNamesBn[viewMonth - 1]} ${BnDigits.of(viewYear)} হিজরি",
                            style = HidayahType.H2, color = Cream, textAlign = TextAlign.Center
                        )
                        val firstGregorian = remember(viewYear, viewMonth) { HijriDates.hijriToGregorian(viewYear, viewMonth, 1) }
                        val lastGregorian = remember(viewYear, viewMonth) { HijriDates.hijriToGregorian(viewYear, viewMonth, HijriDates.hijriMonthLength(viewYear, viewMonth)) }
                        Text(
                            "${BnDigits.of(firstGregorian.dayOfMonth)} ${gregMonthBn(firstGregorian.monthValue)} – ${BnDigits.of(lastGregorian.dayOfMonth)} ${gregMonthBn(lastGregorian.monthValue)} ${BnDigits.of(lastGregorian.year)}",
                            style = HidayahType.Caption, color = OnEmMuted
                        )
                        Text(
                            run {
                                val (sy, sm, sd) = BnCalendar.fromGregorian(firstGregorian)
                                val (ey, em, ed) = BnCalendar.fromGregorian(lastGregorian)
                                if (sy == ey) {
                                    "${BnDigits.of(sd)} ${BnCalendar.monthNamesBn[sm - 1]} – ${BnDigits.of(ed)} ${BnCalendar.monthNamesBn[em - 1]} ${BnDigits.of(ey)} বঙ্গাব্দ"
                                } else {
                                    "${BnDigits.of(sd)} ${BnCalendar.monthNamesBn[sm - 1]} ${BnDigits.of(sy)} – ${BnDigits.of(ed)} ${BnCalendar.monthNamesBn[em - 1]} ${BnDigits.of(ey)} বঙ্গাব্দ"
                                }
                            },
                            style = HidayahType.Caption.copy(fontSize = 10.sp), color = Gold.copy(alpha = 0.8f)
                        )
                    }
                    IconButton(onClick = {
                        if (viewMonth == 12) { viewMonth = 1; viewYear += 1 } else viewMonth += 1
                        selectedDay = 1
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "পরবর্তী মাস", tint = Gold)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))

            // ---------- Weekday header (Saturday-first) ----------
            Row(Modifier.fillMaxWidth()) {
                HijriDates.weekdayShortBn.forEach { day ->
                    Text(
                        day,
                        style = HidayahType.Caption.copy(fontSize = 12.sp),
                        color = if (day == "শুক্র") GoldDeep else pal.sub,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f).padding(vertical = 6.dp)
                    )
                }
            }

            // ---------- Day grid ----------
            val cells = firstColumn + monthLength
            val rows = (cells + 6) / 7
            var dayCounter = 1
            repeat(rows) { row ->
                Row(Modifier.fillMaxWidth()) {
                    repeat(7) { col ->
                        val cellIndex = row * 7 + col
                        if (cellIndex in firstColumn until firstColumn + monthLength) {
                            val day = dayCounter++
                            val isToday = isCurrentMonth && day == todayHDay
                            val isSelected = day == selectedDay
                            val isFriday = col == 6
                            val special = specialDays.firstOrNull { it.month == viewMonth && it.day == day }
                            DayCell(
                                day = day,
                                isToday = isToday,
                                isSelected = isSelected,
                                isFriday = isFriday,
                                hasSpecial = special != null,
                                onClick = { selectedDay = day },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(Modifier.weight(1f).height(56.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // ---------- Selected day details ----------
            val selectedGregorian = remember(viewYear, viewMonth, selectedDay) {
                HijriDates.hijriToGregorian(viewYear, viewMonth, selectedDay)
            }
            val selectedSpecial = specialDays.firstOrNull { it.month == viewMonth && it.day == selectedDay }
            Card(shape = HidayahShapes.Medium, colors = CardDefaults.cardColors(containerColor = pal.card)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(52.dp).clip(HidayahShapes.Small).background(pal.brandOnSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(BnDigits.of(selectedDay), style = HidayahType.H1, color = Gold)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        // তিন পঞ্জিকা — হিজরি (উম্মুল-ক্বুরা), ইংরেজি (গ্রেগরিয়ান), বাংলা (বঙ্গাব্দ)
                        Text(
                            "${BnDigits.of(selectedDay)} ${HijriDates.monthNamesBn[viewMonth - 1]} ${BnDigits.of(viewYear)} হিজরি",
                            style = HidayahType.BodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = pal.ink
                        )
                        Text(HijriDates.gregLabelBn(selectedGregorian), style = HidayahType.Caption, color = pal.sub)
                        Text(BnCalendar.labelBn(selectedGregorian), style = HidayahType.Caption, color = pal.sub)
                    }
                }
            }
            if (selectedSpecial != null) {
                Spacer(Modifier.height(10.dp))
                Surface(shape = HidayahShapes.Medium, color = GoldSoft.copy(alpha = 0.4f)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Event, contentDescription = null, tint = GoldDeep)
                        Spacer(Modifier.width(8.dp))
                        Text(selectedSpecial.label, style = HidayahType.BodyMedium, color = EmeraldDark)
                    }
                }
            }

            // ---------- Upcoming special days ----------
            Spacer(Modifier.height(18.dp))
            SectionLabel("বছরের গুরুত্বপূর্ণ দিনসমূহ")
            Spacer(Modifier.height(8.dp))
            specialDays.forEach { special ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(34.dp).clip(HidayahShapes.Small).background(Emerald.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(BnDigits.of(special.day), style = HidayahType.Caption.copy(fontSize = 13.sp), color = pal.brand)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(special.label, style = HidayahType.Body, color = pal.ink, modifier = Modifier.weight(1f))
                    Text(HijriDates.monthNamesBn[special.month - 1], style = HidayahType.Caption, color = pal.sub)
                }
            }
            // ---------- আজকের আয়াত কার্ড ----------
            Spacer(Modifier.height(18.dp))
            AyahOfDayCard(ayahOfDay, s = 1f, fixedCardHeight = 264.dp)
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    isFriday: Boolean,
    hasSpecial: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = when {
        isSelected -> Emerald
        isToday -> GoldSoft.copy(alpha = 0.5f)
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> Cream
        isFriday -> GoldDeep
        else -> pal.ink
    }
    Column(
        modifier
            .height(56.dp)
            .padding(2.dp)
            .clip(HidayahShapes.Small)
            .background(bg)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(BnDigits.of(day), style = HidayahType.BodyMedium, color = textColor)
        if (hasSpecial) {
            Box(Modifier.size(4.dp).clip(androidx.compose.foundation.shape.CircleShape).background(if (isSelected) Gold else GoldDeep))
        }
    }
}

private fun gregMonthBn(month: Int): String =
    listOf("জানুয়ারী", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")[month - 1]
