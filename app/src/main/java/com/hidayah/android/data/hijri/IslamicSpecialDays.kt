package com.hidayah.android.data.hijri

import java.time.LocalDate

/**
 * ইসলামিক বিশেষ দিনসমূহ (হিজরি মাস-দিন) — ক্যালেন্ডার ও হোম কার্ডের "নিকটতম বিশেষ দিন" হিসাবের এক উৎস।
 * "রমজানের প্রথম দিন" এখানে আছে কিন্তু হোম-কার্ডের নিকটতম-বিশেষ-দিন হিসাবে **বাদ** দেওরা হয়,
 * কারণ ওটা হোম কার্ডে আলাদা লাইনে ("রমজান শুরুতে N দিন") দেখানো হয় — একই তথ্য দুইবার দেখানো হয় না।
 */
object IslamicSpecialDays {

    data class SpecialDay(val month: Int, val day: Int, val label: String)

    val ALL = listOf(
        SpecialDay(1, 1, "ইসলামি নববর্ষ"),
        SpecialDay(1, 10, "আশুরা"),
        SpecialDay(3, 12, "মিলাদুন্নবী"),
        SpecialDay(9, 1, "রমজানের প্রথম দিন"),
        SpecialDay(9, 27, "লাইলাতুল কদর"),
        SpecialDay(10, 1, "ঈদুল ফিতর"),
        SpecialDay(12, 9, "আরাফার দিন"),
        SpecialDay(12, 10, "ঈদুল আযহা")
    )

    /** রমজান-স্ট্যাটাস: চলতি তারিখ রমজানের ভেতরে হলে `inRamadan=true` ও `days = ঈদ আসতে যত দিন`; নাহলে `days = ১ রমজান আসতে যত দিন`। */
    fun ramadanStatus(today: LocalDate): RamadanStatus {
        val (hy, hm, _) = HijriDates.gregorianToHijri(today)
        return if (hm == 9) {
            val eid = HijriDates.hijriToGregorian(hy, 10, 1)
            RamadanStatus(inRamadan = true, days = eid.toEpochDay() - today.toEpochDay())
        } else {
            for (y in listOf(hy, hy + 1)) {
                val start = HijriDates.hijriToGregorian(y, 9, 1)
                if (!start.isBefore(today)) {
                    return RamadanStatus(inRamadan = false, days = start.toEpochDay() - today.toEpochDay())
                }
            }
            RamadanStatus(inRamadan = false, days = -1)
        }
    }

    /** সবচেয়ে নিকটতম বিশেষ দিন ("রমজানের প্রথম দিন" বাদে) ও কত দিন বাকি। আজই হলে days == 0। */
    fun nearestSpecialDay(today: LocalDate): Pair<SpecialDay, Long> {
        val (hy, _, _) = HijriDates.gregorianToHijri(today)
        var best: Pair<SpecialDay, Long>? = null
        for (y in listOf(hy, hy + 1)) {
            for (sd in ALL) {
                if (sd.month == 9 && sd.day == 1) continue
                val greg = runCatching { HijriDates.hijriToGregorian(y, sd.month, sd.day) }.getOrNull() ?: continue
                val delta = greg.toEpochDay() - today.toEpochDay()
                if (delta >= 0 && (best == null || delta < best!!.second)) best = sd to delta
            }
        }
        return best ?: (ALL[0] to -1L)
    }

    data class RamadanStatus(val inRamadan: Boolean, val days: Long)
}
