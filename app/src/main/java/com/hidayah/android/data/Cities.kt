package com.hidayah.android.data

/** Bangladeshi cities with coordinates — used for prayer times and Qibla (offline, no location permission required). */
data class City(val key: String, val nameBn: String, val lat: Double, val lng: Double, val tzOffset: Double)

object Cities {
    val ALL = listOf(
        City("dhaka", "ঢাকা", 23.8103, 90.4125, 6.0),
        City("chittagong", "চট্টগ্রাম", 22.3569, 91.7832, 6.0),
        City("sylhet", "সিলেট", 24.8949, 91.8687, 6.0),
        City("khulna", "খুলনা", 22.8456, 89.5403, 6.0),
        City("rajshahi", "রাজশাহী", 24.3745, 88.6042, 6.0),
        City("barishal", "বরিশাল", 22.7010, 90.3535, 6.0),
        City("rangpur", "রংপুর", 25.7439, 89.2752, 6.0),
        City("mymensingh", "ময়মনসিংহ", 24.7471, 90.4203, 6.0)
    )

    fun byKey(key: String): City = ALL.firstOrNull { it.key == key } ?: ALL.first()
}
