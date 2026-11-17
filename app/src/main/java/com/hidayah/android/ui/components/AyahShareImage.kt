package com.hidayah.android.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.hidayah.android.R
import com.hidayah.android.data.quran.AyahOfDay
import java.io.File
import java.io.FileOutputStream

/**
 * Renders the "আজকের আয়াত" share card as a crisp 1080px PNG for social media,
 * independent of the device screen — same artwork as AyahCardArtwork / ayah-card-background.svg.
 *
 * ① উপরের "বিসমিল্লাহির রাহমানির রাহিম" (বাংলা, ছোট — সব লেখার সাথে কেন্দ্র নয়, ফিক্সড)
 * ② নিচের "© iNWEB Official" — এই দুইটি কেন্দ্র-গণনার বাইরে (ফিক্সড উপরে/নিচে)।
 */
object AyahShareImage {

    fun create(context: Context, ayah: AyahOfDay): File {
        val w = 1080
        val h = 700
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        // background gradient
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, 0f, h.toFloat(),
                intArrayOf(0xFF123F31.toInt(), 0xFF0E3325.toInt(), 0xFF08251B.toInt()),
                floatArrayOf(0f, 0.52f, 1f), Shader.TileMode.CLAMP)
        }
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bg)

        // faint star lattice
        val star = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x14D4AF37
            style = Paint.Style.STROKE
            strokeWidth = 2.4f
            strokeJoin = Paint.Join.ROUND
        }
        val cell = 148f
        var yy = cell / 2
        var row = 0
        while (yy < h + cell) {
            val xOff = if (row % 2 == 0) 0f else cell / 2
            var xx = xOff
            while (xx < w + cell) {
                starPath(xx, yy, 21f).let { c.drawPath(it, star) }
                xx += cell
            }
            yy += cell * 0.88f
            row++
        }
        // central medallion stars
        val med = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x0DD4AF37
            style = Paint.Style.STROKE
            strokeWidth = 3.4f
            strokeJoin = Paint.Join.ROUND
        }
        c.drawPath(starPath(w / 2f, h / 2f, 265f), med)
        c.drawPath(starPath(w / 2f, h / 2f, 185f), med)

        // radial gold glow, top center
        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(w / 2f, -h * 0.32f, w * 0.72f,
                intArrayOf(0x38D4AF37, 0x00D4AF37), null, Shader.TileMode.CLAMP)
        }
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), glow)

        // double frame
        val frame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFD4AF37.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 5.2f
        }
        val frameInner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xA6D4AF37.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 2.4f
        }
        c.drawRoundRect(RectF(13f, 13f, w - 13f, h - 13f), 30f, 30f, frame)
        c.drawRoundRect(RectF(24f, 24f, w - 24f, h - 24f), 23f, 23f, frameInner)
        // corner ornaments
        val corner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xE6D4AF37.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 1.7f
            strokeJoin = Paint.Join.ROUND
        }
        listOf(45f to 45f, (w - 45f) to 45f, 45f to (h - 45f), (w - 45f) to (h - 45f))
            .forEach { (x, y) -> c.drawPath(starPath(x, y, 9.5f), corner) }

        // fonts
        val serif = ResourcesCompat.getFont(context, R.font.noto_sans_bengali_semi_bold) ?: Typeface.DEFAULT_BOLD
        val bnSerif = ResourcesCompat.getFont(context, R.font.noto_serif_bengali_semibold) ?: Typeface.DEFAULT_BOLD
        val arabicType = ResourcesCompat.getFont(context, R.font.amiri_regular) ?: Typeface.DEFAULT

        val cx = w / 2f

        // ===== ফিক্সড: উপরের বিসমিল্লাহ (বাংলা, ছোট) =====
        val bismillah = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = bnSerif; textSize = 26f; color = 0xFFD4AF37.toInt(); textAlign = Paint.Align.CENTER
        }
        c.drawText("বিসমিল্লাহির রাহমানির রাহিম", cx, 68f, bismillah)

        // ---------- প্রস্তুতি: আরবি ও বাংলা লাইন মোড়ানো (উচ্চতা পরিমাপের জন্য) ----------
        val maxTextW = w - 150f
        var sizeAu = 62f
        val arabicPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = arabicType
            color = 0xFFFBF8ED.toInt()
            textAlign = Paint.Align.CENTER
        }
        var lines: List<String>
        while (true) {
            arabicPaint.textSize = sizeAu
            lines = wrapText(ayah.arabic, arabicPaint, maxTextW)
            if (lines.size <= 2 || sizeAu <= 34f) break
            sizeAu -= 4f
        }
        var sizeBn = 32f
        val bnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = bnSerif
            color = 0xFFDCE9DD.toInt()
            textAlign = Paint.Align.CENTER
        }
        var bnLines: List<String>
        while (true) {
            bnPaint.textSize = sizeBn
            bnLines = wrapText(ayah.bengali, bnPaint, maxTextW)
            if (bnLines.size <= 2 || sizeBn <= 18f) break
            sizeBn -= 2f
        }

        val spark = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFD4AF37.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 1.6f
            strokeJoin = Paint.Join.ROUND
        }

        // ---------- কেন্দ্র-ব্লক (© বাদে সব লেখা; প্রতিটি উপাদানের উচ্চতা আলাদা) ----------
        val elements = mutableListOf<Pair<Float, (Float) -> Unit>>()

        elements += 30f to { top ->
            val kicker = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = serif
                textSize = 40f
                color = 0xFFD4AF37.toInt()
                letterSpacing = 0.16f
                textAlign = Paint.Align.CENTER
            }
            val kt = "আজকের আয়াত"
            val kw = kicker.measureText(kt)
            val baseY = top + 26f
            c.drawText(kt, cx + 18f, baseY, kicker)
            c.drawPath(starPath(cx - kw / 2f - 6f, baseY - 13f, 11f), spark)
        }
        elements += 26f to { } // gap

        val arabicH = lines.size * (sizeAu * 1.62f)
        elements += arabicH to { top ->
            var yy2 = top + sizeAu * 1.05f
            lines.forEach { line ->
                c.drawText(line, cx, yy2, arabicPaint)
                yy2 += sizeAu * 1.62f
            }
        }
        elements += 6f to { } // gap

        val bnH = bnLines.size * (sizeBn * 1.5f)
        elements += bnH to { top ->
            var yy2 = top + sizeBn * 1.1f
            bnLines.forEach { line ->
                c.drawText(line, cx, yy2, bnPaint)
                yy2 += sizeBn * 1.5f
            }
        }
        elements += 14f to { } // gap

        elements += 30f to { top ->
            val refPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = bnSerif
                textSize = 25f
                color = 0xFFE9D38A.toInt()
                textAlign = Paint.Align.CENTER
            }
            c.drawText(ayah.reference, cx, top + 22f, refPaint)
        }
        elements += 34f to { } // gap

        elements += 88f to { top ->
            val brandBn = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = bnSerif
                textSize = 52f
                color = 0xFFD4AF37.toInt()
                textAlign = Paint.Align.CENTER
            }
            c.drawText("হিদায়াহ", cx, top + 46f, brandBn)
            val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = serif
                textSize = 18f
                color = 0xB3D4AF37.toInt()
                letterSpacing = 0.40f
                textAlign = Paint.Align.CENTER
            }
            c.drawText("H I D A Y A H", cx, top + 72f, brandPaint)
        }

        val blockH = elements.sumOf { (it.first * 10f).toInt() } / 10f
        // কেন্দ্র-গণনা: বিসমিল্লাহের নিচ (78) থেকে ©-এর উপর (646) পর্যন্ত ব্যান্ডের মাঝে
        val bandTop = 78f
        val bandBottom = 646f
        var yPos = bandTop + ((bandBottom - bandTop) - blockH) / 2f
        elements.forEach { (hgt, draw) -> draw(yPos); yPos += hgt }

        val copyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = serif
            textSize = 20f
            color = 0x8CD4AF37.toInt()
            letterSpacing = 0.30f
            textAlign = Paint.Align.CENTER
        }
        // ===== ফিক্সড: নিচের © =====
        c.drawText("© iNWEB Official", cx, h - 44f, copyPaint)

        // write PNG
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(dir, "ayah-today.png")
        FileOutputStream(file).use { out -> bmp.compress(Bitmap.CompressFormat.PNG, 100, out) }
        bmp.recycle()
        return file
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        for (word in words) {
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(candidate) <= maxWidth) {
                current = StringBuilder(candidate)
            } else {
                if (current.isNotEmpty()) lines.add(current.toString())
                current = StringBuilder(word)
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())
        return lines
    }

    private fun starPath(cx: Float, cy: Float, r: Float): Path {
        val inner = r * 0.44f
        val path = Path()
        for (i in 0 until 16) {
            val angle = (Math.PI / 8.0) * i - Math.PI / 2.0
            val rr = if (i % 2 == 0) r else inner
            val px = cx + (rr * Math.cos(angle)).toFloat()
            val py = cy + (rr * Math.sin(angle)).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        return path
    }
}

/* ═══════════════ ARCHIVED — পুরোনো "H I D A Y A H" এক-লাইন ব্র্যান্ড লকআপ ═══════════════
 * (বিশেষ দ্রষ্টব্য অনুযায়ী সম্পূর্ণ মোছা হয়নি — প্রয়োজন হলে এখানেই ফিরিয়ে আনুন)
 *
 *        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
 *            typeface = serif
 *            textSize = 21f
 *            color = 0xB3D4AF37.toInt()
 *            letterSpacing = 0.42f
 *            textAlign = Paint.Align.CENTER
 *        }
 *        c.drawText("H I D A Y A H", cx, h - 92f, brandPaint)
 *        c.drawPath(starPath(cx, h - 126f, 6f), spark)
 *
 * ═══════════════════════════════════════════════════════════════════════════════════════ */
