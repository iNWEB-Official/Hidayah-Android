package com.hidayah.android.data.quran

import android.content.Context
import org.json.JSONArray

data class Surah(
    val number: Int,
    val nameBn: String,
    val nameArabic: String,
    val nameTransliteration: String,
    val ayahCount: Int,
    val revelationPlace: String
)

data class FatihaAyah(val number: Int, val arabic: String, val bengali: String)

data class AyahOfDay(val arabic: String, val bengali: String, val reference: String)

class QuranRepository(context: Context) {

    private val appContext = context.applicationContext

    val ayahsOfDay = listOf(
        AyahOfDay(
            "فَإِنَّ مَعَ ٱلْعُسْرِ يُسْرًا",
            "নিশ্চয়ই কষ্টের সাথেই রয়েছে স্বস্তি।",
            "সূরা আশ-শারহ  •  ৯৪ : ৬"
        ),
        AyahOfDay(
            "رَبَّنَآ ءَاتِنَا فِي ٱلدُّنْيَا حَسَنَةً وَفِي ٱلْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ ٱلنَّارِ",
            "হে আমাদের রব, দুনিয়া ও আখিরাতে আমাদের কল্যাণ দান করুন।",
            "সূরা আল-বাকারা  •  ২ : ২০১"
        ),
        AyahOfDay(
            "وَقُل رَّبِّ زِدْنِي عِلْمًا وَٱرْزُقْنِي فَهْمًا",
            "হে রব, আমাকে আরও জ্ঞান দান করুন এবং হেকমত দান করুন।",
            "সূরা ত্বহা  •  ২০ : ১১৪"
        )
    )

    fun loadSurahs(): List<Surah> {
        val json = appContext.assets.open("quran_surahs.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Surah(
                number = o.getInt("n"),
                nameBn = o.getString("bn"),
                nameArabic = o.getString("ar"),
                nameTransliteration = o.getString("tr"),
                ayahCount = o.getInt("ayahs"),
                revelationPlace = o.getString("place")
            )
        }
    }

    fun loadFatihaAyahs(): List<FatihaAyah> {
        val json = appContext.assets.open("fatiha.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            FatihaAyah(o.getInt("n"), o.getString("ar"), o.getString("bn"))
        }
    }
}
