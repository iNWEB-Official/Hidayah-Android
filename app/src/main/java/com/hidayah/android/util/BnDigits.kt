package com.hidayah.android.util

/** Converts Latin digits to Bengali numerals (৫, ১...). */
object BnDigits {
    private val MAP = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun of(text: String): String =
        buildString(text.length) {
            for (c in text) append(if (c in '0'..'9') MAP[c - '0'] else c)
        }

    fun of(number: Int): String = of(number.toString())

    fun minutesToBnTime(totalMinutes: Int): String {
        val h24 = totalMinutes / 60
        val m = totalMinutes % 60
        return of(String.format("%d:%02d", h24, m))
    }
}
