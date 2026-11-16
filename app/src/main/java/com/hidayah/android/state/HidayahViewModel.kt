package com.hidayah.android.state

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hidayah.android.data.Cities
import com.hidayah.android.data.dua.DuaRepository
import com.hidayah.android.data.hijri.HijriDates
import com.hidayah.android.data.prefs.SettingsStore
import com.hidayah.android.data.prefs.SettingsState
import com.hidayah.android.data.prayer.PrayerTime
import com.hidayah.android.data.prayer.PrayerTimeCalculator
import com.hidayah.android.data.quran.QuranRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

data class HomeUi(
    val today: LocalDate = LocalDate.now(),
    val prayerTimes: List<PrayerTime> = emptyList(),
    val nextIndex: Int = 0,
    val nowMinutes: Int = 0,
    val countdownMinutes: Int = 0,
    val cityName: String = "ঢাকা"
)

class HidayahViewModel(app: Application) : AndroidViewModel(app) {

    val settingsStore = SettingsStore(app).also { store ->
        // সদস্যপদ-তারিখ — প্রথমবার অ্যাপ খোলার সময় লিখা হয়
        viewModelScope.launch { store.ensureMemberSince() }
    }
    private val prayerCalculator = PrayerTimeCalculator()
    private val quranRepository = QuranRepository(app)
    private val duaRepository = DuaRepository(app)

    val settings: StateFlow<SettingsState> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState())

    /** Ticks every 30 seconds so the countdown stays fresh. */
    private val nowMinutes: Flow<Int> = flow {
        while (true) {
            emit(LocalTime.now().let { it.hour * 60 + it.minute })
            kotlinx.coroutines.delay(30_000)
        }
    }

    /** জিপিএস মোডে থাকলে আসল কো-অর্ডিনেট + ডিভাইসের টাইমজোন; নয়তো নির্বাচিত শহর। */
    fun effectiveGeo(s: SettingsState): PrayerTimeCalculator.GeoLocation =
        if (s.useGpsLocation) {
            val tz = java.util.TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 3_600_000.0
            PrayerTimeCalculator.GeoLocation(s.gpsLatitude, s.gpsLongitude, tz)
        } else {
            val c = Cities.byKey(s.cityKey)
            PrayerTimeCalculator.GeoLocation(c.lat, c.lng, c.tzOffset)
        }

    /** UI-তে দেখানোর অবস্থান-নাম (জিপিএস লেবেল বা শহরের নাম)। */
    fun locationLabel(s: SettingsState): String =
        if (s.useGpsLocation) s.gpsLabel.ifBlank { "বর্তমান অবস্থান" } else Cities.byKey(s.cityKey).nameBn

    val homeUi: StateFlow<HomeUi> = combine(settings, nowMinutes) { s, now ->
        val today = LocalDate.now()
        val times = prayerCalculator.timesFor(today, effectiveGeo(s), s.hanafiAsr)
        val nextIdx = prayerCalculator.nextPrayerIndex(times, now)
        val next = times[nextIdx]
        val countdown = if (next.totalMinutes > now) next.totalMinutes - now
        else (1440 - now) + times.first { it.id == "fajr" }.totalMinutes
        HomeUi(today, times, nextIdx, now, countdown, locationLabel(s))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUi())

    val surahs by lazy { quranRepository.loadSurahs() }
    val fatihaAyahs by lazy { quranRepository.loadFatihaAyahs() }
    val duas by lazy { duaRepository.loadDuas() }
    val ayahsOfDay = quranRepository.ayahsOfDay
    val ayahOfDay get() = ayahsOfDay[(LocalDate.now().toEpochDay() % ayahsOfDay.size).toInt()]

    fun surahByNumber(n: Int) = surahs.first { it.number == n }

    fun currentBearing(s: SettingsState): Double {
        val g = effectiveGeo(s)
        return com.hidayah.android.data.quran.QiblaMath.bearingTo(g.lat, g.lng)
    }

    fun currentDistanceKm(s: SettingsState): Double {
        val g = effectiveGeo(s)
        return com.hidayah.android.data.quran.QiblaMath.distanceKm(g.lat, g.lng)
    }

    // পুরনো city-key API (ব্যাককম্প্যাট)
    fun cityBearing(cityKey: String): Double {
        val c = Cities.byKey(cityKey)
        return com.hidayah.android.data.quran.QiblaMath.bearingTo(c.lat, c.lng)
    }

    fun cityDistanceKm(cityKey: String): Double {
        val c = Cities.byKey(cityKey)
        return com.hidayah.android.data.quran.QiblaMath.distanceKm(c.lat, c.lng)
    }

    fun setGpsLocation(lat: Double, lng: Double, label: String) =
        viewModelScope.launch { settingsStore.setGpsLocation(lat, lng, label) }

    fun clearGpsLocation() = viewModelScope.launch { settingsStore.clearGpsLocation() }

    // --- tasbih actions ---
    fun incrementTasbih() = viewModelScope.launch { settingsStore.incrementTasbih() }
    fun resetTasbihCount() = viewModelScope.launch { settingsStore.resetTasbihCount() }
    fun selectTasbih(value: String) = viewModelScope.launch { settingsStore.setTasbihSelected(value) }
    fun setTasbihTarget(value: Int) = viewModelScope.launch { settingsStore.setTasbihTarget(value) }
    fun setTheme(value: String) = viewModelScope.launch { settingsStore.setTheme(value) }
    fun setUserName(value: String) = viewModelScope.launch { settingsStore.setUserName(value) }
    fun setLanguage(value: String) = viewModelScope.launch { settingsStore.setLanguage(value) }
    fun setProfileImagePath(value: String) = viewModelScope.launch { settingsStore.setProfileImagePath(value) }
    fun setProfileBio(value: String) = viewModelScope.launch { settingsStore.setProfileBio(value) }
    fun setCoverImagePath(value: String) = viewModelScope.launch { settingsStore.setCoverImagePath(value) }

    // --- settings actions ---
    fun setHanafiAsr(v: Boolean) = viewModelScope.launch { settingsStore.setHanafiAsr(v) }
    fun setNotifSeheriIftar(v: Boolean) = viewModelScope.launch { settingsStore.setNotifSeheriIftar(v) }
    fun setNotifTahajjud(v: Boolean) = viewModelScope.launch { settingsStore.setNotifTahajjud(v) }
    fun setNotifJumuah(v: Boolean) = viewModelScope.launch { settingsStore.setNotifJumuah(v) }
    fun setCity(key: String) = viewModelScope.launch { settingsStore.setCityKey(key) }

    // --- quran / dua actions ---
    fun markRead(surah: Int, ayahStart: Int, ayahEnd: Int) =
        viewModelScope.launch { settingsStore.setLastRead(surah, ayahStart, ayahEnd) }

    fun toggleFavoriteDua(id: Int) = viewModelScope.launch { settingsStore.toggleFavoriteDua(id) }

    fun hijriTodayLabel(): String = HijriDates.todayLabelBn()
}
