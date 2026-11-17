package com.hidayah.android.ui.navigation

object Routes {
    const val COVER = "cover"
    const val HOME = "home"
    const val QURAN = "quran"
    const val QIBLA = "qibla"
    const val TASBIH = "tasbih"
    const val SETTINGS = "settings"
    const val MENU = "menu"
    const val DESIGN_SYSTEM = "design-system"
    const val DUAS = "duas"
    const val CALENDAR = "calendar"
    const val PROFILE = "profile"
    const val PROFILE_EDIT = "profile_edit"
    const val READER = "reader/{surah}"
    fun reader(surah: Int) = "reader/$surah"

    val tabRoutes = setOf(HOME, QURAN, QIBLA, TASBIH, MENU)

    /** First-run install flow — the bottom navigation bar is hidden here (per product decision). */
    val installFlowRoutes = setOf(COVER)

    /** Maps every in-app route to its owning bottom-nav tab for highlight purposes. */
    fun tabForRoute(route: String?): String? = when (route) {
        HOME -> HOME
        QURAN -> QURAN
        READER -> QURAN
        QIBLA -> QIBLA
        TASBIH -> TASBIH
        DUAS -> MENU
        CALENDAR -> MENU
        SETTINGS -> MENU
        PROFILE -> MENU
        PROFILE_EDIT -> MENU
        DESIGN_SYSTEM -> MENU
        MENU -> MENU
        else -> null
    }

    /** Bottom bar is active on EVERY in-app page; hidden only during the install flow. */
    fun showBottomBar(route: String?): Boolean = route != null && route !in installFlowRoutes
}
