package com.hidayah.android.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore(name = "hidayah_prefs")

data class SettingsState(
    val hanafiAsr: Boolean = true,
    val notifSeheriIftar: Boolean = true,
    val notifTahajjud: Boolean = false,
    val notifJumuah: Boolean = true,
    val cityKey: String = "dhaka",
    val lastReadSurah: Int = 18,
    val lastReadAyahStart: Int = 1,
    val lastReadAyahEnd: Int = 44,
    val tasbihSelected: String = "সুবহানাল্লাহ",
    val tasbihTarget: Int = 100,
    val tasbihCount: Int = 0,
    val tasbihTodayTotal: Int = 0,
    val favoriteDuaIds: Set<Int> = emptySet(),
    val theme: String = "system", // "system" | "light" | "dark"
    val useGpsLocation: Boolean = false, // true = জিপিএস-অবস্থান (বাংলাদেশের বাইরেরও) ব্যবহার হবে
    val gpsLatitude: Double = 0.0,
    val gpsLongitude: Double = 0.0,
    val gpsLabel: String = "", // রিভার্স-জিওকোডেড স্থান-নাম; খালি/ব্যর্থ হলে "বর্তমান অবস্থান"
    val userName: String = "",
    val profileImagePath: String = "" // অ্যাপ-প্রাইভেট স্টোরেஜের পরম পাথ; খালি মানে ডিফল্ট আভাতার
)

class SettingsStore(context: Context) {

    private val store = context.applicationContext.dataStore

    private object Keys {
        val HANAFI_ASR = booleanPreferencesKey("hanafi_asr")
        val NOTIF_SEHERI_IFTAR = booleanPreferencesKey("notif_seheri_iftar")
        val NOTIF_TAHAJJUD = booleanPreferencesKey("notif_tahajjud")
        val NOTIF_JUMUAH = booleanPreferencesKey("notif_jumuah")
        val CITY_KEY = stringPreferencesKey("city_key")
        val LAST_READ_SURAH = intPreferencesKey("last_read_surah")
        val LAST_READ_AYAH_START = intPreferencesKey("last_read_ayah_start")
        val LAST_READ_AYAH_END = intPreferencesKey("last_read_ayah_end")
        val TASBIH_SELECTED = stringPreferencesKey("tasbih_selected")
        val TASBIH_TARGET = intPreferencesKey("tasbih_target")
        val TASBIH_COUNT = intPreferencesKey("tasbih_count")
        val TASBIH_TODAY_TOTAL = intPreferencesKey("tasbih_today_total")
        val TASBIH_TODAY_DATE = stringPreferencesKey("tasbih_today_date")
        val FAVORITE_DUAS = stringPreferencesKey("favorite_duas")
        val THEME = stringPreferencesKey("theme_preference")
        val GPS_USE = booleanPreferencesKey("gps_location_enabled")
        val GPS_LAT = doublePreferencesKey("gps_latitude")
        val GPS_LNG = doublePreferencesKey("gps_longitude")
        val GPS_LABEL = stringPreferencesKey("gps_label")
        val USER_NAME = stringPreferencesKey("user_name")
        val PROFILE_IMAGE_PATH = stringPreferencesKey("profile_image_path")
    }

    val settings: Flow<SettingsState> = store.data.map { p ->
        val today = LocalDate.now().toString()
        val savedDate = p[Keys.TASBIH_TODAY_DATE] ?: ""
        SettingsState(
            hanafiAsr = p[Keys.HANAFI_ASR] ?: true,
            notifSeheriIftar = p[Keys.NOTIF_SEHERI_IFTAR] ?: true,
            notifTahajjud = p[Keys.NOTIF_TAHAJJUD] ?: false,
            notifJumuah = p[Keys.NOTIF_JUMUAH] ?: true,
            cityKey = p[Keys.CITY_KEY] ?: "dhaka",
            lastReadSurah = p[Keys.LAST_READ_SURAH] ?: 18,
            lastReadAyahStart = p[Keys.LAST_READ_AYAH_START] ?: 1,
            lastReadAyahEnd = p[Keys.LAST_READ_AYAH_END] ?: 44,
            tasbihSelected = p[Keys.TASBIH_SELECTED] ?: "সুবহানাল্লাহ",
            tasbihTarget = p[Keys.TASBIH_TARGET] ?: 100,
            tasbihCount = p[Keys.TASBIH_COUNT] ?: 0,
            tasbihTodayTotal = if (savedDate == today) (p[Keys.TASBIH_TODAY_TOTAL] ?: 0) else 0,
            favoriteDuaIds = p[Keys.FAVORITE_DUAS].orEmpty()
                .split(',').mapNotNull { it.trim().toIntOrNull() }.toSet(),
            theme = p[Keys.THEME] ?: "system",
            useGpsLocation = p[Keys.GPS_USE] ?: false,
            gpsLatitude = p[Keys.GPS_LAT] ?: 0.0,
            gpsLongitude = p[Keys.GPS_LNG] ?: 0.0,
            gpsLabel = p[Keys.GPS_LABEL] ?: "",
            userName = p[Keys.USER_NAME] ?: "",
            profileImagePath = p[Keys.PROFILE_IMAGE_PATH] ?: ""
        )
    }

