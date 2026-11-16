package com.hidayah.android.data.quran

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class QiblaMathTest {

    // Reference point: Dhaka
    private val dhakaLat = 23.8103
    private val dhakaLng = 90.4125

    @Test
    fun bearingFromDhakaIsApproximately273To274Degrees() {
        val bearing = QiblaMath.bearingTo(dhakaLat, dhakaLng)
        assertTrue("bearing was $bearing", bearing in 272.0..275.0)
    }

    @Test
    fun bearingFromMeccaIsApproximatelyZero() {
        // From the Kaaba itself the convention is undefined but stable (0..360)
        val bearing = QiblaMath.bearingTo(QiblaMath.KAABA_LAT, QiblaMath.KAABA_LNG)
        assertTrue(bearing in 0.0..<360.0)
    }

    @Test
    fun distanceDhakaToKaabaIsRoughly5000km() {
        val km = QiblaMath.distanceKm(dhakaLat, dhakaLng)
        assertTrue("distance was $km", km in 4500.0..5400.0)
    }

    @Test
    fun distanceFromKaabaToItselfIsZero() {
        val km = QiblaMath.distanceKm(QiblaMath.KAABA_LAT, QiblaMath.KAABA_LNG)
        assertTrue(abs(km) < 1e-9)
    }

    @Test
    fun bearingAlwaysNormalizedTo0To360() {
        val points = listOf(
            0.0 to 0.0, 90.0 to 0.0, -33.9 to -151.2, 51.5 to -0.12, 6.2 to 106.8
        )
        for ((lat, lng) in points) {
            val b = QiblaMath.bearingTo(lat, lng)
            assertTrue("bearing $b out of range for ($lat,$lng)", b >= 0.0 && b < 360.0)
        }
    }

    @Test
    fun knownKaabaCoordinates() {
        assertEquals(21.4225, QiblaMath.KAABA_LAT, 1e-9)
        assertEquals(39.8262, QiblaMath.KAABA_LNG, 1e-9)
    }
}
