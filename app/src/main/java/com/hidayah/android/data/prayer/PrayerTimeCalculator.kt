package com.hidayah.android.data.prayer

import java.time.LocalDate
import kotlin.math.*

/** One computed prayer time. */
data class PrayerTime(val id: String, val nameBangla: String, val totalMinutes: Int)

/**
 * Self-contained astronomical prayer-time engine (validated against the `astral`
 * reference within ~1 minute across 2 years for Dhaka coordinates).
 *
 * Method: solar position (declination + equation of time) → hour angles.
 * Fajr/Isha use depression angles 18°/17° (MWL-style), Maghrib uses 0.833° refraction,
 * Asr uses shadow-length factor (1 = Shafi'i/standard, 2 = Hanafi).
 */
class PrayerTimeCalculator {

    data class GeoLocation(val lat: Double, val lng: Double, val tzOffsetHours: Double)

    fun timesFor(date: LocalDate, location: GeoLocation, hanafiAsr: Boolean): List<PrayerTime> {
        val jd = julianDay(date.year, date.monthValue, date.dayOfMonth, 12.0)
        val (declDeg, eotMinutes) = sunPosition(jd)
        val noon = 12.0 - location.lng / 15.0 - eotMinutes / 60.0 + location.tzOffsetHours

        val riseSetH = hourAngle(location.lat, declDeg, -0.833)
        val fajrH = hourAngle(location.lat, declDeg, -18.0)
        val ishaH = hourAngle(location.lat, declDeg, -17.0)
        val asrElev = Math.toDegrees(
            atan(1.0 / ((if (hanafiAsr) 2.0 else 1.0) + tan(abs(Math.toRadians(location.lat - declDeg)))))
        )
        val asrH = hourAngle(location.lat, declDeg, asrElev)

        fun mins(hours: Double): Int {
            var m = (hours * 60).roundToInt()
            m %= 1440; if (m < 0) m += 1440
            return m
        }

        return listOf(
            PrayerTime("fajr", "ফজর", mins(noon - fajrH / 15.0)),
            PrayerTime("sunrise", "সূর্যোদয়", mins(noon - riseSetH / 15.0)),
            PrayerTime("dhuhr", "যোহর", mins(noon)),
            PrayerTime("asr", "আসর", mins(noon + asrH / 15.0)),
            PrayerTime("maghrib", "মাগরিব", mins(noon + riseSetH / 15.0)),
            PrayerTime("isha", "এশা", mins(noon + ishaH / 15.0))
        )
    }

    /** Index of the next upcoming prayer (ignores sunrise for countdown purposes). */
    fun nextPrayerIndex(times: List<PrayerTime>, nowMinutes: Int): Int {
        val order = times.filter { it.id != "sunrise" }
        val idx = order.indexOfFirst { it.totalMinutes > nowMinutes }
        return if (idx >= 0) times.indexOf(order[idx]) else times.indexOfFirst { it.id == "fajr" }
    }

    fun nextPrayerEpochMillis(date: LocalDate, time: PrayerTime): Long =
        date.atTime(time.totalMinutes / 60, time.totalMinutes % 60)
            .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun julianDay(year0: Int, month0: Int, day: Int, hour: Double): Double {
        var y = year0; var m = month0
        if (m <= 2) { y -= 1; m += 12 }
        val a = y / 100
        val b = 2 - a + a / 4
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5 + hour / 24.0
    }

    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val t = (jd - 2451545.0) / 36525.0
        val l0 = (280.46646 + 36000.76983 * t + 0.0003032 * t * t) % 360.0
        val m = 357.52911 + 35999.05029 * t - 0.0001537 * t * t
        val mRad = Math.toRadians(m)
        val e = 0.016708634 - 0.000042037 * t - 0.0000001267 * t * t
        val c = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(mRad) +
            (0.019993 - 0.000101 * t) * sin(2 * mRad) +
            0.000289 * sin(3 * mRad)
        val trueLong = l0 + c
        val omega = 125.04 - 1934.136 * t
        val lambda = trueLong - 0.00569 - 0.00478 * sin(Math.toRadians(omega))
        val eps0 = 23.0 + (26.0 + (21.448 - 46.815 * t - 0.00059 * t * t + 0.001813 * t * t * t) / 60.0) / 60.0
        val eps = eps0 + 0.00256 * cos(Math.toRadians(omega))
        val decl = Math.toDegrees(asin(sin(Math.toRadians(eps)) * sin(Math.toRadians(lambda))))
        val y = tan(Math.toRadians(eps / 2)).pow(2)
        val l0Rad = Math.toRadians(l0)
        val eotRad = y * sin(2 * l0Rad) - 2 * e * sin(mRad) +
            4 * e * y * sin(mRad) * cos(2 * l0Rad) -
            0.5 * y * y * sin(4 * l0Rad) - 1.5 * e * e * sin(2 * mRad)
        return decl to 4.0 * Math.toDegrees(eotRad)
    }

    private fun hourAngle(lat: Double, decl: Double, elevDeg: Double): Double {
        val latR = Math.toRadians(lat)
        val declR = Math.toRadians(decl)
        val eR = Math.toRadians(elevDeg)
        val cosH = (sin(eR) - sin(latR) * sin(declR)) / (cos(latR) * cos(declR))
        return Math.toDegrees(acos(cosH.coerceIn(-1.0, 1.0)))
    }
}
