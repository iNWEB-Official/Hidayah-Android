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
import com.hidayah.android.data.bengali.BnCalendar
import com.hidayah.android.data.hijri.HijriDates
import com.hidayah.android.data.hijri.IslamicSpecialDays
import com.hidayah.android.util.BnDigits
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

/**
 * হোম স্ক্রিনের "আজকের পঞ্জিকা" কার্ডের শেয়ারেবল PNG (1080×760)।
 * HomeScreen-এর কার্ডের সাথে একই কন্টেন্ট ও ডিজাইন-ভাষা; একেবারে নিচে © iNWEB Official।
 */
object CalendarShareImage {

    fun create(context: Context, date: LocalDate): File {
        val w = 1080
        val h = 760
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        // deep-emerald gradient background
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, 0f, h.toFloat(),
                intArrayOf(0xFF123F31.toInt(), 0xFF0E3325.toInt(), 0xFF08251B.toInt()),
                floatArrayOf(0f, 0.52f, 1f), Shader.TileMode.CLAMP)
        }
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bg)

        // faint 8-pointed star lattice
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
                c.drawPath(starPath(xx, yy, 21f), star)
                xx += cell
            }
            yy += cell * 0.88f
            row++
        }

        // radial gold glow, top center
        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(w / 2f, -h * 0.30f, w * 0.72f,
                intArrayOf(0x38D4AF37, 0x00D4AF37), null, Shader.TileMode.CLAMP)
        }
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), glow)

        // double frame
        val frame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFD4AF37.toInt(); style = Paint.Style.STROKE; strokeWidth = 5.2f
        }
        val frameInner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xA6D4AF37.toInt(); style = Paint.Style.STROKE; strokeWidth = 2.4f
        }
        c.drawRoundRect(RectF(13f, 13f, w - 13f, h - 13f), 30f, 30f, frame)
        c.drawRoundRect(RectF(24f, 24f, w - 24f, h - 24f), 23f, 23f, frameInner)
        val corner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xE6D4AF37.toInt(); style = Paint.Style.STROKE; strokeWidth = 1.7f; strokeJoin = Paint.Join.ROUND
        }
        listOf(45f to 45f, (w - 45f) to 45f, 45f to (h - 45f), (w - 45f) to (h - 45f))
            .forEach { (x, y) -> c.drawPath(starPath(x, y, 9.5f), corner) }

        // fonts — রেপোর নিজস্ব TTF
        val bnSerif = ResourcesCompat.getFont(context, R.font.noto_serif_bengali_semibold) ?: Typeface.DEFAULT_BOLD
        val serif = ResourcesCompat.getFont(context, R.font.noto_sans_bengali_semi_bold) ?: Typeface.DEFAULT_BOLD

        val cx = w / 2f
        val gold = 0xFFD4AF37.toInt()

        // বিসমিল্লাহ — কার্ডের উপরে (Amiri)
        val bismillahType = ResourcesCompat.getFont(context, R.font.amiri_regular) ?: Typeface.DEFAULT
        val bismillah = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = bismillahType; textSize = 34f; color = gold; textAlign = Paint.Align.CENTER
        }
        c.drawText("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", cx, 96f, bismillah)

        // kicker: ✦ আজকের পঞ্জিকা
        val kicker = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = serif; textSize = 30f; color = gold; letterSpacing = 0.18f; textAlign = Paint.Align.LEFT
        }
        val kickerText = "আজকের পঞ্জিকা"
        val kickerW = kicker.measureText(kickerText)
        val spark = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = gold; style = Paint.Style.STROKE; strokeWidth = 1.5f; strokeJoin = Paint.Join.ROUND
        }
        val kickerStartX = cx - (kickerW + 54f) / 2f
        c.drawPath(starPath(kickerStartX + 12f, 146f, 11f), spark)
        c.drawText(kickerText, kickerStartX + 36f, 158f, kicker)

        // গ্রেগরিয়ান তারিখ
        val greg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = bnSerif; textSize = 46f; color = 0xFFFBF8ED.toInt(); textAlign = Paint.Align.CENTER
        }
        c.drawText(HijriDates.gregLabelBn(date), cx, 252f, greg)

        // হিজরি তারিখ
        val hijri = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = bnSerif; textSize = 33f; color = 0xFFE9D38A.toInt(); textAlign = Paint.Align.CENTER
        }
        val (hy, hm, hd) = HijriDates.gregorianToHijri(date)
        c.drawText("${BnDigits.of(hd)} ${HijriDates.monthNamesBn[hm - 1]} ${BnDigits.of(hy)} হিজরি", cx, 308f, hijri)

        // বঙ্গাব্দ তারিখ
        val bn = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = bnSerif; textSize = 33f; color = 0xD9D4AF37.toInt(); textAlign = Paint.Align.CENTER
        }
        c.drawText(BnCalendar.labelBn(date), cx, 356f, bn)

        // divider
        val divider = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x66D4AF37 }
        c.drawRoundRect(RectF(cx - 230f, 382f, cx + 230f, 385f), 2f, 2f, divider)

        // রমজান-লাইন (ক্রিসেন্ট গ্লিফসহ, কেন্দ্র-সারিবদ্ধ)
        val ramadanStatus = IslamicSpecialDays.ramadanStatus(date)
        val ramadanText = if (ramadanStatus.inRamadan) {
            "রমজান চলছে — ঈদ আসতে ${BnDigits.of(ramadanStatus.days.toInt())} দিন"
        } else {
            "রমজান শুরুতে আর ${BnDigits.of(ramadanStatus.days.toInt())} দিন"
        }
        val ramadanPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = bnSerif; textSize = 31f; color = gold; textAlign = Paint.Align.LEFT
        }
        drawGlyphTextRow(c, ramadanText, ramadanPaint, 446f, cx, gold, glyph = Glyph.CRESCENT)

        // নিকটতম বিশেষ দিন (ক্রিসেন্ট লাইনে রমজান বাদ — একই তথ্য দুইবার নয়)
        val (special, specialDays) = IslamicSpecialDays.nearestSpecialDay(date)
        val specialText = if (specialDays == 0L) {
            "নিকটতম: ${special.label} — আজ!"
        } else {
            "নিকটতম: ${special.label} — আর ${BnDigits.of(specialDays.toInt())} দিন"
        }
        val specialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = bnSerif; textSize = 29f; color = 0xFFDCE9DD.toInt(); textAlign = Paint.Align.LEFT
        }
        drawGlyphTextRow(c, specialText, specialPaint, 510f, cx, 0xFFD4AF37.toInt(), glyph = Glyph.STAR)

        // brand + ©
        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = serif; textSize = 21f; color = 0xB3D4AF37.toInt(); letterSpacing = 0.42f; textAlign = Paint.Align.CENTER
        }
        c.drawText("H I D A Y A H", cx, h - 104f, brandPaint)
        c.drawPath(starPath(cx, h - 136f, 6f), spark)

        val copyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = serif; textSize = 20f; color = 0x8CD4AF37.toInt(); letterSpacing = 0.30f; textAlign = Paint.Align.CENTER
        }
        c.drawText("© iNWEB Official", cx, h - 34f, copyPaint)

        // write PNG
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(dir, "hidayah-calendar-card.png")
        FileOutputStream(file).use { out -> bmp.compress(Bitmap.CompressFormat.PNG, 100, out) }
        bmp.recycle()
        return file
    }

    private enum class Glyph { CRESCENT, STAR }

    /** গ্লিফ + টেক্সট একটি যৌথ প্রস্থ নিয়ে ক্যানভাসের কেন্দ্রে আঁকেন। */
    private fun drawGlyphTextRow(c: Canvas, text: String, paint: Paint, baselineY: Float, cx: Float, glyphColor: Int, glyph: Glyph) {
        val glyphSize = 26f
        val gap = 16f
        val textW = paint.measureText(text)
        val totalW = glyphSize + gap + textW
        var x = cx - totalW / 2f
        when (glyph) {
            Glyph.CRESCENT -> c.drawPath(crescentPath(x + glyphSize / 2f, baselineY - glyphSize / 2f, glyphSize / 2f), Paint(Paint.ANTI_ALIAS_FLAG).apply { color = glyphColor })
            Glyph.STAR -> c.drawPath(starPath(x + glyphSize / 2f, baselineY - glyphSize / 2f, glyphSize / 2f), Paint(Paint.ANTI_ALIAS_FLAG).apply { color = glyphColor })
        }
        c.drawText(text, x + glyphSize + gap, baselineY, paint)
    }

    private fun crescentPath(cx: Float, cy: Float, r: Float): Path {
        val outer = Path().apply { addOval(RectF(cx - r, cy - r, cx + r, cy + r), Path.Direction.CW) }
        val cut = Path().apply { addOval(RectF(cx - r + r * 0.42f, cy - r, cx + r + r * 0.42f, cy + r), Path.Direction.CW) }
        return Path().apply { op(outer, cut, Path.Op.DIFFERENCE) }
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
