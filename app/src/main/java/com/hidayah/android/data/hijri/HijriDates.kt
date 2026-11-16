package com.hidayah.android.data.hijri

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.ULocale
import com.hidayah.android.util.BnDigits
import java.time.LocalDate
import java.util.Date
import java.util.Locale

/**
 * Hijri calendar helpers built on Android's ICU implementation of the
 * Umm al-Qura calendar (requested with the `calendar=islamic-umalqura` locale
 * keyword) — the same calendar used officially in Saudi Arabia and widely
 * referenced across the Muslim world.
 */
object HijriDates {

    private val UMM_AL_QURA = ULocale("en@calendar=islamic-umalqura")

    val monthNamesBn = listOf(
        "মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি", "জমাদাল উলা", "জমাদাল আখিরাহ",
        "রজব", "শাবান", "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
    )

    val weekdayShortBn = listOf("শনি", "রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র") // Saturday-first grid
    val weekdayBn = listOf("রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার") // Sunday-first, index = java.time % 7

    /** Hijri date (year, month 1-12, day 1-30) for a Gregorian date. */
    fun gregorianToHijri(date: LocalDate): Triple<Int, Int, Int> {
        val gregorian = Calendar.getInstance()
        gregorian.clear()
        gregorian.set(date.year, date.monthValue - 1, date.dayOfMonth, 12, 0, 0)

        val hijri = Calendar.getInstance(UMM_AL_QURA)
        hijri.clear()
        hijri.timeInMillis = gregorian.timeInMillis

        return Triple(
            hijri.get(Calendar.YEAR),
            hijri.get(Calendar.MONTH) + 1,
            hijri.get(Calendar.DAY_OF_MONTH)
        )
    }

    /** Gregorian date for a Hijri date. */
    fun hijriToGregorian(year: Int, month: Int, day: Int): LocalDate {
        val hijri = Calendar.getInstance(UMM_AL_QURA)
        hijri.clear()
        hijri.set(Calendar.YEAR, year)
        hijri.set(Calendar.MONTH, month - 1)
        hijri.set(Calendar.DAY_OF_MONTH, day)
        hijri.set(Calendar.HOUR_OF_DAY, 12)
        hijri.set(Calendar.MINUTE, 0)
        hijri.set(Calendar.SECOND, 0)

        val gregorian = Calendar.getInstance(ULocale("en@calendar=gregorian"))
        gregorian.clear()
        gregorian.timeInMillis = hijri.timeInMillis

        return LocalDate.of(
            gregorian.get(Calendar.YEAR),
            gregorian.get(Calendar.MONTH) + 1,
            gregorian.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun hijriMonthLength(year: Int, month: Int): Int {
        val (ny, nm) = if (month == 12) (year + 1) to 1 else year to (month + 1)
        return (hijriToGregorian(ny, nm, 1).toEpochDay() - hijriToGregorian(year, month, 1).toEpochDay()).toInt()
    }

    /** Saturday-first column index (0 = Saturday, 6 = Friday) of the 1st of the month. */
    fun weekdayColumnOfFirst(year: Int, month: Int): Int {
        val first = hijriToGregorian(year, month, 1)
        // java.time: Mon=1..Sun=7 → convert to Saturday-first index
        return (first.dayOfWeek.value + 1) % 7
    }

    fun todayLabelBn(): String {
        val (hy, hm, hd) = gregorianToHijri(LocalDate.now())
        return "${BnDigits.of(hd)} ${monthNamesBn[hm - 1]} ${BnDigits.of(hy)} হিজরি"
    }

    fun gregLabelBn(date: LocalDate): String {
        val monthsBn = listOf("জানুয়ারী", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
            "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
        val dow = weekdayBn[date.dayOfWeek.value % 7]
        return "$dow, ${BnDigits.of(date.dayOfMonth)} ${monthsBn[date.monthValue - 1]} ${BnDigits.of(date.year)}"
    }

    /** Formats like "২৪ নভেম্বর ২০২৬" for Gregorian dates. */
    fun gregShortBn(date: LocalDate): String {
        val monthsBn = listOf("জানুয়ারী", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
            "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
        return "${BnDigits.of(date.dayOfMonth)} ${monthsBn[date.monthValue - 1]} ${BnDigits.of(date.year)}"
    }
}
