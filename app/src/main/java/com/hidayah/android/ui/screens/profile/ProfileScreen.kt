package com.hidayah.android.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hidayah.android.BuildConfig
import com.hidayah.android.data.Cities
import com.hidayah.android.state.HidayahViewModel
import com.hidayah.android.R
import com.hidayah.android.ui.components.*
import com.hidayah.android.ui.theme.*

private val pal: HidayahPalette @Composable get() = LocalHidayahPalette.current

/**
 * ইউজার প্রোফাইল — ফেসবুকের প্রোফাইলের ধাঁচে: কভার ফটো + নিচে ঝোলা গোল আভাতার, নাম, বায়ো লাইন,
 * বোতাম সারি (এডিট/সেটিংস), তারপর "তথ্য" তালিকা। ডেটা সম্পূর্ণ ডিভাইসে সংরক্ষিত — কোনো সার্ভার/লগইন নেই।
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
    val cityName = remember(settings.cityKey, settings.useGpsLocation, settings.gpsLabel, settings.gpsLatitude) {
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

    Column(
        Modifier
            .fillMaxSize()
            .background(pal.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ============ কভার (ফেসবুকের ধাঁচে) ============
        Box(
            Modifier
                .fillMaxWidth()
                .height(190.dp)
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(R.drawable.bg_next_prayer),
                contentDescription = "কভার ছবি",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0x33000000), Color(0x99000000), pal.background.copy(alpha = 0.2f))))
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier.statusBarsPadding().padding(top = 4.dp, start = 4.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "পেছনে যান", tint = Cream)
            }
            // কভারে "হিদায়াহ" লেখা — ব্র্যান্ড পরিচয়
            Text(
                "هِدَايَة",
                style = HidayahType.Arabic.copy(fontSize = 30.sp),
                color = Gold.copy(alpha = 0.95f),
                modifier = Modifier.align(Alignment.Center).padding(bottom = 34.dp)
            )
        }

        // ============ আভাতার (কভারের সাথে ঝোলানো) ============
        Box(Modifier.fillMaxWidth().height(54.dp)) {
            Box(
                Modifier
                    .offset(x = 20.dp)
                    .align(Alignment.TopStart)
                    .offset(y = (-52).dp)
            ) {
                ProfileAvatar(
                    imagePath = settings.profileImagePath,
                    displayName = settings.userName,
                    size = 108.dp
                )
            }
        }

        // ============ নাম ও বায়ো ============
        Column(Modifier.padding(horizontal = 20.dp)) {
            Text(displayName, style = HidayahType.H1.copy(fontSize = 24.sp), color = pal.ink)
            if (isGuest) {
                Text(
                    "অতিথি হিসেবে ব্যবহার করছেন — নাম ও ছবি সেট করা নেই",
                    style = HidayahType.Caption, color = pal.sub,
                    modifier = Modifier.padding(top = 2.dp)
                )
            } else {
                Text(
                    "আসসালামু আলাইকুম ওয়ারাহমাতুল্লাহ",
                    style = HidayahType.Body, color = pal.sub,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // ফেসবুকের ধাঁচে বোতাম সারি
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

            if (isGuest) {
                // ============ গেস্ট ব্যানার ============
                Spacer(Modifier.height(14.dp))
                Surface(shape = HidayahShapes.Medium, color = GoldSoft.copy(alpha = 0.35f)) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            "আপনার নিজস্ব প্রোফাইল তৈরি করুন",
                            style = HidayahType.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = EmeraldDark
                        )
                        Text(
                            "নাম ও ছবি সেট করলে হোমপেজ ও সেটিংসে আপনার পরিচয় দেখাবে। সব তথ্য শুধু আপনার ফোনেই থাকবে — কোনো অ্যাকাউন্ট বা সার্ভারে পাঠানো হয় না।",
                            style = HidayahType.Caption, color = EmeraldDark.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // ============ তথ্য (ফেসবুকের "Details" তালিকা) ============
        Column(Modifier.padding(horizontal = 20.dp)) {
            SectionLabel("তথ্য")
            Spacer(Modifier.height(8.dp))
            DetailRow(Icons.Filled.Place, "অবস্থান", cityName) { onOpenSettings() }
            DetailRow(Icons.Filled.Mosque, "মাযহাব", madhab) { onOpenSettings() }
            DetailRow(Icons.Filled.DarkMode, "থিম", themeLabel) { onOpenSettings() }
            DetailRow(Icons.Filled.Language, "ভাষা", "বাংলা") {}
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
