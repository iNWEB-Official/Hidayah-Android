package com.hidayah.android.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import java.io.File

/**
 * কভার ছবি — অ্যাপ-প্রাইভেট ফাইলে থাকলে সেটাই, অন্যথায় দেওয়া ডিফল্ট রিসোর্স।
 * ফাইল ডিকোড ব্যর্থ হলেও ডিফল্টে ফিরে যায়।
 */
@Composable
fun CoverImage(path: String, defaultRes: Int, modifier: Modifier = Modifier, contentDescription: String? = null) {
    Box(modifier) {
        val bitmap = remember(path) {
            if (path.isBlank()) return@remember null
            val f = File(path)
            if (!f.exists()) return@remember null
            runCatching {
                val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                BitmapFactory.decodeFile(f.absolutePath, opts)?.asImageBitmap()
            }.getOrNull()
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = painterResource(defaultRes),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
