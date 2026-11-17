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
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Path
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

    // জিপিএস চালু থাকলে আসল কো-অর্ডিনেট, নয়তো নির্বাচিত শহর
    val locationKey = remember(settings.cityKey, settings.useGpsLocation, settings.gpsLatitude, settings.gpsLongitude) {
        if (settings.useGpsLocation) "gps:%.5f,%.5f".format(settings.gpsLatitude, settings.gpsLongitude) else settings.cityKey
    }
    val locationLabel = viewModel.locationLabel(settings)
    val bearing = remember(locationKey) { viewModel.currentBearing(settings) }
    val distanceKm = remember(locationKey) { viewModel.currentDistanceKm(settings) }

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

    val isDark = pal.isDark
    val bgTop = if (isDark) Color(0xFF14231C) else Color(0xFFFBF8ED)
    val bgBottom = if (isDark) Color(0xFF0E1A13) else Color(0xFFF0EBDB)
    val brandTitle = if (isDark) Cream else Color(0xFF0E4D3C)
    val cardFill = if (isDark) pal.card else Color(0xFFFFFFFF).copy(alpha = 0.72f)

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgTop, bgBottom)))
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding()) {
            // ---------- টপ: আর্চ ফ্রেম ----------
            ArchFrame(Modifier.fillMaxWidth().height(74.dp).padding(top = 6.dp))

            // ---------- ব্র্যান্ড হেডার ----------
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MosqueGlyphFilled(Modifier.size(46.dp, 30.dp), if (isDark) Gold else Color(0xFF0E4D3C))
                Text(
                    "হিদায়াহ",
                    style = HidayahType.Display.copy(fontSize = 40.sp, lineHeight = 52.sp, fontWeight = FontWeight.ExtraBold),
                    color = brandTitle
                )
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    GoldLine(Modifier.weight(1f))
                    Text(
                        "কিবলা কম্পাস",
                        style = HidayahType.H2.copy(fontSize = 15.sp, letterSpacing = 1.sp),
                        color = brandTitle,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    GoldLine(Modifier.weight(1f))
                }
                Text(
                    "সহজভাবে খুঁজে নিন কিবলার দিক",
                    style = HidayahType.Caption.copy(fontSize = 12.5.sp),
                    color = pal.sub,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            Spacer(Modifier.height(12.dp))

            // ---------- লোকেশন পিল ----------
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp)
                    .clip(HidayahShapes.Pill)
                    .background(cardFill)
                    .border(1.4.dp, Gold.copy(alpha = 0.85f), HidayahShapes.Pill)
                    .padding(horizontal = 18.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Place, contentDescription = null, tint = Color(0xFF0E4D3C), modifier = Modifier.size(34.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("আপনার অবস্থান", style = HidayahType.Overline.copy(fontSize = 9.5.sp), color = pal.sub)
                    Text(
                        locationLabel,
                        style = HidayahType.TitleSans.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                        color = if (isDark) pal.ink else Color(0xFF0E4D3C),
                        maxLines = 1
                    )
                }
                Box(Modifier.width(1.4.dp).height(36.dp).background(Gold.copy(alpha = 0.7f)))
                Spacer(Modifier.width(14.dp))
                Icon(Icons.Filled.MyLocation, contentDescription = null, tint = Color(0xFF0E4D3C), modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.height(14.dp))

            // ---------- কম্পাস ডায়াল ----------
            CompassDial(
                azimuthDeg = azimuthDeg,
                qiblaBearingDeg = bearing.toFloat(),
                modifier = Modifier.size(292.dp)
            )
            Spacer(Modifier.height(12.dp))

            // ---------- কিবলার দিক ব্যানার ----------
            QiblaBanner(
                bearingDeg = bearing,
                direction = cardinalBn(bearing),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp)
            )
            Spacer(Modifier.height(14.dp))

            // ---------- দূরত্ব + নিকটতম মসজিদ ----------
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .clip(HidayahShapes.Large)
                    .background(cardFill)
                    .border(1.4.dp, Gold.copy(alpha = 0.85f), HidayahShapes.Large)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Filled.Place, contentDescription = null, tint = Color(0xFF0E4D3C), modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("দূরত্ব", style = HidayahType.Overline.copy(fontSize = 9.5.sp), color = pal.sub)
                        Text(
                            "${BnDigits.of(distanceKm.roundToInt())} কিমি",
                            style = HidayahType.TitleSans.copy(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold),
                            color = if (isDark) pal.ink else Color(0xFF0E4D3C)
                        )
                        Text("কাবা পর্যন্ত সরলরেখা", style = HidayahType.Caption.copy(fontSize = 9.5.sp), color = pal.sub)
                    }
                }
                Box(Modifier.width(1.4.dp).height(46.dp).background(Gold.copy(alpha = 0.7f)))
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    MosqueGlyphFilled(Modifier.size(30.dp, 22.dp), if (isDark) Gold else Color(0xFF0E4D3C))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("নিকটতম মসজিদ", style = HidayahType.Overline.copy(fontSize = 9.5.sp), color = pal.sub)
                        Text(
                            "—",
                            style = HidayahType.TitleSans.copy(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold),
                            color = if (isDark) pal.ink else Color(0xFF0E4D3C)
                        )
                        Text("শীঘ্রই আসছে", style = HidayahType.Caption.copy(fontSize = 9.5.sp), color = pal.sub)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))

            if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
                Surface(
                    shape = HidayahShapes.Medium,
                    color = GoldSoft.copy(alpha = if (isDark) 0.22f else 0.4f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldDeep.copy(alpha = 0.5f)),
                    modifier = Modifier.padding(horizontal = 22.dp)
                ) {
                    Text(
                        "কম্পাস অস্থির মনে হলে ফোনটি ৮-এর আকৃতিতে ঘুরিয়ে ক্যালিব্রেট করুন। ধাতব বস্তু ও ইলেকট্রনিক্স থেকে দূরে থাকুন।",
                        style = HidayahType.Caption, color = pal.ink,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // ---------- অলঙ্করণ বিভাজক ----------
            OrnamentDivider(Modifier.fillMaxWidth().padding(horizontal = 60.dp))
            Spacer(Modifier.height(4.dp))

            // ---------- মসজিদ স্কাইলাইন ফুটার ----------
            MosqueSkyline(Modifier.fillMaxWidth().height(58.dp))
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

// ---------- সংযোজন — কিবলা ডিজাইন (২০২৬-১৭; মকাপ-ভিত্তিক) ----------

@Composable
private fun GoldLine(modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(1.3.dp)
            .background(Brush.horizontalGradient(listOf(Color.Transparent, Gold, Gold, Color.Transparent)))
    )
}

@Composable
private fun ArchFrame(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val apexY = h * 0.06f
        val baseY = h * 0.98f
        fun archPath(inset: Float): Path {
            val left = Offset(-w * 0.06f + inset, baseY)
            val right = Offset(w * 1.06f - inset, baseY)
            val apex = Offset(w / 2f, apexY + inset * 0.55f)
            val ctrlL = Offset(w * 0.30f, apexY + h * 0.42f)
            val ctrlR = Offset(w * 0.70f, apexY + h * 0.42f)
            return Path().apply {
                moveTo(left.x, left.y)
                cubicTo(ctrlL.x, ctrlL.y, ctrlL.x, apex.y, apex.x, apex.y)
                cubicTo(ctrlR.x, ctrlR.y, ctrlR.x, ctrlR.y, right.x, right.y)
            }
        }
        drawPath(archPath(0f), Color(0xFF0E4D3C), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5.2f))
        drawPath(archPath(0f), Gold, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.4f))
        drawPath(archPath(7f), Gold, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.3f))
        // শীর্ষ চূড়া — ছোট ফানুস/ফিনিয়াল
        drawLine(Gold, Offset(w / 2f, 0f), Offset(w / 2f, apexY + 2f), 2.2f)
        drawCircle(Gold, 3.2f, Offset(w / 2f, 3f))
        // নিচের দুই প্রান্তে কোণাকার ঘোরান
        drawCircle(Gold, 2.6f, Offset(-w * 0.02f + 2f, baseY * 0.88f))
        drawCircle(Gold, 2.6f, Offset(w * 1.02f - 2f, baseY * 0.88f))
    }
}

