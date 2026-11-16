package com.hidayah.android.data.quran

import kotlin.math.*

object QiblaMath {
    const val KAABA_LAT = 21.4225
    const val KAABA_LNG = 39.8262

    /** Initial bearing from the given location to the Kaaba (degrees, 0 = north). */
    fun bearingTo(lat: Double, lng: Double): Double {
        val l1 = Math.toRadians(lat)
        val l2 = Math.toRadians(KAABA_LAT)
        val dLng = Math.toRadians(KAABA_LNG - lng)
        val y = sin(dLng) * cos(l2)
        val x = cos(l1) * sin(l2) - sin(l1) * cos(l2) * cos(dLng)
        return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
    }

    /** Great-circle distance to the Kaaba in kilometres. */
    fun distanceKm(lat: Double, lng: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(KAABA_LAT - lat)
        val dLng = Math.toRadians(KAABA_LNG - lng)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat)) * cos(Math.toRadians(KAABA_LAT)) * sin(dLng / 2).pow(2)
        return 2 * r * asin(sqrt(a))
    }
}
