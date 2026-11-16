package com.hidayah.android.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.hidayah.android.data.hijri.HijriDates
import com.hidayah.android.data.hijri.IslamicSpecialDays
import com.hidayah.android.data.bengali.BnCalendar
import com.hidayah.android.data.Cities
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import android.content.Intent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import com.hidayah.android.R
import com.hidayah.android.state.HidayahViewModel
import com.hidayah.android.ui.components.*
import com.hidayah.android.ui.theme.*
import com.hidayah.android.util.BnDigits
import java.time.LocalDate


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

private val pal: HidayahPalette @Composable get() = LocalHidayahPalette.current

/**
 * Home — FIXED, non-scrolling dashboard. Every block lives in its own card and
 * the whole layout scales with the available screen height (uniform scale factor),
 * so nothing is ever cut off. Locked to portrait via the manifest.
 */
@Composable
fun HomeScreen(
    viewModel: HidayahViewModel,
    onOpenDuas: () -> Unit,
    onOpenQuran: () -> Unit,
    onOpenQibla: () -> Unit = {},
    onOpenTasbih: () -> Unit = {},
    onOpenCalendar: () -> Unit = {},
    onOpenProfile: () -> Unit = {}
) {
    val homeUi by viewModel.homeUi.collectAsState()
    val homeSettings by viewModel.settings.collectAsState()
    val localContext = LocalContext.current
    val useBangla = androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language.startsWith("bn")
    var locationMenuExpanded by remember { mutableStateOf(false) }
    val displayName = homeSettings.userName.trim().ifBlank { "অতিথি" }

    // জিপিএস ফিক্স (বাংলাদেশের বাইরেরও) — পারমিশন চেয়ে রিভার্স-জিওকোডসহ
    val requestGpsFix = rememberGpsLocationRequester { fix ->
        if (fix != null) {
            viewModel.setGpsLocation(fix.lat, fix.lng, fix.label)
            android.widget.Toast.makeText(localContext, "অবস্থান সেট হয়েছে: ${fix.label}", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            android.widget.Toast.makeText(localContext, "জিপিএস থেকে অবস্থান পাওয়া যায়নি — পারমিশন/লোকেশন চালু আছে কিনা দেখুন", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    // জিপিএস মোড চালু থাকলে অ্যাপ খোলার সময় নীরবে কো-অর্ডিনেট রিফ্রেশ
    val silentGpsRefresh = rememberSilentGpsRefresh { fix -> viewModel.setGpsLocation(fix.lat, fix.lng, fix.label) }
    LaunchedEffect(homeSettings.useGpsLocation) {
        if (homeSettings.useGpsLocation) silentGpsRefresh()
    }
    val comingSoonMsg = if (useBangla) "শীঘ্রই আসছে" else "Up Coming"
    val showComingSoon: () -> Unit = {
        android.widget.Toast.makeText(localContext, comingSoonMsg, android.widget.Toast.LENGTH_SHORT).show()
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(pal.background)) {
        // uniform scale relative to a ~780dp tall reference viewport
        val s = (maxHeight.value / 780f).coerceIn(0.72f, 1.06f)
        val dp: (Float) -> Dp = { (it * s).dp }
        val sp: (Float) -> androidx.compose.ui.unit.TextUnit = { (it * s).sp }
        val gap = dp(10f)
        val padH = dp(14f)

        Column(Modifier.fillMaxSize()) {
            // ============ EMERALD HEADER (star lattice) — mockup 01-home ============
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFF11493A), Color(0xFF0D3A2C), Color(0xFF082A20))))
                    .hidayahStarLattice()
                    .statusBarsPadding()
                    .padding(horizontal = dp(18f))
                    .padding(top = dp(6f), bottom = dp(44f))
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).clickable(onClick = onOpenProfile)
                        ) {
                            ProfileAvatar(
                                imagePath = homeSettings.profileImagePath,
                                displayName = homeSettings.userName,
                                size = dp(46f)
                            )
                            Spacer(Modifier.width(dp(11f)))
                            Column {
                                Text("আসসালামু আলাইকুম,", style = HidayahType.Caption.copy(fontSize = sp(11.5f)), color = OnEmMuted)
                                // লগইন/নাম সেট করা না থাকলে "অতিথি"
                                Text(displayName, style = HidayahType.H2.copy(fontSize = sp(20f)), color = Cream)
                            }
                        }
                        Box {
                            Icon(Icons.Filled.Notifications, contentDescription = "বিজ্ঞপ্তি", tint = Cream, modifier = Modifier.size(dp(26f)))
                            Box(
                                Modifier
                                    .size(dp(10.5f))
                                    .clip(CircleShape)
                                    .background(Gold)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 1.dp, y = (-1).dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(dp(11f)))
                    // অবস্থান পিল — ট্যাপ করে ড্রপডাউন থেকে ম্যানুয়ালি শহর বা জিপিএস দিয়ে সেট করুন
                    Box {
                        Row(
                            Modifier
                                .clip(HidayahShapes.Pill)
                                .clickable { locationMenuExpanded = true }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Place, contentDescription = null, tint = Gold, modifier = Modifier.size(dp(16f)))
                            Spacer(Modifier.width(dp(5f)))
                            Text(if (homeSettings.useGpsLocation) homeUi.cityName else "${homeUi.cityName}, বাংলাদেশ", style = HidayahType.BodyMedium.copy(fontSize = sp(14.5f)), color = Cream)
                            Spacer(Modifier.width(dp(3f)))
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Gold, modifier = Modifier.size(dp(17f)))
                        }
                        DropdownMenu(
                            expanded = locationMenuExpanded,
                            onDismissRequest = { locationMenuExpanded = false }
                        ) {
                            // জিপিএস — বাংলাদেশের বাইরের ব্যবহারকারীদের জন্যও সঠিক
                            DropdownMenuItem(
                                text = { Text(if (homeSettings.useGpsLocation) "জিপিএস অবস্থান সক্রিয়" else "জিপিএস থেকে সেট করুন") },
                                leadingIcon = { Icon(Icons.Filled.MyLocation, contentDescription = null, tint = Gold) },
                                trailingIcon = if (homeSettings.useGpsLocation) {
                                    { Icon(Icons.Filled.Check, contentDescription = "সক্রিয়", tint = Gold) }
                                } else null,
                                onClick = { locationMenuExpanded = false; requestGpsFix() }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("বাংলাদেশের শহরসমূহ", style = HidayahType.Caption, color = pal.sub) },
                                enabled = false,
                                onClick = {}
                            )
                            Cities.ALL.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text(city.nameBn) },
                                    leadingIcon = {
                                        if (!homeSettings.useGpsLocation && homeUi.cityName == city.nameBn) Icon(Icons.Filled.Check, contentDescription = null)
                                    },
                                    onClick = { viewModel.setCity(city.key); locationMenuExpanded = false }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(dp(11f)))
                    Surface(
                        shape = HidayahShapes.Large,
                        color = Color(0xFFFFFFFF).copy(alpha = 0.07f),
                        border = BorderStroke(1.dp, Color(0xFFFFFFFF).copy(alpha = 0.10f)),
                        modifier = Modifier.clickable(onClick = onOpenCalendar)
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
            }

            // ============ FIXED BODY — fills the remaining height, every block in its own card ============
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = padH),
                verticalArrangement = Arrangement.spacedBy(gap)
            ) {
                // ---------- Next prayer — cream card overlapping the header ----------
                val next = homeUi.prayerTimes.getOrNull(homeUi.nextIndex)
                if (next != null) {
                    NextPrayerCard(
                        name = next.nameBangla,
                        time = BnDigits.minutesToBnTime(next.totalMinutes),
                        dayPart = dayPartFor(next.id),
                        countdownMinutes = homeUi.countdownMinutes,
                        progress = prayerWindowFraction(homeUi.prayerTimes, homeUi.nextIndex, homeUi.nowMinutes),
                        s = s,
                        modifier = Modifier.weight(1.10f).offset(y = (-14).dp)
                    )
                } else {
                    Spacer(Modifier.weight(1.12f))
                }

                // ---------- Section header ----------
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("আজকের নামাজের সময়", style = HidayahType.TitleSans.copy(fontSize = sp(15f)), color = pal.ink, modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("সম্পূর্ণ সূচি", style = HidayahType.Caption.copy(fontSize = sp(12.5f)), color = GoldDeep)
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = GoldDeep, modifier = Modifier.size(dp(16f)))
                    }
                }

                // ---------- Six times panel ----------
                PrayerTimesPanel(times = homeUi.prayerTimes, nextIndex = homeUi.nextIndex, s = s, modifier = Modifier.weight(0.95f))

                // ---------- Quick actions — background-image card ----------
                Card(
                    shape = HidayahShapes.Medium,
                    colors = CardDefaults.cardColors(containerColor = EmeraldDeep),
                    modifier = Modifier.fillMaxWidth().weight(1.26f)
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Image(
                            painterResource(R.drawable.bg_quick_actions),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(HidayahShapes.Medium)
                        )
                        Box(
                            Modifier
                                .fillMaxSize()
                                .clip(HidayahShapes.Medium)
                                .background(Brush.verticalGradient(listOf(Color(0x26000000), Color(0x4D000000))))
                        )
                        Column(
                            Modifier.fillMaxSize().padding(horizontal = dp(4f), vertical = dp(5f)),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                                RoundQuickAction("কুরআন", onClick = onOpenQuran, s = s) { tint ->
                                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = tint, modifier = Modifier.size(dp(22f)))
                                }
                                RoundQuickAction("কিবলা", onClick = onOpenQibla, s = s) { tint ->
                                    Icon(Icons.Filled.Explore, contentDescription = null, tint = tint, modifier = Modifier.size(dp(22f)))
                                }
                                RoundQuickAction("দোয়া", onClick = onOpenDuas, s = s, filledGold = true) { _ ->
                                    Text("دُعَا", style = HidayahType.Arabic.copy(fontSize = sp(16f)), color = EmeraldDark)
                                }
                                RoundQuickAction("তসবিহ", onClick = onOpenTasbih, s = s) { tint ->
                                    TasbihBeadsIcon(tint = tint, modifier = Modifier.size(dp(22f)))
                                }
                                RoundQuickAction("ক্যালেন্ডার", onClick = onOpenCalendar, s = s) { tint ->
                                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = tint, modifier = Modifier.size(dp(22f)))
                                }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                                RoundQuickAction("যাকাত", onClick = showComingSoon, s = s, comingSoon = true) { tint ->
                                    Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = tint, modifier = Modifier.size(dp(21f)))
                                }
                                RoundQuickAction("তাফসীর", onClick = showComingSoon, s = s, comingSoon = true) { tint ->
                                    Icon(Icons.Filled.AutoStories, contentDescription = null, tint = tint, modifier = Modifier.size(dp(21f)))
                                }
                                RoundQuickAction("প্রশ্নোত্তর", onClick = showComingSoon, s = s, comingSoon = true) { tint ->
                                    Icon(Icons.Filled.HelpOutline, contentDescription = null, tint = tint, modifier = Modifier.size(dp(21f)))
                                }
                                RoundQuickAction("হজ-উমরা", onClick = showComingSoon, s = s, comingSoon = true) { tint ->
                                    KaabaGlyph(tint = tint, modifier = Modifier.size(dp(21f)))
                                }
                                RoundQuickAction("মাসজিদ", onClick = showComingSoon, s = s, comingSoon = true) { tint ->
                                    MosqueGlyph(tint = tint, modifier = Modifier.size(dp(21f)))
                                }
                            }
                        }
                    }
                }

                // ---------- Ayah of the day — header row (share/copy বাইরে) + pure card ----------
                AyahOfDayCard(viewModel, s = s, modifier = Modifier.weight(1.26f).padding(bottom = dp(2f)))
            }
        }
    }
}