@Composable
private fun OrnamentDivider(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val cy = size.height / 2f
        val gold = Gold
        drawLine(gold, Offset(0f, cy), Offset(w / 2f - 26f, cy), 1.4f)
        drawLine(gold, Offset(w / 2f + 26f, cy), Offset(w, cy), 1.4f)
        // কেন্দ্রীয় ডায়মন্ড-পিক ফ্লোরিশ
        val path = Path().apply {
            moveTo(w / 2f - 24f, cy)
            lineTo(w / 2f - 10f, cy - 7f)
            lineTo(w / 2f, cy)
            lineTo(w / 2f + 10f, cy - 7f)
            lineTo(w / 2f + 24f, cy)
            lineTo(w / 2f, cy)
            close()
        }
        drawPath(path, gold)
        drawCircle(gold, 2.4f, Offset(w / 2f - 30f, cy))
        drawCircle(gold, 2.4f, Offset(w / 2f + 30f, cy))
    }
}

@Composable
private fun MosqueGlyphFilled(modifier: Modifier = Modifier, tint: Color = Color(0xFF0E4D3C)) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        // কেন্দ্রীয় গম্বুজ + মিনারদ্বয় — সিলুয়েট
        fun dome(cx: Float, baseY: Float, r: Float): Path {
            return Path().apply {
                moveTo(cx - r, baseY)
                arcTo(Rect(cx - r, baseY - r * 1.55f, cx + r, baseY + r * 0.45f), 180f, 180f, false)
                lineTo(cx + r, baseY)
                close()
            }
        }
        // কেন্দ্রীয় গম্বুজ
        drawPath(dome(w / 2f, h * 0.62f, w * 0.20f), tint)
        drawRect(tint, topLeft = Offset(w / 2f - w * 0.22f, h * 0.62f), size = Size(w * 0.44f, h * 0.26f))
        // ফিনিয়াল (চন্দ্র)
        drawLine(tint, Offset(w / 2f, h * 0.02f), Offset(w / 2f, h * 0.20f), 2.6f)
        drawCircle(tint, h * 0.055f, Offset(w / 2f, h * 0.03f))
        // পার্শ্ব মিনারদুটো
        listOf(w * 0.13f, w * 0.87f).forEach { mx ->
            drawRoundRect(tint, topLeft = Offset(mx - w * 0.055f, h * 0.38f), size = Size(w * 0.11f, h * 0.50f), cornerRadius = CornerRadius(3f))
            drawPath(dome(mx, h * 0.38f, w * 0.085f), tint)
        }
        // ভিত্তি বেস
        drawRect(tint, topLeft = Offset(w * 0.02f, h * 0.86f), size = Size(w * 0.96f, h * 0.10f))
    }
}

