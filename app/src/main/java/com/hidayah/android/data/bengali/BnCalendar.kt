package com.hidayah.android.data.bengali

import com.hidayah.android.util.BnDigits
import java.time.LocalDate

/**
 * সংশোধিত বাংলাদেশ বাংলা পঞ্জিকা (সনাতন নিয়ম: ১ বৈশাখ = ১৪ এপ্রিল;
 * বৈশাখ–ভাদ্র ৩১ দিনের, অগ্রহায়ণ–চৈত্র ৩০ দিনের; অধিবর্ষে (বঙ্গাব্দ % ৪ == ০) চৈত্র ৩১ দিনের)।
 */
object BnCalendar {

    val monthNamesBn = listOf(
        "বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন",
        "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র"
    )

    /** বঙ্গাব্দ (year, month 1-12, day) for a Gregorian date. */
    fun fromGregorian(date: LocalDate): Triple<Int, Int, Int> {
        val nyThisYear = LocalDate.of(date.year, 4, 14)
        val afterNewYear = !date.isBefore(nyThisYear)
        val bnYear = if (afterNewYear) date.year - 593 else date.year - 594
        val start = LocalDate.of(if (afterNewYear) date.year else date.year - 1, 4, 14)
        var remaining = (date.toEpochDay() - start.toEpochDay()).toInt()
        for (i in 0 until 12) {
            val len = monthLength(bnYear, i + 1)
            if (remaining < len) return Triple(bnYear, i + 1, remaining + 1)
            remaining -= len
        }
        return Triple(bnYear, 12, 30)
    }

    fun monthLength(bnYear: Int, month: Int): Int = when {
        month <= 5 -> 31
        month < 12 -> 30
        else -> if (bnYear % 4 == 0) 31 else 30
    }

    /** "২৬ শে কার্তিক ১৪৩৩ বঙ্গাব্দ" — বঙ্গাব্দ লেবেল (বাংলাদেশে প্রচলিত লিখনভঙ্গি)। */
    fun labelBn(date: LocalDate): String {
        val (y, m, d) = fromGregorian(date)
        return "${BnDigits.of(d)} শে ${monthNamesBn[m - 1]} ${BnDigits.of(y)} বঙ্গাব্দ"
    }
}
