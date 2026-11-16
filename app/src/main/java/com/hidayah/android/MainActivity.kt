package com.hidayah.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hidayah.android.ui.components.HidayahBottomBar
import com.hidayah.android.ui.navigation.Routes
import com.hidayah.android.ui.screens.calendar.CalendarScreen
import com.hidayah.android.ui.screens.cover.CoverScreen
import com.hidayah.android.ui.screens.dua.DuasScreen
import com.hidayah.android.ui.screens.home.HomeScreen
import com.hidayah.android.ui.screens.qibla.QiblaScreen
import com.hidayah.android.ui.screens.quran.QuranScreen
import com.hidayah.android.ui.screens.quran.ReaderScreen
import com.hidayah.android.ui.screens.settings.SettingsScreen
import com.hidayah.android.ui.screens.tasbih.TasbihScreen
import com.hidayah.android.ui.screens.designsystem.DesignSystemScreen
import com.hidayah.android.ui.theme.HidayahTheme
import com.hidayah.android.ui.theme.LocalHidayahPalette
import com.hidayah.android.ui.theme.HidayahPalette
import com.hidayah.android.state.HidayahViewModel

private val pal: HidayahPalette @Composable get() = LocalHidayahPalette.current

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // dark status/navigation scrims -> white icons always (brand is dark)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            val themeModel: HidayahViewModel = viewModel()
            val settings by themeModel.settings.collectAsState()
            val dark = when (settings.theme) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            HidayahTheme(darkTheme = dark) { HidayahApp(themeModel) }
        }
    }
}

@Composable
fun HidayahApp(viewModel: HidayahViewModel = viewModel()) {
    val navController = rememberNavController()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route
    val showBottomBar = Routes.showBottomBar(currentRoute)
    val highlightedTab = Routes.tabForRoute(currentRoute)

    Scaffold(
        containerColor = pal.background,
        bottomBar = {
            if (showBottomBar) {
                HidayahBottomBar(highlightedTab) { route ->
                    if (route != highlightedTab) {
                        navController.navigate(route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.COVER,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            composable(Routes.COVER) {
                CoverScreen(onStart = {
                    navController.navigate(Routes.HOME) { popUpTo(Routes.COVER) { inclusive = true } }
                })
            }
            composable(Routes.HOME) {
                HomeScreen(
                    viewModel = viewModel,
                    onOpenDuas = { navController.navigate(Routes.DUAS) },
                    onOpenCalendar = { navController.navigate(Routes.CALENDAR) },
                    onOpenQuran = { navController.navigate(Routes.QURAN) },
                    onOpenQibla = { navController.navigate(Routes.QIBLA) },
                    onOpenTasbih = { navController.navigate(Routes.TASBIH) }
                )
            }
            composable(Routes.QURAN) {
                QuranScreen(viewModel = viewModel, onOpenSurah = { navController.navigate(Routes.reader(it)) })
            }
            composable(
                Routes.READER,
                arguments = listOf(navArgument("surah") { type = NavType.IntType })
            ) { entry ->
                val surah = entry.arguments?.getInt("surah") ?: 1
                ReaderScreen(viewModel = viewModel, surahNumber = surah, onBack = { navController.popBackStack() }, onOpenSurah = { navController.navigate(Routes.reader(it)) })
            }
            composable(Routes.QIBLA) { QiblaScreen(viewModel = viewModel) }
            composable(Routes.TASBIH) { TasbihScreen(viewModel = viewModel) }
            composable(Routes.DUAS) {
                DuasScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Routes.CALENDAR) {
                CalendarScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SETTINGS) { SettingsScreen(viewModel = viewModel, onOpenDesignSystem = { navController.navigate(Routes.DESIGN_SYSTEM) }) }
            composable(Routes.DESIGN_SYSTEM) { DesignSystemScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