// ---------------------------------------------------------------

@Composable
private fun NextPrayerCard(
    name: String,
    time: String,
    dayPart: String,
    countdownMinutes: Int,
    progress: Float,
    s: Float,
    modifier: Modifier = Modifier
) {
    val sp: (Float) -> androidx.compose.ui.unit.TextUnit = { (it * s).sp }
    Card(
        shape = HidayahShapes.Large,
        colors = CardDefaults.cardColors(containerColor = EmeraldDeep),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painterResource(R.drawable.bg_next_prayer),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(HidayahShapes.Large)
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(HidayahShapes.Large)
                    .background(Brush.horizontalGradient(listOf(Color(0xE008261D), Color(0x7308261D), Color(0x2608261D))))
            )
            Row(
                Modifier.fillMaxSize().padding(horizontal = (17 * s).dp, vertical = (10 * s).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text("পরবর্তী নামাজ", style = HidayahType.Overline.copy(color = Gold, fontSize = sp(10.5f), letterSpacing = 1.5.sp))
                Spacer(Modifier.height((3 * s).dp))
                Text(
                    name,
                    style = HidayahType.Display.copy(fontFamily = NotoSerifBengali, fontWeight = FontWeight.Bold, fontSize = sp(33f), lineHeight = (42 * s).sp),
                    color = Cream
                )
                Spacer(Modifier.height((2 * s).dp))
                val h = countdownMinutes / 60
                val m = countdownMinutes % 60
                val tail = if (h > 0) "আর ${BnDigits.of(h)} ঘণ্টা ${BnDigits.of(m)} মিনিট" else "আর ${BnDigits.of(m)} মিনিট"
                Text("$dayPart $time  •  $tail", style = HidayahType.Body.copy(fontSize = sp(12.5f)), color = OnEmMuted)
            }
            Spacer(Modifier.width((8 * s).dp))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size((88 * s).dp)) {
                Canvas(Modifier.size((88 * s).dp)) {
                    val stroke = 8.5f * s * density
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    val topLeft = Offset(stroke / 2f, stroke / 2f)
                    drawArc(
                        color = Color(0x33FBF8ED), startAngle = -90f, sweepAngle = 360f, useCenter = false,
                        topLeft = topLeft, size = arcSize, style = Stroke(stroke)
                    )
                    drawArc(
                        color = Gold, startAngle = -90f, sweepAngle = 360f * progress.coerceIn(0.03f, 1f), useCenter = false,
                        topLeft = topLeft, size = arcSize, style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                }
                MosqueGlyph(Modifier.size((40 * s).dp), tint = Gold)
            }
            }
        }
    }
}

