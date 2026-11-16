package com.hidayah.android.data.dua

import android.content.Context
import org.json.JSONArray

data class Dua(
    val id: Int,
    val title: String,
    val source: String,
    val category: String,
    val arabic: String,
    val bengali: String
)

class DuaRepository(context: Context) {

    private val appContext = context.applicationContext

    fun loadDuas(): List<Dua> {
        val json = appContext.assets.open("duas.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(json)
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Dua(
                id = o.getInt("id"),
                title = o.getString("title"),
                source = o.getString("src"),
                category = o.getString("cat"),
                arabic = o.getString("ar"),
                bengali = o.getString("bn")
            )
        }
    }
}
