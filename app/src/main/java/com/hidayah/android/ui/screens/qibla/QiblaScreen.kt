package com.hidayah.android.ui.screens.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hidayah.android.state.HidayahViewModel
import com.hidayah.android.ui.components.*
import com.hidayah.android.ui.theme.*
import com.hidayah.android.util.BnDigits
import kotlin.math.*

private val pal: HidayahPalette @Composable get() = LocalHidayahPalette.current

private const val KAABA_LAT = 21.4225
private const val KAABA_LNG = 39.8262

@Composable
fun QiblaScreen(viewModel: HidayahViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    val bearing = remember(settings.cityKey) { viewModel.cityBearing(settings.cityKey) }
    val distanceKm = remember(settings.cityKey) { viewModel.cityDistanceKm(settings.cityKey) }

    // ---------- Sensor fusion: rotation vector (or accel+mag fallback) ----------
    var azimuthDeg by remember { mutableFloatStateOf(0f) }
    var accuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_UNRELIABLE) }
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    DisposableEffect(Unit) {
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var hasGravity = false; var hasGeomag = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        val R = FloatArray(9)
                        SensorManager.getRotationMatrixFromVector(R, event.values)
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(R, orientation)
                        azimuthDeg = ((Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f)
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        lowPass(event.values, gravity, 0.15f); hasGravity = true
                        updateFromAccelMag(gravity, geomagnetic, hasGeomag)?.let { azimuthDeg = it }
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        lowPass(event.values, geomagnetic, 0.15f); hasGeomag = true
                        updateFromAccelMag(gravity, geomagnetic, hasGravity)?.let { azimuthDeg = it }
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, acc: Int) { accuracy = acc }
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            accel?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
            mag?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        }
        onDispose { sensorManager.unregisterListener(listener) }
    }

    Column(Modifier.fillMaxSize().background(pal.background)) {
        ScreenTitle(
            title = "কিবলা",
            subtitle = "কাবার পবিত্র দিক নির্ণয়",
            centered = true,
            modifier = Modifier.statusBarsPadding().padding(top = 10.dp, bottom = 4.dp)
        )
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(4.dp))
            // ---------- Compass dial ----------
            CompassDial(
                azimuthDeg = azimuthDeg,
                qiblaBearingDeg = bearing.toFloat(),
                modifier = Modifier.size(280.dp)
            )
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = HidayahShapes.Pill, color = Emerald) {
                    Row(Modifier.padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Navigation, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("কিবলার দিক  ${BnDigits.of(bearing.roundToInt())}°", style = HidayahType.TitleSans, color = Cream)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth()) {
                InfoTile(Modifier.weight(1f), "দূরত্ব", "${BnDigits.of(distanceKm.roundToInt())} কিমি", "ঢাকা থেকে কাবা")
                Spacer(Modifier.width(10.dp))
                InfoTile(Modifier.weight(1f), "দিক", cardinalBn(bearing), "উত্তর থেকে ঘড়ির কাঁটায়")
            }
            Spacer(Modifier.height(14.dp))
            if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
                Surface(shape = HidayahShapes.Medium, color = GoldSoft.copy(alpha = 0.4f), border = androidx.compose.foundation.BorderStroke(1.dp, GoldDeep.copy(alpha = 0.5f))) {
                    Text(
                        "কম্পাস অস্থির মনে হলে ফোনটি ৮-এর আকৃতিতে ঘুরিয়ে ক্যালিব্রেট করুন। ধাতব বস্তু ও ইলেকট্রনিক্স থেকে দূরে থাকুন।",
                        style = HidayahType.Caption, color = pal.ink,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "সূক্ষ্মতার জন্য ফোনটি সমতলে ধরুন এবং ধীরে ধীরে ঘোরান। অবস্থান: ${com.hidayah.android.data.Cities.byKey(settings.cityKey).nameBn} (সেটিংসে পরিবর্তনযোগ্য)।",
                style = HidayahType.Caption, color = pal.sub
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun lowPass(input: FloatArray, output: FloatArray, alpha: Float) {
    for (i in input.indices) output[i] = output[i] + alpha * (input[i] - output[i])
}

private fun updateFromAccelMag(gravity: FloatArray, geomagnetic: FloatArray, ready: Boolean): Float? {
    if (!ready) return null
    val R = FloatArray(9); val I = FloatArray(9)
    if (!SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) return null
    val orientation = FloatArray(3)
    SensorManager.getOrientation(R, orientation)
    return (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f
}

private fun cardinalBn(bearing: Double): String = when {
    bearing >= 337.5 || bearing < 22.5 -> "উত্তর"
    bearing < 67.5 -> "উত্তর-পূর্ব"
    bearing < 112.5 -> "পূর্ব"
    bearing < 157.5 -> "দক্ষিণ-পূর্ব"
    bearing < 202.5 -> "দক্ষিণ"
    bearing < 247.5 -> "দক্ষিণ-পশ্চিম"
    bearing < 292.5 -> "পশ্চিম"
    else -> "উত্তর-পশ্চিম"
}

@Composable
private fun InfoTile(modifier: Modifier, label: String, value: String, caption: String) {
    Card(modifier, shape = HidayahShapes.Medium, colors = CardDefaults.cardColors(containerColor = pal.card)) {
        Column(Modifier.padding(14.dp)) {
            Text(label, style = HidayahType.Overline, color = pal.sub)
            Spacer(Modifier.height(6.dp))
            Text(value, style = HidayahType.H2, color = pal.brand)
            Text(caption, style = HidayahType.Caption, color = pal.sub)
        }
    }
}

@Composable
private fun CompassDial(azimuthDeg: Float, qiblaBearingDeg: Float, modifier: Modifier = Modifier) {
    val dialRing = pal.brand
    val dialFace = pal.brandOnSurface
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f
            // outer ring + face
            drawCircle(color = EmeraldDark, radius = radius, center = c)
            drawCircle(color = dialRing, radius = radius * 0.94f, center = c)
            drawCircle(color = dialFace, radius = radius * 0.80f, center = c)
            // degree ticks
            for (deg in 0 until 360 step 6) {
                val major = deg % 30 == 0
                val rad = Math.toRadians(deg.toDouble())
                val outer = radius * 0.90f
                val inner = radius * (if (major) 0.78f else 0.84f)
                val x1 = c.x + (outer * sin(rad)).toFloat()
                val y1 = c.y - (outer * cos(rad)).toFloat()
                val x2 = c.x + (inner * sin(rad)).toFloat()
                val y2 = c.y - (inner * cos(rad)).toFloat()
                drawLine(
                    color = if (major) Gold else OnEmMuted.copy(alpha = 0.6f),
                    start = Offset(x1, y1), end = Offset(x2, y2),
                    strokeWidth = if (major) 3.5f else 1.6f
                )
            }
            // qibla marker on the dial ring (rotates with azimuth)
            val rel = Math.toRadians((qiblaBearingDeg - azimuthDeg).toDouble())
            val kx = c.x + (radius * 0.72f * sin(rel)).toFloat()
            val ky = c.y - (radius * 0.72f * cos(rel)).toFloat()
            drawCircle(color = Gold, radius = 9f, center = Offset(kx, ky))
            drawCircle(color = EmeraldDark, radius = 4.5f, center = Offset(kx, ky))
        }
        // Cardinal letters as plain Texts, rotating with the rose
        CardinalLabels(azimuthDeg)
        // fixed top pointer (heading marker)
        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val topY = size.height / 2f - size.minDimension / 2f + 4f
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(c.x - 11f, topY)
                lineTo(c.x + 11f, topY)
                lineTo(c.x, topY + 20f)
                close()
            }
            drawPath(path, Gold)
        }
        // Kaaba glyph at center
        KaabaGlyph()
    }
}

