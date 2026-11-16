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
 * হোম "আজকের পঞ্জিকা" কার্ডের শেয়ারেবল PNG (1080×760)।
 * নিয়ম: সব লেখা **কেন্দ্র-সারিবদ্ধ** (উপরে/নিচে/ডানে/বামে — সব দিক থেকে মাপা); তবে
 *  ① উপরের "বিসমিল্লাহির রাহমানির রাহিম" ও  ② নিচের "© iNWEB Official" — এই দুইটি কেন্দ্র-গণনার বাইরে (ফিক্সড উপরে/নিচে)।
 * কার্ডের সকল সংখ্যার সংখ্যা-অংশ বড় ও সোনালি (হাইলাইট); রমজানের ১ তারিখে "রমাদান মোবারক" বড় সবুজ।
 */
object CalendarShareImage {

    fun create(context: Context, date: LocalDate): File {
        val w = 1080
        val h = 760
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, 0f, h.toFloat(),
                intArrayOf(0xFF123F31.toInt(), 0xFF0E3325.toInt(), 0xFF08251B.toInt()),
                floatArrayOf(0f, 0.52f, 1f), Shader.TileMode.CLAMP)
        }
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bg)

        val starStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x14D4AF37; style = Paint.Style.STROKE; strokeWidth = 2.4f; strokeJoin = Paint.Join.ROUND
        }
        val cell = 148f
        var yy = cell / 2
        var row = 0
        while (yy < h + cell) {
            val xOff = if (row % 2 == 0) 0f else cell / 2
            var xx = xOff
            while (xx < w + cell) { c.drawPath(starPath(xx, yy, 21f), starStroke); xx += cell }
            yy += cell * 0.88f
            row++
        }

        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(w / 2f, -h * 0.30f, w * 0.72f,
                intArrayOf(0x38D4AF37, 0x00D4AF37), null, Shader.TileMode.CLAMP)
        }
        c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), glow)

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

        val bnSerif = ResourcesCompat.getFont(context, R.font.noto_serif_bengali_semibold) ?: Typeface.DEFAULT_BOLD
        val bnSerifBold = ResourcesCompat.getFont(context, R.font.noto_serif_bengali_bold) ?: Typeface.DEFAULT_BOLD
        val bnSansSemi = ResourcesCompat.getFont(context, R.font.noto_sans_bengali_semi_bold) ?: Typeface.DEFAULT_BOLD

        val cx = w / 2f
        val gold = 0xFFD4AF37.toInt()
        val goldSoft = 0xFFE9D38A.toInt()
        val cream = 0xFFFBF8ED.toInt()
        val prose = 0xFFDCE9DD.toInt()

        // ===== ফিক্সড: উপরের বিসমিল্লাহ (বাংলা, ছোট) =====
        val bismillah = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = bnSerif; textSize = 26f; color = gold; textAlign = Paint.Align.CENTER
        }
        c.drawText("বিসমিল্লাহির রাহমানির রাহিম", cx, 68f, bismillah)

        // ===== কেন্দ্র-ব্লক: kicker → brand (বিসমিল্লাহ ও © বাদে সব) =====
        val (hy, hm, hd) = HijriDates.gregorianToHijri(date)
        val ramadanStatus = IslamicSpecialDays.ramadanStatus(date)
        val isFirstRamadan = hm == 9 && hd == 1

        val elements = mutableListOf<Pair<Float, (Float) -> Unit>>() // height, draw(topY)

        // 1) kicker: ✦ আজকের পঞ্জিকা
        run {
            val kf = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = bnSansSemi; textSize = 28f; color = gold; letterSpacing = 0.18f; textAlign = Paint.Align.LEFT
            }
            val kt = "আজকের পঞ্জিকা"
            val kw = kf.measureText(kt)
            elements += 30f to { top ->
                val baseY = top + 24f
                val totalW = kw + 54f
                val startX = cx - totalW / 2f
                val spark = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = gold; style = Paint.Style.STROKE; strokeWidth = 1.5f; strokeJoin = Paint.Join.ROUND
                }
                c.drawPath(starPath(startX + 12f, baseY - 9f, 11f), spark)
                c.drawText(kt, startX + 36f, baseY, kf)
            }
        }
        elements += 30f to { } // gap 30 (spacer element)
        // 2) Gregorian line — digits বড় সোনালি
        elements += 62f to { top -> drawSegLine(c, HijriDates.gregLabelBn(date), bnSerif, 46f, cream, bnSerifBold, 57f, goldSoft, cx, top + 46f) }
        elements += 10f to { }
        // 3) Hijri line
        val hijriText = "${BnDigits.of(hd)} ${HijriDates.monthNamesBn[hm - 1]} ${BnDigits.of(hy)} হিজরি"
        elements += 46f to { top -> drawSegLine(c, hijriText, bnSerif, 32f, 0xFFE9D38A.toInt(), bnSerifBold, 41f, gold, cx, top + 32f) }
        elements += 8f to { }
        // 4) Bengali line
        elements += 46f to { top -> drawSegLine(c, BnCalendar.labelBn(date), bnSerif, 32f, 0xD9D4AF37.toInt(), bnSerifBold, 41f, gold, cx, top + 32f) }
        elements += 22f to { }
        // 5) divider
        elements += 3f to { top ->
            val divider = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x66D4AF37 }
            c.drawRoundRect(RectF(cx - 190f, top, cx + 190f, top + 3f), 2f, 2f, divider)
        }
        elements += 24f to { }
        // 6) Ramadan line / রমাদান মোবারক
        if (isFirstRamadan) {
            elements += 72f to { top ->
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = bnSerifBold; textSize = 60f; color = 0xFF4ADE80.toInt(); textAlign = Paint.Align.CENTER
                }
                c.drawText("রমাদান মোবারক", cx, top + 52f, paint)
            }
        } else {
            val prefix = if (ramadanStatus.inRamadan) "ঈদ আসতে আর মাত্র " else "রমজান শুরু হতে আর মাত্র "
            val ramadanText = prefix + BnDigits.of(ramadanStatus.days.toInt()) + " দিন বাকি আছে।"
            elements += 48f to { top -> drawSegLine(c, ramadanText, bnSerif, 24f, prose, bnSerifBold, 42f, goldSoft, cx, top + 30f) }
        }
        elements += 14f to { }
        // 7) নিকটতম বিশেষ দিন
        val (special, specialDays) = IslamicSpecialDays.nearestSpecialDay(date)
        val specialText = if (specialDays == 0L) {
            "নিকটতম বিশেষ দিন: ${special.label} — আজ!"
        } else {
            "নিকটতম বিশেষ দিন: ${special.label} — আর মাত্র ${BnDigits.of(specialDays.toInt())} দিন পরে।"
        }
        elements += 44f to { top -> drawSegLine(c, specialText, bnSerif, 21f, prose, bnSerifBold, 37f, goldSoft, cx, top + 27f) }
        elements += 30f to { }
        // 8) ব্র্যান্ড লকআপ — দুই লাইন (বাংলা + English)
        elements += 92f to { top ->
            val brandBn = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = bnSerifBold; textSize = 52f; color = gold; textAlign = Paint.Align.CENTER
            }
            c.drawText("হিদায়াহ", cx, top + 46f, brandBn)
            val brandEn = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = bnSansSemi; textSize = 18f; color = 0xB3D4AF37.toInt(); letterSpacing = 0.40f; textAlign = Paint.Align.CENTER
            }
            c.drawText("H I D A Y A H", cx, top + 76f, brandEn)
        }

        // কেন্দ্র-গণনা: বিসমিল্লাহের নিচ (96) থেকে ©-এর উপর (706) পর্যন্ত ব্যান্ডের মাঝে
        val bandTop = 78f
        val bandBottom = 706f
        val blockH = elements.sumOf { (it.first * 10f).toInt() } / 10f
        var y = bandTop + ((bandBottom - bandTop) - blockH) / 2f
        elements.forEach { (hgt, draw) -> draw(y); y += hgt }

        // ===== ফিক্সড: নিচের © =====
        val copyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = bnSansSemi; textSize = 20f; color = 0x8CD4AF37.toInt(); letterSpacing = 0.30f; textAlign = Paint.Align.CENTER
        }
        c.drawText("© iNWEB Official", cx, h - 44f, copyPaint)

        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(dir, "hidayah-calendar-card.png")
        FileOutputStream(file).use { out -> bmp.compress(Bitmap.CompressFormat.PNG, 100, out) }
        bmp.recycle()
        return file
    }

    /** সব সংখ্যা-রান বড় ও হাইলাইট সোনালি; বাকি টেক্সট সাধারণত prose রঙে — পুরো লাইন কেন্দ্রে। */
    private fun drawSegLine(c: Canvas, text: String, proseType: Typeface, proseSize: Float, proseColor: Int,
                            digitType: Typeface, digitSize: Float, digitColor: Int, cx: Float, baselineY: Float) {
        val digitRe = Regex("[০-৯]+|[0-9]+")
        data class Run(val text: String, val paint: Paint)
        val runs = mutableListOf<Run>()
        var last = 0
        for (m in digitRe.findAll(text)) {
            if (m.range.first > last) {
                runs += Run(text.substring(last, m.range.first), Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = proseType; textSize = proseSize; color = proseColor; textAlign = Paint.Align.LEFT
                })
            }
            runs += Run(m.value, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = digitType; textSize = digitSize; color = digitColor; textAlign = Paint.Align.LEFT
            })
            last = m.range.last + 1
        }
        if (last < text.length) {
            runs += Run(text.substring(last), Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = proseType; textSize = proseSize; color = proseColor; textAlign = Paint.Align.LEFT
            })
        }
        val totalW = runs.sumOf { it.paint.measureText(it.text).toDouble() }.toFloat()
        var x = cx - totalW / 2f
        runs.forEach { r -> c.drawText(r.text, x, baselineY, r.paint); x += r.paint.measureText(r.text) }
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

/* ═══════════════ ARCHIVED — পুরোনো "H I D A Y A H" এক-লাইন ব্র্যান্ড লকআপ ═══════════════
 * (বিশেষ দ্রষ্টব্য অনুযায়ী সম্পূর্ণ মোছা হয়নি — প্রয়োজন হলে এখানেই ফিরিয়ে আনুন)
 *
 *        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
 *            typeface = bnSansSemi; textSize = 21f
 *            color = 0xB3D4AF37.toInt(); letterSpacing = 0.42f; textAlign = Align.CENTER
 *        }
 *        c.drawText("H I D A Y A H", cx, h - 104f, brandPaint)
 *        c.drawPath(starPath(cx, h - 136f, 6f), spark)
 *
 * ═══════════════════════════════════════════════════════════════════════════════════════ */
