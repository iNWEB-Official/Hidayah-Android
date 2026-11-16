package com.hidayah.android.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hidayah.android.ui.theme.*
import java.io.File

/**
 * ইউজারের বাছাই করা প্রোফাইল ছবি দেখায়। সমর্থিত মান:
 *  - ""              → নামের প্রথম অক্ষর, নাম খালি হলে Person আইকন (অতিথি/লগইন-ছাড়া)
 *  - "default:N"     → অ্যাপে থাকা ডিফল্ট ছবি (DefaultAvatars)
 *  - অন্যথায়         → অ্যাপ-প্রাইভেট ফাইলে সংরক্ষিত পরম পাথ
 */
@Composable
fun ProfileAvatar(
    imagePath: String,
    displayName: String,
    size: Dp,
    onClick: (() -> Unit)? = null
) {
    val base = Modifier
        .size(size)
        .clip(CircleShape)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    val defaultRes = DefaultAvatars.resIdFor(imagePath)
    if (defaultRes != null) {
        Image(
            painter = painterResource(defaultRes),
            contentDescription = "প্রোফাইল ছবি",
            contentScale = ContentScale.Crop,
            modifier = base.border(2.dp, Gold.copy(alpha = 0.6f), CircleShape)
        )
        return
    }

    val bitmap = remember(imagePath) {
        if (imagePath.isBlank()) return@remember null
        val f = File(imagePath)
        if (!f.exists()) return@remember null
        runCatching {
            val opts = BitmapFactory.Options().apply { inSampleSize = 4 } // আভাতার ছোট — সম্পূর্ণ ডিকোড লাগে না
            BitmapFactory.decodeFile(f.absolutePath, opts)?.asImageBitmap()
        }.getOrNull()
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "প্রোফাইল ছবি",
            contentScale = ContentScale.Crop,
            modifier = base.border(2.dp, Gold.copy(alpha = 0.6f), CircleShape)
        )
    } else {
        Box(
            base.background(Brush.linearGradient(listOf(Gold, GoldDeep))),
            contentAlignment = Alignment.Center
        ) {
            val initial = displayName.trim().firstOrNull()?.toString()
            if (!initial.isNullOrBlank()) {
                Text(initial, style = HidayahType.H2.copy(fontSize = (size.value * 0.42f).sp), color = EmeraldDark)
            } else {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = "অতিথি",
                    tint = EmeraldDark,
                    modifier = Modifier.size(size * 0.62f)
                )
            }
        }
    }
}

/** পিক করা ছবি অ্যাপ-প্রাইভেট `filesDir/profile/<fileName>`-এ কপি করে পরম পাথ ফেরত দেয় (null = ব্যর্থতা)। */
fun saveProfilePhoto(context: Context, uri: Uri, fileName: String = "avatar.jpg"): String? = runCatching {
    val dir = File(context.filesDir, "profile").apply { mkdirs() }
    val out = File(dir, fileName)
    context.contentResolver.openInputStream(uri)!!.use { input ->
        out.outputStream().use { output -> input.copyTo(output) }
    }
    out.absolutePath
}.getOrNull()
