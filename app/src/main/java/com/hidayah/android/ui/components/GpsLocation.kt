package com.hidayah.android.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/** জিপিএস ফিক্স — কো-অর্ডিনেট + রিভার্স-জিওকোডেড লেবেল (ব্যর্থ হলে "বর্তমান অবস্থান")। */
data class GpsFix(val lat: Double, val lng: Double, val label: String)

/**
 * এক-চাপে জিপিএস অবস্থান নেওয়া — পারমিশন চেয়ে (প্রয়োজনে), LocationManager-এর
 * last-known বা এককালীন আপডেট (৮ সেকেন্ড টাইমআউট) → রিভার্স জিওকোড → onResult(fix|null)।
 * Play Services লাগে না; বাংলাদেশের বাইরের ব্যবহারকারীদের জন্যও সঠিক।
 */
@Composable
fun rememberGpsLocationRequester(onResult: (GpsFix?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    fun handleCoords(lat: Double, lng: Double) {
        coroutineScope.launch {
            val label = reverseGeocode(context, lat, lng).ifBlank { "বর্তমান অবস্থান" }
            onResult(GpsFix(lat, lng, label))
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            acquireCoords(context, ::handleCoords) { onResult(null) }
        } else {
            onResult(null)
        }
    }

    return remember(context, onResult, launcher) {
        {
            val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            when {
                fine || coarse -> acquireCoords(context, ::handleCoords) { onResult(null) }
                else -> launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
}

/** পারমিশন যাচাই করে নিকটবর্তী GPS-তৎক্ষণ অনুরোধ (নীরব রিফ্রেশ; অনুমতি না থাকলে কিছুই করে না)। */
@Composable
fun rememberSilentGpsRefresh(onFix: (GpsFix) -> Unit): () -> Unit {
    val context = LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    return remember(context, onFix) {
        {
            val granted = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
            if (granted) {
                acquireCoords(context,
                    { lat, lng ->
                        coroutineScope.launch {
                            val label = reverseGeocode(context, lat, lng).ifBlank { "বর্তমান অবস্থান" }
                            onFix(GpsFix(lat, lng, label))
                        }
                    },
                    {})
            }
        }
    }
}

private fun acquireCoords(context: Context, onCoords: (Double, Double) -> Unit, onFail: () -> Unit) {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    if (lm == null) { onFail(); return }

    fun bestLastKnown(): Location? =
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .mapNotNull { p ->
                runCatching { if (lm.isProviderEnabled(p)) lm.getLastKnownLocation(p) else null }.getOrNull()
            }
            .maxByOrNull { it.time }

    bestLastKnown()?.let { onCoords(it.latitude, it.longitude); return }

    val provider = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        .firstOrNull { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }
    if (provider == null) { onFail(); return }

    var done = false
    val timeoutMs = 8_000L
    val listener = object : LocationListener {
        override fun onLocationChanged(loc: Location) {
            if (done) return
            done = true
            runCatching { lm.removeUpdates(this) }
            onCoords(loc.latitude, loc.longitude)
        }
    }
    val handler = android.os.Handler(Looper.getMainLooper())
    handler.postDelayed({
        if (!done) {
            done = true
            runCatching { lm.removeUpdates(listener) }
            onFail()
        }
    }, timeoutMs)

    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            lm.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
        } else {
            @Suppress("DEPRECATION")
            lm.requestLocationUpdates(provider, 0L, 0f, listener)
        }
    }.onFailure { onFail() }
}

/** রিভার্স জিওকোড — "স্থান, দেশ" (বাংলা লোকালে); ব্যর্থতা/অপেক্ষারত হলে ফাঁকা স্ট্রিং। */
private suspend fun reverseGeocode(context: Context, lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
    runCatching {
        @Suppress("DEPRECATION")
        val geocoder = Geocoder(context, Locale.forLanguageTag("bn"))
        @Suppress("DEPRECATION")
        val a: Address? = geocoder.getFromLocation(lat, lng, 1)?.firstOrNull()
        if (a == null) return@runCatching ""
        val place = a.locality ?: a.subAdminArea ?: a.adminArea
        val country = a.countryName
        listOfNotNull(place, country).joinToString(", ")
    }.getOrDefault("")
}