    suspend fun setHanafiAsr(value: Boolean) = store.edit { it[Keys.HANAFI_ASR] = value }
    suspend fun setNotifSeheriIftar(value: Boolean) = store.edit { it[Keys.NOTIF_SEHERI_IFTAR] = value }
    suspend fun setNotifTahajjud(value: Boolean) = store.edit { it[Keys.NOTIF_TAHAJJUD] = value }
    suspend fun setNotifJumuah(value: Boolean) = store.edit { it[Keys.NOTIF_JUMUAH] = value }
    // ম্যানুয়াল শহর বেছে নিলে জিপিএস মোড বন্ধ হয়ে যায়
    suspend fun setCityKey(value: String) = store.edit {
        it[Keys.CITY_KEY] = value
        it[Keys.GPS_USE] = false
    }
    suspend fun setGpsLocation(lat: Double, lng: Double, label: String) = store.edit {
        it[Keys.GPS_USE] = true
        it[Keys.GPS_LAT] = lat
        it[Keys.GPS_LNG] = lng
        it[Keys.GPS_LABEL] = label.trim().take(80)
    }
    suspend fun clearGpsLocation() = store.edit { it[Keys.GPS_USE] = false }
    suspend fun setLastRead(surah: Int, ayahStart: Int, ayahEnd: Int) = store.edit {
        it[Keys.LAST_READ_SURAH] = surah; it[Keys.LAST_READ_AYAH_START] = ayahStart; it[Keys.LAST_READ_AYAH_END] = ayahEnd
    }
    suspend fun setTasbihSelected(value: String) = store.edit { it[Keys.TASBIH_SELECTED] = value; it[Keys.TASBIH_COUNT] = 0 }
    suspend fun setTasbihTarget(value: Int) = store.edit { p ->
        p[Keys.TASBIH_TARGET] = value
        if ((p[Keys.TASBIH_COUNT] ?: 0) > value) p[Keys.TASBIH_COUNT] = 0
    }
    suspend fun incrementTasbih() = store.edit { p ->
        val today = LocalDate.now().toString()
        val savedDate = p[Keys.TASBIH_TODAY_DATE] ?: ""
        val todayTotal = if (savedDate == today) (p[Keys.TASBIH_TODAY_TOTAL] ?: 0) else 0
        p[Keys.TASBIH_COUNT] = (p[Keys.TASBIH_COUNT] ?: 0) + 1
        p[Keys.TASBIH_TODAY_TOTAL] = todayTotal + 1
        p[Keys.TASBIH_TODAY_DATE] = today
    }
    suspend fun resetTasbihCount() = store.edit { it[Keys.TASBIH_COUNT] = 0 }
    suspend fun setTheme(value: String) = store.edit { it[Keys.THEME] = value }
    suspend fun setUserName(value: String) = store.edit { it[Keys.USER_NAME] = value.trim().take(60) }
    suspend fun setProfileImagePath(value: String) = store.edit { it[Keys.PROFILE_IMAGE_PATH] = value }

    suspend fun toggleFavoriteDua(id: Int) = store.edit { p ->
        val current = p[Keys.FAVORITE_DUAS].orEmpty().split(',').mapNotNull { it.trim().toIntOrNull() }.toMutableSet()
        if (!current.add(id)) current.remove(id)
        p[Keys.FAVORITE_DUAS] = current.joinToString(",")
    }
}
