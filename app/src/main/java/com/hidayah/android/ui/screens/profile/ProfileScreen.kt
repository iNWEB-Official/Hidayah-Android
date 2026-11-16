package com.hidayah.android.ui.screens.profile

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hidayah.android.BuildConfig
import com.hidayah.android.data.Cities
import com.hidayah.android.state.HidayahViewModel
import com.hidayah.android.R
import com.hidayah.android.ui.components.*
import com.hidayah.android.ui.theme.*
import com.hidayah.android.util.BnDigits
import java.time.Instant
import java.time.ZoneId

private val pal: HidayahPalette @Composable get() = LocalHidayahPalette.current

/**
 * পূর্ণাঙ্গ ইউজার প্রোফাইল — ফেসবুকের ধাঁচে:
 * কভার (ইউজার-বাছাই/ডিফল্ট) + ঝোলা আভাতার (এডিট বাজ) + নাম + বায়ো + সদস্যপদ-তারিখ +
 * সম্পন্নতার প্রগ্রেস + পরিসংখ্যান (তসবিহ/দোয়া/শেষ-পড়া) + বোতাম সারি + তথ্য তালিকা।
 * সম্পূর্ণ ডিভাইস-স্থানীয় — কোনো লগইন/সার্ভার নেই।
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: HidayahViewModel,
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val locationName = remember(settings.cityKey, settings.useGpsLocation, settings.gpsLabel, settings.gpsLatitude) {
        if (settings.useGpsLocation) settings.gpsLabel.ifBlank { "বর্তমান অবস্থান (জিপিএস)" } else Cities.byKey(settings.cityKey).nameBn
    }
    val isGuest = settings.userName.isBlank()
    val displayName = settings.userName.trim().ifBlank { "অতিথি" }
    val madhab = if (settings.hanafiAsr) "হানাফি মাযহাব" else "শাফেয়ী মাযহাব"
    val themeLabel = when (settings.theme) {
        "light" -> "লাইট থিম"
        "dark" -> "ডার্ক থিম"
        else -> "সিস্টেম থিম"
    }
    val bio = settings.profileBio.ifBlank { "আসসালামু আলাইকুম ওয়ারাহমাতুল্লাহ" }

    // সম্পন্নতার হিসাব — নাম, প্রোফাইল ছবি, বায়ো (৩টি)
    val doneSteps = listOf(
        settings.userName.isNotBlank(),
        settings.profileImagePath.isNotBlank(),
        settings.profileBio.isNotBlank()
    ).count { it }

    Column(
        Modifier
            .fillMaxSize()
            .background(pal.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ============ কভার (ইউজারের বাছাই করা বা ডিফল্ট) ============
        Box(Modifier.fillMaxWidth().height(190.dp)) {
            CoverImage(
                path = settings.coverImagePath,
                defaultRes = R.drawable.bg_next_prayer,
                modifier = Modifier.fillMaxSize(),
                contentDescription = "কভার ছবি"
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0x33000000), Color(0x99000000), pal.background.copy(alpha = 0.25f))))
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier.statusBarsPadding().padding(top = 4.dp, start = 4.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "পেছনে যান", tint = Cream)
            }
            // কভারে "শেয়ার" — অ্যাপ আমন্ত্রণ
            IconButton(
                onClick = {
                    val shareText = "আমি হিদায়াহ অ্যাপ ব্যবহার করছি — নামাজের সময়, আল-কুরআন, কিবলা, তসবিহ — সব এক জায়গায়।\nhttps://play.google.com/store/apps/details?id=com.hidayah.android"
                    runCatching {
                        context.startActivity(
                            Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }, "হিদায়াহ শেয়ার করুন")
                        )
                    }.onFailure {
                        Toast.makeText(context, "শেয়ার করা যায়নি", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(top = 4.dp, end = 4.dp)
            ) {
                Icon(Icons.Filled.Share, contentDescription = "হিদায়াহ শেয়ার করুন", tint = Cream)
            }
            Text(
                "هِدَايَة",
                style = HidayahType.Arabic.copy(fontSize = 30.sp),
                color = Gold.copy(alpha = 0.95f),
                modifier = Modifier.align(Alignment.Center).padding(bottom = 30.dp)
            )
        }

        // ============ আভাতার (কভারের সাথে ঝোলানো + এডিট বাজ) ============
        Box(Modifier.fillMaxWidth().height(54.dp)) {
            Box(Modifier.offset(x = 20.dp).align(Alignment.TopStart).offset(y = (-52).dp)) {
                ProfileAvatar(
                    imagePath = settings.profileImagePath,
                    displayName = settings.userName,
                    size = 108.dp
                )
                // ফেসবুকের মতো ক্যামেরা-বাজ — ট্যাপে সরাসরি এডিট পেজ
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Gold)
                        .clickable(onClick = onEditProfile),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = "প্রোফাইল ছবি বদলান", tint = EmeraldDark, modifier = Modifier.size(17.dp))
                }
            }
        }

        // ============ নাম / বায়ো / সদস্যপদ ============
        Column(Modifier.padding(horizontal = 20.dp)) {
            Text(displayName, style = HidayahType.H1.copy(fontSize = 24.sp), color = pal.ink)
            Text(
                bio,
                style = HidayahType.Body, color = pal.sub,
                modifier = Modifier.padding(top = 2.dp)
            )
            val sinceText = memberSinceBn(settings.memberSince)
            if (sinceText.isNotBlank()) {
                Text(
                    "সদস্য: $sinceText থেকে",
                    style = HidayahType.Caption, color = pal.sub,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }

            // ============ সম্পন্নতার প্রগ্রেস (অসম্পূর্ণ প্রোফাইলে) ============
            if (doneSteps < 3) {
                Spacer(Modifier.height(10.dp))
                Surface(shape = HidayahShapes.Medium, color = GoldSoft.copy(alpha = 0.35f)) {
                    Column(Modifier.padding(12.dp).fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "প্রোফাইল সম্পন্ন করুন — ${BnDigits.of(doneSteps)}/৩ ধাপ",
                                style = HidayahType.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = EmeraldDark,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "এডিট »",
                                style = HidayahType.BodyMedium.copy(fontWeight = FontWeight.Bold, color = pal.brand),
                                modifier = Modifier.clickable(onClick = onEditProfile)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { doneSteps / 3f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(HidayahShapes.Pill),
                            color = Emerald,
                            trackColor = Emerald.copy(alpha = 0.15f)
                        )
                    }
                }
            }

            // ============ বোতাম সারি (ফেসবুকের ধাঁচে) ============
            Row(Modifier.padding(top = 12.dp)) {
                Button(
                    onClick = onEditProfile,
                    shape = HidayahShapes.Pill,
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = EmeraldDark),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp))
                    Text(if (isGuest) "প্রোফাইল তৈরি করুন" else "এডিট প্রোফাইল", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(10.dp))
                OutlinedButton(
                    onClick = onOpenSettings,
                    shape = HidayahShapes.Pill,
                    border = androidx.compose.foundation.BorderStroke(1.dp, pal.sub.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(17.dp), tint = pal.ink)
                    Spacer(Modifier.width(7.dp))
                    Text("সেটিংস", color = pal.ink)
                }
            }

            // ============ পরিসংখ্যান (সত্যিকার ডেটা) ============
            Spacer(Modifier.height(16.dp))
            Surface(shape = HidayahShapes.Card, color = pal.card) {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatCell(BnDigits.of(settings.tasbihTodayTotal), "আজকের তসবিহ")
                    StatDivider()
                    StatCell(BnDigits.of(settings.favoriteDuaIds.size), "ফেভারিট দোয়া")
                    StatDivider()
                    StatCell("সূরা ${BnDigits.of(settings.lastReadSurah)}", "শেষ পড়া")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ============ তথ্য (ফেসবুকের "Details" তালিকা) ============
        Column(Modifier.padding(horizontal = 20.dp)) {
            SectionLabel("তথ্য")
            Spacer(Modifier.height(8.dp))
            DetailRow(Icons.Filled.Place, "অবস্থান", locationName) { onOpenSettings() }
            DetailRow(Icons.Filled.Mosque, "মাযহাব", madhab) { onOpenSettings() }
            DetailRow(Icons.Filled.DarkMode, "থিম", themeLabel) { onOpenSettings() }
        }

        Spacer(Modifier.height(26.dp))
        Text(
            "হিদায়াহ v${BuildConfig.VERSION_NAME}",
            style = HidayahType.Caption, color = pal.sub,
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun StatCell(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = HidayahType.H2.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold), color = pal.brand)
        Text(label, style = HidayahType.Caption, color = pal.sub)
    }
}

@Composable
private fun StatDivider() {
    Box(Modifier.width(1.dp).height(34.dp).background(pal.line))
}

private fun memberSinceBn(epochMillis: Long): String {
    if (epochMillis <= 0L) return ""
    val d = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    val months = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )
    return "${months[d.monthValue - 1]} ${BnDigits.of(d.year)}"
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, onClick: () -> Unit) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = true, onClick = onClick)
    ) {
        Row(
            Modifier.padding(vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(36.dp).clip(HidayahShapes.Small).background(Emerald.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, contentDescription = null, tint = pal.brand, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, style = HidayahType.Caption, color = pal.sub)
                Text(value, style = HidayahType.BodyMedium, color = pal.ink)
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = pal.sub, modifier = Modifier.size(20.dp))
        }
    }
    HorizontalDivider(color = pal.sub.copy(alpha = 0.14f))
}