@Composable
private fun MosqueSkyline(modifier: Modifier = Modifier) {
    val isDark = pal.isDark
    val tint = if (isDark) Color(0xFF3A5145) else Color(0xFF9DB5A6)
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val base = h * 0.96f
        fun domePath(cx: Float, baseY: Float, r: Float): Path = Path().apply {
            moveTo(cx - r, baseY)
            arcTo(Rect(cx - r, baseY - r * 1.6f, cx + r, baseY + r * 0.4f), 180f, 180f, false)
            lineTo(cx + r, baseY)
            close()
        }
        // [কেন্দ্র, ভিত্তি, গম্বুজ-ব্যাসার্ধ, মিনার?]
        val buildings = listOf(
            Triple(0.06f, 0.30f, 0.055f), Triple(0.16f, 0.44f, 0.075f), Triple(0.28f, 0.52f, 0.090f),
            Triple(0.40f, 0.40f, 0.070f), Triple(0.50f, 0.60f, 0.115f), Triple(0.60f, 0.40f, 0.070f),
            Triple(0.72f, 0.52f, 0.090f), Triple(0.84f, 0.44f, 0.075f), Triple(0.94f, 0.30f, 0.055f)
        )
        buildings.forEach { (cxf, hf, rf) ->
            val cx = w * cxf
            val bh = h * hf
            drawRect(tint, topLeft = Offset(cx - w * rf * 1.15f, base - bh), size = Size(w * rf * 2.3f, bh))
            drawPath(domePath(cx, base - bh, w * rf), tint)
            // মিনার টেনে ধরি
            listOf(cx - w * rf * 1.45f, cx + w * rf * 1.45f).forEach { mx ->
                drawRoundRect(tint, topLeft = Offset(mx - w * 0.016f, base - bh * 1.28f), size = Size(w * 0.032f, bh * 1.28f), cornerRadius = CornerRadius(2f))
                drawCircle(tint, w * 0.022f, Offset(mx, base - bh * 1.28f))
            }
        }
        // বেস লাইন
        drawRect(tint, topLeft = Offset(0f, base), size = Size(w, h - base))
    }
}

