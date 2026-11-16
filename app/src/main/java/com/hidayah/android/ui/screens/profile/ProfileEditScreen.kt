package com.hidayah.android.ui.screens.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hidayah.android.state.HidayahViewModel
import com.hidayah.android.ui.components.*
import com.hidayah.android.ui.theme.*

private val pal: HidayahPalette @Composable get() = LocalHidayahPalette.current

/**
 * প্রোফাইল এডিট — ফেসবুকের "Edit Profile" পেজের ধাঁচে: এক পেজে ছবি (ডিফল্ট ছবি বা গ্যালারি),
 * নাম ও শহরে দ্রুত শর্টকাট। পরিবর্তন তাৎক্ষণিকভাবে সেভ হয়; উপরে ✅ দিয়ে বের হতে পারবেন।
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    viewModel: HidayahViewModel,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    var nameDraft by remember(settings.userName) { mutableStateOf(settings.userName) }

    // গ্যালারি থেকে ছবি — অনুমতি লাগে না (Photo Picker), অ্যাপ-প্রাইভেট ফাইলে সংরক্ষিত হয়
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { u ->
            val path = saveProfilePhoto(context, u)
            if (path != null) {
                viewModel.setProfileImagePath(path)
                Toast.makeText(context, "ছবি সেট হয়েছে", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "ছবি সেভ করা যায়নি", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val launchGallery = {
        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Column(Modifier.fillMaxSize().background(pal.background)) {
        // ============ শীর্ষ বার ============
        Surface(color = pal.background.copy(alpha = 0.96f)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "পেছনে যান", tint = pal.ink)
                }
                Text(
                    "প্রোফাইল এডিট",
                    style = HidayahType.TitleSans.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
                    color = pal.ink,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { viewModel.setUserName(nameDraft); onBack() }
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(17.dp), tint = pal.brand)
                    Spacer(Modifier.width(4.dp))
                    Text("সম্পন্ন", color = pal.brand, fontWeight = FontWeight.Bold)
                }
            }
        }
        HorizontalDivider(color = pal.sub.copy(alpha = 0.15f))

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ============ সেকশন: প্রোফাইল ছবি ============
            SectionLabel("প্রোফাইল ছবি")
            Spacer(Modifier.height(8.dp))
            Text(
                "নিচের ডিফল্ট ছবিগুলো থেকে বেছে নিন, অথবা গ্যালারি থেকে নিজের ছবি দিন",
                style = HidayahType.Caption, color = pal.sub
            )
            Spacer(Modifier.height(12.dp))

            // ৬টি ডিফল্ট ছবি + ১টি "গ্যালারি" টাইল — ৪ কলমে গ্রিড
            val selectedDefault = settings.profileImagePath
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val tiles = DefaultAvatars.ALL + null // null = গ্যালারি টাইল
                tiles.chunked(4).forEach { rowTiles ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowTiles.forEach { avatar ->
                            if (avatar == null) {
                                GalleryTile(onClick = launchGallery, modifier = Modifier.weight(1f))
                            } else {
                                DefaultAvatarTile(
                                    resId = avatar.resId,
                                    label = avatar.label,
                                    selected = selectedDefault == avatar.id,
                                    onClick = {
                                        viewModel.setProfileImagePath(avatar.id)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        repeat(4 - rowTiles.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
            if (settings.profileImagePath.isNotBlank() && !settings.profileImagePath.startsWith("default:")) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { viewModel.setProfileImagePath("") }) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = pal.sub)
                    Spacer(Modifier.width(6.dp))
                    Text("ছবি সরিয়ে ডিফল্ট আভাতারে ফিরুন", style = HidayahType.Caption, color = pal.sub)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ============ সেকশন: নাম ============
            SectionLabel("নাম")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = nameDraft,
                onValueChange = { if (it.length <= 60) nameDraft = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("যেমন: রাহিম উদ্দিন") },
                label = { Text("আপনার নাম") },
                supportingText = { Text("খালি রাখলে হোমপেজে 'অতিথি' দেখাবে (${nameDraft.length}/৬০)") },
                shape = HidayahShapes.Medium
            )

            Spacer(Modifier.height(20.dp))

            // ============ সেকশন: শর্টকাট ============
            SectionLabel("আরও সেটিংস")
            Spacer(Modifier.height(6.dp))
            ShortcutRow(Icons.Filled.Place, "শহর (জিপিএস বা ম্যানুয়াল)", "সেটিংসের শহরে বদলান", onOpenSettings)
            ShortcutRow(Icons.Filled.Mosque, "মাযহাব (হানাফি/শাফেয়ী)", "সেটিংসের মাযহাবে বদলান", onOpenSettings)
            ShortcutRow(Icons.Filled.DarkMode, "থিম (সিস্টেম/লাইট/ডার্ক)", "সেটিংসের থিমে বদলান", onOpenSettings)

            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun DefaultAvatarTile(
    resId: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            androidx.compose.foundation.Image(
                painter = painterResource(resId),
                contentDescription = label,
                modifier = Modifier
                    .size(64.dp)
                    .clip(HidayahShapes.Card)
                    .border(
                        width = if (selected) 3.dp else 1.dp,
                        color = if (selected) Gold else pal.sub.copy(alpha = 0.25f),
                        shape = HidayahShapes.Card
                    )
                    .clickable(onClick = onClick)
            )
            if (selected) {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 5.dp, y = 5.dp)
                        .size(22.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Gold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Check, contentDescription = "নির্বাচিত", tint = EmeraldDark, modifier = Modifier.size(13.dp))
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = HidayahType.Caption.copy(fontSize = 9.5.sp), color = pal.sub, maxLines = 1)
    }
}

@Composable
private fun GalleryTile(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(64.dp)
                .clip(HidayahShapes.Card)
                .background(pal.brandOnSurface)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.PhotoLibrary, contentDescription = "গ্যালারি থেকে ছবি বেছে নিন", tint = Cream, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text("গ্যালারি", style = HidayahType.Caption.copy(fontSize = 9.5.sp), color = pal.sub, maxLines = 1)
    }
}

@Composable
private fun ShortcutRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    caption: String,
    onClick: () -> Unit
) {
    Surface(
        color = pal.card,
        shape = HidayahShapes.Medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = pal.brand, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = HidayahType.BodyMedium, color = pal.ink)
                Text(caption, style = HidayahType.Caption.copy(fontSize = 10.5.sp), color = pal.sub)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = pal.sub, modifier = Modifier.size(20.dp))
        }
    }
}
