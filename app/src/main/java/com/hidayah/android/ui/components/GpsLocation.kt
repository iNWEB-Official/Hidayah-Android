package com.hidayah.android.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.hidayah.android.data.Cities

/**
 * "জিপিএস থেকে অবস্থান সেট করুন" — পারমিশন চেয়ে, LocationManager (Play Services ছাড়া, অফলাইনে কাজ করে)
 * থেকে nearest জনপ্রিয় শহর বেছে onCityKey(key|null) কল করে।
 */
@Composable
fun rememberNearestCityRequester(onCityKey: (String?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) readGpsThenApply(context, onCityKey) else onCityKey(null)
    }
    return remember(context, onCityKey, launcher) {
        {
            val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            when {
                fine || coarse -> readGpsThenApply(context, onCityKey)
                else -> launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
}

private fun readGpsThenApply(context: Context, onCityKey: (String?) -> Unit) {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    if (lm == null) { onCityKey(null); return }

    fun bestLastKnown(): Location? =
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .mapNotNull { p ->
                runCatching { if (lm.isProviderEnabled(p)) lm.getLastKnownLocation(p) else null }.getOrNull()
            }
            .maxByOrNull { it.time }

    bestLastKnown()?.let {
        onCityKey(Cities.nearestKey(it.latitude, it.longitude))
        return
    }

    // কোনো cached অবস্থান নেই — এককালীন আপডেট, ৮ সেকেন্ডে টাইমআউট
    val provider = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        .firstOrNull { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }
    if (provider == null) { onCityKey(null); return }

    var done = false
    val timeoutMs = 8_000L
    val listener = object : LocationListener {
        override fun onLocationChanged(loc: Location) {
            if (done) return
            done = true
            runCatching { lm.removeUpdates(this) }
            onCityKey(Cities.nearestKey(loc.latitude, loc.longitude))
        }
    }
    val handler = android.os.Handler(Looper.getMainLooper())
    handler.postDelayed({
        if (!done) {
            done = true
            runCatching { lm.removeUpdates(listener) }
            onCityKey(null)
        }
    }, timeoutMs)

    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            lm.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
        } else {
            @Suppress("DEPRECATION")
            lm.requestLocationUpdates(provider, 0L, 0f, listener)
        }
    }.onFailure { onCityKey(null) }
}

/** ব্যবহারকারীকে জানিয়ে কী হবে: সফল হলে শহরের নাম, ব্যর্থ হলে বার্তা — বাস্তবায়ন স্ক্রিন-সাইডে টোস্টে। */
fun gpsResultMessage(context: Context, cityKey: String?): String = if (cityKey != null) {
    "অবস্থান সেট হয়েছে: ${com.hidayah.android.data.Cities.byKey(cityKey).nameBn}"
} else {
    "জিপিএস থেকে অবস্থান পাওয়া যায়নি — পারমিশন/লোকেশন সার্ভিস চালু আছে কিনা দেখুন"
}
