package com.hidayah.android.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hidayah.android.data.Cities
import com.hidayah.android.state.HidayahViewModel
import com.hidayah.android.ui.components.*
import com.hidayah.android.ui.theme.*
import com.hidayah.android.util.BnDigits
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

private val pal: HidayahPalette @Composable get() = LocalHidayahPalette.current

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: HidayahViewModel) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    var showCitySheet by remember { mutableStateOf(false) }
    var showMadhabSheet by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }

    val requestGpsCity = rememberGpsLocationRequester { fix ->
        if (fix != null) {
            viewModel.setGpsLocation(fix.lat, fix.lng, fix.label)
            Toast.makeText(context, "অবস্থান সেট হয়েছে: ${fix.label}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "জিপিএস থেকে অবস্থান পাওয়া যায়নি — পারমিশন/লোকেশন চালু আছে কিনা দেখুন", Toast.LENGTH_SHORT).show()
        }
    }
    val locationSubtitle = if (settings.useGpsLocation) settings.gpsLabel.ifBlank { "বর্তমান অবস্থান (জিপিএস)" } else Cities.byKey(settings.cityKey).nameBn

    Column(Modifier.fillMaxSize().background(pal.background)) {
        ScreenTitle(
            title = "সেটিংস",
            subtitle = "অ্যাপ ও নামাজের পছন্দসমূহ",
            modifier = Modifier.statusBarsPadding().padding(horizontal = 20.dp).padding(top = 14.dp, bottom = 4.dp)
        )
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ---------- Appearance ----------
            SectionLabel("চেহারা")
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Filled.DarkMode,
                    title = "থিম",
                    subtitle = when (settings.theme) {
                        "light" -> "লাইট — ক্রিম ও সবুজ"
                        "dark" -> "ডার্ক — গাঢ় সবুজ"
                        else -> "সিস্টেম অনুযায়ী"
                    },
                    onClick = { showThemeSheet = true },
                    trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = pal.sub) }
                )
                HorizontalDivider(color = pal.line, thickness = 0.6.dp)
                SettingsRow(
                    icon = Icons.Filled.Language,
                    title = "ভাষা",
                    subtitle = when (settings.language) {
                        "bn" -> "বাংলা"
                        "en" -> "English (অনুবাদ চলমান)"
                        else -> "সিস্টেম অনুযায়ী (বাংলা)"
                    },
                    onClick = { showLanguageSheet = true },
                    trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = pal.sub) }
                )
            }
            Spacer(Modifier.height(18.dp))

            // ---------- Prayer calculation ----------
            SectionLabel("নামাজের হিসাব")
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                SettingsRow(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    title = "মাযহাব (আসর)",
                    subtitle = if (settings.hanafiAsr) "হানাফি — আসর: ছায়া দ্বিগুণ" else "শাফেয়ী — আসর: ছায়া একগুণ",
                    onClick = { showMadhabSheet = true },
                    trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = pal.sub) }
                )
                HorizontalDivider(color = pal.line, thickness = 0.6.dp)
                SettingsRow(
                    icon = Icons.Filled.Place,
                    title = "অবস্থান",
                    subtitle = locationSubtitle,
                    onClick = { showCitySheet = true },
                    trailing = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = pal.sub) }
                )
            }
            Spacer(Modifier.height(18.dp))

            // ---------- Notifications ----------
            SectionLabel("বিজ্ঞপ্তি")
            Spacer(Modifier.height(8.dp))
            SettingsGroup {
                ToggleRow(
                    icon = Icons.Filled.WbSunny,
                    title = "সেহরি ও ইফতার",
                    subtitle = "রমজানে সেহরি শেষ ও ইফতারের সময় নোটিফিকেশন",
                    checked = settings.notifSeheriIftar,
                    onCheckedChange = viewModel::setNotifSeheriIftar
                )
                HorizontalDivider(color = pal.line, thickness = 0.6.dp)
                ToggleRow(
                    icon = Icons.Filled.Nightlight,
                    title = "তাহাজ্জুদ",
                    subtitle = "শেষ রাতের তাহাজ্জুদের জন্য নোটিফিকেশন",
                    checked = settings.notifTahajjud,
                    onCheckedChange = viewModel::setNotifTahajjud
                )
                HorizontalDivider(color = pal.line, thickness = 0.6.dp)
                ToggleRow(
                    icon = Icons.Filled.Groups,
                    title = "জুমার দিন",
                    subtitle = "প্রতি শুক্রবার সকালে জুমার জন্য রিমাইন্ডার",
                    checked = settings.notifJumuah,
                    onCheckedChange = viewModel::setNotifJumuah
                )
            }
            Spacer(Modifier.height(18.dp))

            Spacer(Modifier.height(24.dp))
            Text(
                "بِسْمِ ٱللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                style = HidayahType.Arabic.copy(fontSize = 15.sp, lineHeight = 24.sp),
                color = pal.sub,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(28.dp))
        }
    }

    if (showLanguageSheet) {
        val languageOptions = listOf(
            Triple("system", "সিস্টেম অনুযায়ী", "ফোন সেটিংসের ভাষা অনুসরণ করবে"),
            Triple("bn", "বাংলা", "অ্যাপের ভাষা বাংলা (ডিফল্ট)"),
            Triple("en", "English", "অনুবাদ চলমান — নিচের বারের লেবেল এখনই বদলাবে")
        )
        AlertDialog(
            onDismissRequest = { showLanguageSheet = false },
            containerColor = pal.card,
            title = { Text("ভাষা বাছুন", style = HidayahType.H2, color = pal.ink) },
            text = {
                Column {
                    languageOptions.forEach { (key, label, caption) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(HidayahShapes.Medium)
                                .clickable { applyLanguage(key, viewModel, context) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.language == key,
                                onClick = { applyLanguage(key, viewModel, context) },
                                colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                    selectedColor = Emerald, unselectedColor = pal.sub
                                )
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(label, style = HidayahType.BodyMedium, color = pal.ink)
                                Text(caption, style = HidayahType.Caption.copy(fontSize = 11.sp), color = pal.sub)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageSheet = false }) {
                    Text("সম্পন্ন", style = HidayahType.BodyMedium, color = pal.brand)
                }
            }
        )
    }

    if (showThemeSheet) {
        val themeOptions = listOf(
            Triple("system", "সিস্টেম অনুযায়ী", "ফোন সেটিংসের থিম অনুসরণ করবে"),
            Triple("light", "লাইট — ক্রিম ও সবুজ", "বর্তমান মকআপের ক্রিম কার্ড + সবুজ হেডার"),
            Triple("dark", "ডার্ক — গাঢ় সবুজ", "আরামদায়ক গাঢ় সবুজ মোড, রাতে স্নিগ্ধ")
        )
        AlertDialog(
            onDismissRequest = { showThemeSheet = false },
            containerColor = pal.card,
            title = { Text("থিম বাছুন", style = HidayahType.H2, color = pal.ink) },
            text = {
                Column {
                    themeOptions.forEach { (key, label, caption) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(HidayahShapes.Medium)
                                .clickable { viewModel.setTheme(key) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.theme == key,
                                onClick = { viewModel.setTheme(key) },
                                colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                    selectedColor = Emerald, unselectedColor = pal.sub
                                )
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(label, style = HidayahType.BodyMedium, color = pal.ink)
                                Text(caption, style = HidayahType.Caption.copy(fontSize = 11.sp), color = pal.sub)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeSheet = false }) {
                    Text("সম্পন্ন", style = HidayahType.BodyMedium, color = pal.brand)
                }
            }
        )
    }

    if (showCitySheet) {
        ModalBottomSheet(onDismissRequest = { showCitySheet = false }, containerColor = pal.card) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                Text("শহর নির্বাচন করুন", style = HidayahType.H2, color = pal.ink)
                Spacer(Modifier.height(4.dp))
                Text("নামাজের সময় ও কিবলা এই শহরের ভিত্তিতে হিসাব হবে", style = HidayahType.Caption, color = pal.sub)
                Spacer(Modifier.height(10.dp))
                // জিপিএস থেকে স্বয়ংক্রিয় শহর — পারমিশন চেয়ে নিকটতম শহর সেট করবে
                Surface(
                    shape = HidayahShapes.Small,
                    color = pal.brandMist,
                    modifier = Modifier.fillMaxWidth().clickable {
                        showCitySheet = false
                        requestGpsCity()
                    }
                ) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.MyLocation, contentDescription = null, tint = pal.brand)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (settings.useGpsLocation) "জিপিএস অবস্থান (সক্রিয়)" else "জিপিএস থেকে সেট করুন",
                                style = HidayahType.BodyMedium, color = pal.ink
                            )
                            Text(
                                if (settings.useGpsLocation) settings.gpsLabel.ifBlank { "বর্তমান অবস্থান" }
                                else "বাংলাদেশের বাইরের ব্যবহারকারীদের জন্যও",
                                style = HidayahType.Caption, color = pal.sub
                            )
                        }
                        if (settings.useGpsLocation) Icon(Icons.Filled.Check, contentDescription = "সক্রিয়", tint = pal.brand)
                    }
                }
                Spacer(Modifier.height(10.dp))
                LazyColumn(Modifier.heightIn(max = 380.dp)) {
                    items(Cities.ALL, key = { it.key }) { city ->
                        val selected = !settings.useGpsLocation && city.key == settings.cityKey
                        Surface(
                            shape = HidayahShapes.Small,
                            color = if (selected) Emerald else pal.background,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp).clickable {
                                viewModel.setCity(city.key); showCitySheet = false
                            }
                        ) {
                            Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(city.nameBn, style = HidayahType.BodyMedium, color = if (selected) Cream else pal.ink, modifier = Modifier.weight(1f))
                                if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Gold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMadhabSheet) {
        AlertDialog(
            onDismissRequest = { showMadhabSheet = false },
            containerColor = pal.card,
            title = { Text("মাযহাব নির্বাচন করুন", style = HidayahType.H2, color = pal.ink) },
            text = {
                Column {
                    MadhabOption(
                        title = "হানাফি",
                        subtitle = "আসর: ছায়া নিজের দৈর্ঘ্যের দ্বিগুণ হলে",
                        selected = settings.hanafiAsr,
                        onClick = { viewModel.setHanafiAsr(true); showMadhabSheet = false }
                    )
                    Spacer(Modifier.height(8.dp))
                    MadhabOption(
                        title = "শাফেয়ী / মালিকী / হাম্বলী",
                        subtitle = "আসর: ছায়া নিজের দৈর্ঘ্যের সমান হলে",
                        selected = !settings.hanafiAsr,
                        onClick = { viewModel.setHanafiAsr(false); showMadhabSheet = false }
                    )
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Card(shape = HidayahShapes.Medium, colors = CardDefaults.cardColors(containerColor = pal.card)) {
        Column(Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = pal.brand, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = HidayahType.BodyMedium, color = pal.ink)
            Text(subtitle, style = HidayahType.Caption, color = pal.sub)
        }
        Row(verticalAlignment = Alignment.CenterVertically) { trailing() }
    }
}

@Composable
private fun ToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = pal.brand, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = HidayahType.BodyMedium, color = pal.ink)
            Text(subtitle, style = HidayahType.Caption, color = pal.sub)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = Emerald,
                checkedThumbColor = Gold,
                checkedBorderColor = pal.brandOnSurface,
                uncheckedTrackColor = pal.background,
                uncheckedBorderColor = pal.line
            )
        )
    }
}

@Composable
private fun MadhabOption(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = HidayahShapes.Medium,
        color = if (selected) Emerald else pal.background,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (selected) Emerald else pal.line),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = HidayahType.BodyMedium, color = if (selected) Cream else pal.ink)
                Text(subtitle, style = HidayahType.Caption, color = if (selected) OnEmMuted else pal.sub)
            }
            if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Gold)
        }
    }
}


/** ভাষা প্রয়োগ — DataStore + AppCompat per-app locale (API 25 পর্যন্ত autoStoreLocales দিয়ে)। */
private fun applyLanguage(value: String, viewModel: HidayahViewModel, context: android.content.Context) {
    viewModel.setLanguage(value)
    val tags = when (value) {
        "bn" -> "bn"
        "en" -> "en"
        else -> ""
    }
    androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
        androidx.core.os.LocaleListCompat.forLanguageTags(tags)
    )
    if (value == "en") {
        android.widget.Toast.makeText(context, "English UI আসছে — পুরো অনুবাদের কাজ চলছে", android.widget.Toast.LENGTH_SHORT).show()
    }
}