@Composable
private fun QiblaBanner(bearingDeg: Double, direction: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF124E3D), Color(0xFF062A20))))
            .border(2.dp, Gold, RoundedCornerShape(26.dp))
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // বাম ডায়মন্ড-ফ্লোরিশ
            Canvas(Modifier.size(30.dp)) {
                val c = size.minDimension / 2f
                drawPath(
                    Path().apply {
                        moveTo(0f, c); lineTo(c * 0.55f, c * 0.28f); lineTo(c, c)
                        lineTo(c * 0.55f, c * 1.72f); close()
                    },
                    Gold
                )
                drawCircle(Gold, c * 0.16f, Offset(c * 0.72f, c))
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "কিবলার দিক",
                    style = HidayahType.H2.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                    color = Cream
                )
                Text(
                    "${BnDigits.of(bearingDeg.roundToInt())}°",
                    style = HidayahType.Display.copy(fontSize = 46.sp, lineHeight = 52.sp, fontWeight = FontWeight.ExtraBold),
                    color = Gold
                )
                Text(
                    "($direction দিকে)",
                    style = HidayahType.Caption.copy(fontSize = 12.sp),
                    color = Cream.copy(alpha = 0.92f)
                )
            }
            Spacer(Modifier.width(12.dp))
            Canvas(Modifier.size(30.dp)) {
                val c = size.minDimension / 2f
                drawPath(
                    Path().apply {
                        moveTo(size.width, c); lineTo(size.width - c * 0.55f, c * 0.28f); lineTo(size.width - c, c)
                        lineTo(size.width - c * 0.55f, c * 1.72f); close()
                    },
                    Gold
                )
                drawCircle(Gold, c * 0.16f, Offset(size.width - c * 0.72f, c))
            }
        }
    }
}

@Composable
private fun CompassDial(azimuthDeg: Float, qiblaBearingDeg: Float, modifier: Modifier = Modifier) {
    val dialSize = 292.dp
    Box(modifier.size(dialSize), contentAlignment = Alignment.Center) {
        // ---------- স্থির বাইরের রিং + ভিত্তি ----------
        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val r = size.minDimension / 2f
            // গোল্ড আউটার রিম
            drawCircle(
                Brush.verticalGradient(listOf(Color(0xFFF3DF9C), Gold, Color(0xFFA8791F), Gold)),
                r, c
            )
            drawCircle(Color(0xFF041A12), r * 0.905f, c)
            drawCircle(Gold, r * 0.872f, c)
            // প্রধান মুখ — গভীর সবুজ রেডিয়াল
            drawCircle(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF155943), Color(0xFF093A2C), Color(0xFF062A20)),
                    center = Offset(c.x, c.y - r * 0.1f),
                    radius = r * 0.86f
                ),
                r * 0.845f, c
            )
        }
        // ---------- আজিমুথ-ঘোরানো রোজ (টিক + দিগদর্শন) ----------
        Box(Modifier.fillMaxSize().rotate(-azimuthDeg)) {
            Canvas(Modifier.fillMaxSize()) {
                val c = Offset(size.width / 2f, size.height / 2f)
                val r = size.minDimension / 2f
                for (deg in 0 until 360 step 6) {
                    val major = deg % 30 == 0
                    val rad = Math.toRadians(deg.toDouble())
                    val outer = r * 0.80f
                    val inner = r * (if (major) 0.685f else 0.745f)
                    val x1 = c.x + (outer * sin(rad)).toFloat(); val y1 = c.y - (outer * cos(rad)).toFloat()
                    val x2 = c.x + (inner * sin(rad)).toFloat(); val y2 = c.y - (inner * cos(rad)).toFloat()
                    drawLine(
                        if (major) Gold else OnEmMuted.copy(alpha = 0.55f),
                        Offset(x1, y1), Offset(x2, y2),
                        strokeWidth = if (major) 4.5f else 2f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
            // দিগদর্শনের চারটে বৃত্ত — রিংয়ের ওপর
            listOf("উ" to 0f, "পূ" to 90f, "দ" to 180f, "প" to 270f).forEach { (label, deg) ->
                val rad = Math.toRadians(deg.toDouble())
                val d = (sin(rad) * 0.735f); val dY = (-cos(rad) * 0.735f)
                // বৃত্ত (Canvas) + লেখা (Box offset) একসাথে — রোটেশন বکس-ই ঘোরে
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.offset(x = (d * dialSize.value / 2f).dp, y = (dY * dialSize.value / 2f).dp)) {
                        Canvas(Modifier.size(44.dp)) {
                            drawCircle(
                                Brush.verticalGradient(listOf(Color(0xFF155943), Color(0xFF062A20))),
                                size.minDimension / 2f, Offset(size.width / 2f, size.height / 2f)
                            )
                            drawCircle(
                                Gold, size.minDimension / 2f, Offset(size.width / 2f, size.height / 2f),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.6f)
                            )
                        }
                        Text(
                            label,
                            style = HidayahType.TitleSans.copy(fontSize = 19.sp, fontWeight = FontWeight.ExtraBold),
                            color = Cream,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
        // ---------- কেন্দ্র: আকাশে কাবা (ক্যানভাস) ----------
        KaabaScene(Modifier.size(dialSize * 0.60f))
        // ---------- কিবলার কাঁটা (ক্বিবলা-আপেক্ষিক) ----------
        Box(Modifier.fillMaxSize().rotate(qiblaBearingDeg - azimuthDeg)) {
            Canvas(Modifier.fillMaxSize()) {
                val c = Offset(size.width / 2f, size.height / 2f)
                val r = size.minDimension / 2f
                val tipY = c.y - r * 0.635f
                val baseY = c.y - r * 0.18f
                val halfBase = r * 0.085f
                val arrow = Path().apply {
                    moveTo(c.x, tipY)
                    lineTo(c.x + halfBase, baseY)
                    lineTo(c.x + halfBase * 0.42f, baseY)
                    lineTo(c.x + halfBase * 0.42f, baseY + r * 0.10f)
                    lineTo(c.x - halfBase * 0.42f, baseY + r * 0.10f)
                    lineTo(c.x - halfBase * 0.42f, baseY)
                    lineTo(c.x - halfBase, baseY)
                    close()
                }
                drawPath(arrow, Brush.verticalGradient(listOf(Color(0xFFFFE98A), Gold, Color(0xFFA8791F))))
                drawPath(arrow, Color(0xFF5C4413), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.2f))
            }
        }
        // ---------- স্থির টপ মার্কার (সূচক) ----------
        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, 6f)
            val path = Path().apply {
                moveTo(c.x - 10f, 2f)
                lineTo(c.x + 10f, 2f)
                lineTo(c.x, 20f)
                close()
            }
            drawPath(path, Color(0xFF062A20))
            drawPath(path, Gold, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.6f))
        }
    }
}