@Composable
private fun CardinalLabels(azimuthDeg: Float) {
    Box(Modifier.fillMaxSize().rotate(-azimuthDeg)) {
        Box(Modifier.fillMaxSize()) {
            listOf("উ" to 0f, "পূ" to 90f, "দ" to 180f, "প" to 270f).forEach { (label, deg) ->
                val rad = Math.toRadians(deg.toDouble())
                val densityOffset = 96f
                // place labels around the dial using alignment math
                val x = (sin(rad) * densityOffset).toFloat()
                val y = (-cos(rad) * densityOffset).toFloat()
                Box(
                    Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        style = HidayahType.TitleSans.copy(fontSize = 18.sp),
                        color = if (label == "উ") Gold else Cream,
                        modifier = Modifier.offset(x = x.dp.plus(0.dp), y = y.dp.plus(0.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun KaabaGlyph() {
    Canvas(Modifier.size(64.dp)) {
        val w = size.width
        // kaaba cube (isometric-ish)
        val body = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.18f, w * 0.34f)
            lineTo(w * 0.50f, w * 0.16f)
            lineTo(w * 0.82f, w * 0.34f)
            lineTo(w * 0.82f, w * 0.70f)
            lineTo(w * 0.50f, w * 0.88f)
            lineTo(w * 0.18f, w * 0.70f)
            close()
        }
        drawPath(body, Color(0xFF171310))
        val band = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.18f, w * 0.42f)
            lineTo(w * 0.50f, w * 0.24f)
            lineTo(w * 0.82f, w * 0.42f)
            lineTo(w * 0.82f, w * 0.52f)
            lineTo(w * 0.50f, w * 0.34f)
            lineTo(w * 0.18f, w * 0.52f)
            close()
        }
        drawPath(band, Gold)
    }
}