@Composable
private fun PrayerTimesPanel(
    times: List<com.hidayah.android.data.prayer.PrayerTime>,
    nextIndex: Int,
    s: Float,
    modifier: Modifier = Modifier
) {
    Card(
        shape = HidayahShapes.Medium,
        colors = CardDefaults.cardColors(containerColor = EmeraldDeep),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painterResource(R.drawable.bg_prayer_times),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(HidayahShapes.Medium)
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(HidayahShapes.Medium)
                    .background(Brush.verticalGradient(listOf(Color(0x40122B21), Color(0x73122B21))))
            )
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = (6 * s).dp, vertical = (8 * s).dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                times.forEachIndexed { index, prayer ->
                    val isNext = index == nextIndex
                    PrayerTimeCell(
                        id = prayer.id,
                        name = prayer.nameBangla,
                        time = BnDigits.minutesToBnTime(prayer.totalMinutes),
                        isNext = isNext,
                        s = s,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PrayerTimeCell(id: String, name: String, time: String, isNext: Boolean, s: Float, modifier: Modifier = Modifier) {
    val sp: (Float) -> androidx.compose.ui.unit.TextUnit = { (it * s).sp }
    val iconTint = if (isNext) Gold else OnEmMuted
    val cellBg = if (isNext) Gold.copy(alpha = 0.16f) else Color.Transparent
    Column(
        modifier
            .clip(HidayahShapes.Medium)
            .background(cellBg)
            .padding(vertical = (7 * s).dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PrayerGlyph(id = id, tint = iconTint, modifier = Modifier.size((25 * s).dp))
        Spacer(Modifier.height((3 * s).dp))
        Text(
            name,
            style = HidayahType.Caption.copy(fontSize = sp(11.5f)),
            color = if (isNext) GoldSoft else OnEmMuted,
            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
        Text(
            time,
            style = HidayahType.Caption.copy(fontSize = sp(11.5f)),
            color = if (isNext) Gold else Cream,
            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun RoundQuickAction(
    label: String,
    onClick: () -> Unit,
    s: Float,
    filledGold: Boolean = false,
    comingSoon: Boolean = false,
    icon: @Composable (tint: Color) -> Unit
) {
    val sp: (Float) -> androidx.compose.ui.unit.TextUnit = { (it * s).sp }
    val isBn = androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language.startsWith("bn")
    val badgeText = if (isBn) "শীঘ্রই আসছে" else "Up Coming"
    val circle = (46 * s).dp
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            Box(
                Modifier
                    .size(circle)
                    .clip(CircleShape)
                    .background(
                        if (filledGold) Brush.linearGradient(listOf(Gold, Color(0xFFC9A227)))
                        else Brush.linearGradient(listOf(Color(0xFFFFFFFF).copy(alpha = 0.16f), Color(0xFFFFFFFF).copy(alpha = 0.07f)))
                    )
                    .then(if (filledGold) Modifier else Modifier.border(1.dp, Gold.copy(alpha = 0.45f), CircleShape))
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                icon(if (filledGold) EmeraldDark else Gold)
            }
            if (comingSoon) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 5.dp, y = (-4).dp)
                        .clip(HidayahShapes.Pill)
                        .background(Gold)
                        .padding(horizontal = 4.dp, vertical = 0.5.dp)
                ) {
                    Text(
                        badgeText,
                        style = HidayahType.Caption.copy(fontSize = 6.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.sp),
                        color = EmeraldDark,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
        Spacer(Modifier.height((4 * s).dp))
        Text(label, style = HidayahType.Caption.copy(fontSize = sp(10.5f), fontWeight = FontWeight.SemiBold), color = Cream, maxLines = 1)
    }
}

@Composable
private fun AyahOfDayCard(viewModel: HidayahViewModel, s: Float, modifier: Modifier = Modifier) {
    val sp: (Float) -> androidx.compose.ui.unit.TextUnit = { (it * s).sp }
    val ayah = viewModel.ayahOfDay
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val dpv = { v: Float -> (v * s).dp }

    val shareText = remember(ayah) {
        "${ayah.arabic}\n\n${ayah.bengali}\n\n${ayah.reference}\n\n— হিদায়াহ (আজকের আয়াত)"
    }

    fun shareImage() {
        try {
            val file = com.hidayah.android.ui.components.AyahShareImage.create(context, ayah)
            val uri = androidx.core.content.FileProvider.getUriForFile(context, "com.hidayah.android.fileprovider", file)
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
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
        Box(Modifier.fillMaxSize()) {
            AyahCardArtwork(Modifier.fillMaxSize())

            // ---------- captured content (artwork + kicker + verse + brand) ----------
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = dpv(18f), vertical = dpv(9f)),
                verticalArrangement = Arrangement.Center
            ) {
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
                    "H I D A Y A H",
                    style = HidayahType.Overline.copy(color = Gold.copy(alpha = 0.75f), fontSize = sp(8.5f), letterSpacing = 3.2.sp),
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

// ---------- custom-drawn glyphs (mockup-faithful, no assets) ----------

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

@Composable
private fun KaabaGlyph(modifier: Modifier = Modifier, tint: Color = Gold) {
    Canvas(modifier) {
        val u = size.minDimension / 24f
        // কাবার কিউব
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
private fun MosqueGlyph(modifier: Modifier = Modifier, tint: Color = Emerald) {
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
        drawArc(Color(0xFFFBF8ED), 180f, 180f, true, topLeft = Offset(cx - 2.2f * u, baseY - 6.0f * u - 2.2f * u), size = Size(4.4f * u, 4.4f * u))
        drawRoundRect(Color(0xFFFBF8ED), topLeft = Offset(cx - 2.2f * u, baseY - 6.0f * u), size = Size(4.4f * u, 6.0f * u), cornerRadius = androidx.compose.ui.geometry.CornerRadius(0.4f * u))
    }
}

@Composable
fun TasbihBeadsIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val u = size.minDimension / 24f
        val cx = 12f * u
        val cy = 11.2f * u
        val r = 6.1f * u
        val beadR = 1.25f * u
        var angle = 90f + 36f
        while (angle <= 90f + 360f - 36f) {
            val rad = Math.toRadians(angle.toDouble())
            val px = cx + (r * kotlin.math.cos(rad)).toFloat()
            val py = cy + (r * kotlin.math.sin(rad)).toFloat()
            drawCircle(tint, beadR, Offset(px, py))
            angle += 27f
        }
        drawLine(tint, Offset(cx, cy + r), Offset(cx, 21.3f * u), 1.15f * u, cap = StrokeCap.Round)
        drawCircle(tint, 1.35f * u, Offset(cx, 22.3f * u))
    }
}

@Composable
private fun PrayerGlyph(id: String, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val u = size.minDimension / 24f
        val cx = size.width / 2f
        val horizonY = 15.5f * u
        val stroke = 1.55f * u
        val cap = StrokeCap.Round
        fun horizon() = drawLine(tint, Offset(1.6f * u, horizonY), Offset(size.width - 1.6f * u, horizonY), stroke, cap = cap)
        fun halfSun() = drawArc(tint, 180f, 180f, false, topLeft = Offset(cx - 4.9f * u, horizonY - 4.9f * u), size = Size(9.8f * u, 9.8f * u), style = Stroke(stroke, cap = cap))
        fun arrowUp() {
            drawLine(tint, Offset(cx, 3.0f * u), Offset(cx, 8.6f * u), stroke, cap = cap)
            drawLine(tint, Offset(cx - 2.1f * u, 4.6f * u), Offset(cx, 2.6f * u), stroke, cap = cap)
            drawLine(tint, Offset(cx + 2.1f * u, 4.6f * u), Offset(cx, 2.6f * u), stroke, cap = cap)
        }
        fun arrowDown() {
            drawLine(tint, Offset(cx, 2.6f * u), Offset(cx, 8.2f * u), stroke, cap = cap)
            drawLine(tint, Offset(cx - 2.1f * u, 6.3f * u), Offset(cx, 8.7f * u), stroke, cap = cap)
            drawLine(tint, Offset(cx + 2.1f * u, 6.3f * u), Offset(cx, 8.7f * u), stroke, cap = cap)
        }
        fun rays(centerX: Float, centerY: Float, rFrom: Float, rTo: Float) {
            for (deg in 0 until 360 step 45) {
                val rad = Math.toRadians(deg.toDouble())
                val c = kotlin.math.cos(rad).toFloat()
                val sn = kotlin.math.sin(rad).toFloat()
                drawLine(tint, Offset(centerX + rFrom * c, centerY + rFrom * sn), Offset(centerX + rTo * c, centerY + rTo * sn), stroke, cap = cap)
            }
        }
        when (id) {
            "fajr" -> { halfSun(); horizon(); rays(cx, horizonY, 4.0f * u, 6.2f * u) }
            "sunrise" -> { arrowUp(); halfSun(); horizon() }
            "dhuhr" -> { val cy = 12f * u; drawCircle(tint, 4.9f * u, Offset(cx, cy), style = Stroke(stroke)); rays(cx, cy, 6.9f * u, 9.4f * u) }
            "asr" -> { val cy = 12.5f * u; drawCircle(tint, 4.9f * u, Offset(cx, cy), style = Stroke(stroke)); rays(cx, cy, 6.7f * u, 8.9f * u) }
            "maghrib" -> { arrowDown(); halfSun(); horizon() }
            "isha" -> {
                val cy = 12f * u
                val outer = Path().apply { addOval(androidx.compose.ui.geometry.Rect(cx - 5.6f * u, cy - 5.6f * u, cx + 5.6f * u, cy + 5.6f * u)) }
                val cut = Path().apply { addOval(androidx.compose.ui.geometry.Rect(cx - 2.6f * u, cy - 4.8f * u, cx + 7.6f * u, cy + 4.8f * u)) }
                drawPath(Path().apply { op(outer, cut, PathOperation.Difference) }, tint)
            }
            else -> drawCircle(tint, 5f * u, Offset(cx, 12f * u), style = Stroke(stroke))
        }
    }
}

private fun dayPartFor(id: String): String = when (id) {
    "fajr" -> "ভোর"; "sunrise" -> "সকাল"; "dhuhr" -> "দুপুর"; "asr" -> "বিকেল"; "maghrib" -> "সন্ধ্যা"; else -> "রাত"
}

private fun prayerWindowFraction(times: List<com.hidayah.android.data.prayer.PrayerTime>, nextIndex: Int, nowMinutes: Int): Float {
    if (times.isEmpty()) return 0.5f
    val next = times[nextIndex].totalMinutes
    val prevIndex = (nextIndex - 1 + times.size) % times.size
    val prev = times[prevIndex].totalMinutes
    return if (nextIndex == 0) {
        val elapsed = (1440 - prev) + nowMinutes
        (elapsed.toFloat() / ((1440 - prev) + next)).coerceIn(0f, 1f)
    } else {
        val total = next - prev
        if (total <= 0) 0.5f else ((nowMinutes - prev).toFloat() / total).coerceIn(0f, 1f)
    }
}