@Composable
private fun KaabaScene(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = kotlin.math.min(w, h) / 2f
        // ক্লিপ — বৃত্তের ভেতরেই সব
        val clip = Path().apply { addOval(Rect(0f, 0f, w, h)) }
        drawRect(Brush.verticalGradient(listOf(Color(0xFFBFE0EC), Color(0xFFDCE9EC))), topLeft = Offset.Zero, size = Size(w, h))
        clipPath(clip) {
            // চন্দ্র-মাটা আলো
            drawCircle(Color(0xFFFFFFFF).copy(alpha = 0.5f), r * 0.32f, Offset(w * 0.72f, h * 0.24f))
            // মেঘ
            drawOval(Color(0xFFFFFFFF).copy(alpha = 0.75f), topLeft = Offset(w * 0.08f, h * 0.30f), size = Size(w * 0.34f, h * 0.10f))
            drawOval(Color(0xFFFFFFFF).copy(alpha = 0.6f), topLeft = Offset(w * 0.52f, h * 0.16f), size = Size(w * 0.36f, h * 0.09f))
            // দিগন্ত রেখা — মসজিদ সিলুয়েট দুই পাশে
            val horizon = h * 0.62f
            val sil = Color(0xFF7E938B).copy(alpha = 0.9f)
            listOf(0.16f to 0.16f, 0.32f to 0.11f, 0.68f to 0.11f, 0.84f to 0.16f).forEach { (fx, sh) ->
                val sx = w * fx
                drawRect(sil, topLeft = Offset(sx - w * sh * 0.8f, horizon - h * sh), size = Size(w * sh * 1.6f, h * sh))
                drawArc(sil, 180f, 180f, true, topLeft = Offset(sx - w * sh * 0.8f, horizon - h * sh * 1.55f), size = Size(w * sh * 1.6f, h * sh * 1.15f))
            }
            // মেঝে — মার্বেল
            drawRect(Brush.verticalGradient(listOf(Color(0xFFE9E5D6), Color(0xFFCDC7B4))), topLeft = Offset(0f, horizon), size = Size(w, h - horizon))
            // কাবার ছায়া
            drawOval(Color(0xFF000000).copy(alpha = 0.22f), topLeft = Offset(cx - r * 0.52f, horizon + h * 0.055f), size = Size(r * 1.04f, h * 0.085f))
            // ---------- কাবা (আইসমোলজিক্যাল কিউব) ----------
            val bw = r * 0.78f
            val topY = h * 0.30f
            val bodyH = h * 0.42f
            val left = cx - bw / 2f
            val right = cx + bw / 2f
            // বাম মুখ
            drawPath(
                Path().apply {
                    moveTo(left, topY + bodyH * 0.16f)
                    lineTo(cx - bw / 2f, topY + bodyH)
                    lineTo(cx, topY + bodyH * 1.14f)
                    lineTo(cx, topY + bodyH * 0.28f)
                    close()
                },
                Color(0xFF131110)
            )
            // ডান মুখ
            drawPath(
                Path().apply {
                    moveTo(right, topY + bodyH * 0.16f)
                    lineTo(cx + bw / 2f, topY + bodyH)
                    lineTo(cx, topY + bodyH * 1.14f)
                    lineTo(cx, topY + bodyH * 0.28f)
                    close()
                },
                Color(0xFF1E1B19)
            )
            // টপ মুখ (ছাত)
            drawPath(
                Path().apply {
                    moveTo(cx, topY)
                    lineTo(right, topY + bodyH * 0.16f)
                    lineTo(cx, topY + bodyH * 0.30f)
                    lineTo(left, topY + bodyH * 0.16f)
                    close()
                },
                Color(0xFF262320)
            )
            // কিসা — গোল্ড ব্যান্ড (দুই মুখ)
            val bandH = bodyH * 0.16f
            drawPath(
                Path().apply {
                    moveTo(left + 2f, topY + bodyH * 0.34f)
                    lineTo(cx, topY + bodyH * 0.115f + bodyH * 0.20f)
                    lineTo(cx, topY + bodyH * 0.115f + bodyH * 0.20f + bandH)
                    lineTo(left + 2f, topY + bodyH * 0.34f + bandH)
                    close()
                },
                Gold
            )
            drawPath(
                Path().apply {
                    moveTo(cx, topY + bodyH * 0.115f + bodyH * 0.20f)
                    lineTo(right - 2f, topY + bodyH * 0.34f)
                    lineTo(right - 2f, topY + bodyH * 0.34f + bandH)
                    lineTo(cx, topY + bodyH * 0.115f + bodyH * 0.20f + bandH)
                    close()
                },
                Gold
            )
            // কিসা নিচের সূক্ষ্ম গোল্ড লহর
            drawPath(
                Path().apply {
                    moveTo(left + 2f, topY + bodyH * 0.34f + bandH + 3f)
                    lineTo(cx, topY + bodyH * 0.115f + bodyH * 0.20f + bandH + 3f)
                    lineTo(right - 2f, topY + bodyH * 0.34f + bandH + 3f)
                },
                Color(0xFF8A6D1E),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.6f)
            )
            // দরজা — ডান মুখের মাঝখানে (গোল্ড আর্ক)
            val doorW = bw * 0.16f
            val doorH = bodyH * 0.30f
            val doorTop = topY + bodyH * 0.66f
            val doorX = cx + bw * 0.24f - doorW / 2f
            drawPath(
                Path().apply {
                    moveTo(doorX, doorTop + doorH)
                    lineTo(doorX, doorTop + doorW * 0.5f)
                    arcTo(Rect(doorX, doorTop, doorX + doorW, doorTop + doorW), 180f, 180f, false)
                    lineTo(doorX + doorW, doorTop + doorH)
                    close()
                },
                Gold
            )
            drawPath(
                Path().apply {
                    moveTo(doorX + 3f, doorTop + doorH - 3f)
                    lineTo(doorX + 3f, doorTop + doorW * 0.62f)
                    arcTo(Rect(doorX + 3f, doorTop + 3f, doorX + doorW - 3f, doorTop + doorW - 3f), 180f, 180f, false)
                    lineTo(doorX + doorW - 3f, doorTop + doorH - 3f)
                    close()
                },
                Color(0xFF0E4D3C)
            )
        }
        // বাইরের গোল্ড বর্ডার (বৃত্ত)
        drawCircle(Gold, r - 1f, Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.6f))
    }
}
