package com.hidayah.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
import com.hidayah.android.ui.screens.menu.MenuScreen
import com.hidayah.android.ui.screens.home.HomeScreen
import com.hidayah.android.ui.screens.profile.ProfileScreen
import com.hidayah.android.ui.screens.profile.ProfileEditScreen
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

class MainActivity : AppCompatActivity() {
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
                    when {
                        // অন্য ট্যাবে যাচ্ছি — মূল ট্যাবে পৌঁছে দাও
                        route != highlightedTab -> navController.navigate(route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        // ট্যাবেরই কোনো সাব-পেজে আছি (যেমন: ক্যালেন্ডার→হোম, প্রোফাইল→সেটিংস, রিডার→কুরআন)
                        // — আগে back stack-এ ট্যাব খুঁজুন; না থাকলে নতুন করে ট্যাব রুট খুলুন
                        currentRoute != null && currentRoute != route -> {
                            val found = navController.popBackStack(route, inclusive = false)
                            if (!found) {
                                navController.navigate(route) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                        // ইতিমধ্যে ট্যাব রুটে — কিচ্ছু নেই
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.COVER,
            // টপ ইনসেট স্ক্রিনগুলো নিজেরাই statusBarsPadding() দিয়ে নেয় —
            // এখানে শুধু নিচে bottomBar-এর উচ্চতা বাদ দেওয়া হয় (ডবল প্যাডিং হতো)।
            modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())
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
                    onOpenTasbih = { navController.navigate(Routes.TASBIH) },
                    onOpenProfile = { navController.navigate(Routes.PROFILE) }
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
                CalendarScreen(onBack = { navController.popBackStack() }, ayahOfDay = viewModel.ayahOfDay)
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onEditProfile = { navController.navigate(Routes.PROFILE_EDIT) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) }
                )
            }
            composable(Routes.PROFILE_EDIT) {
                ProfileEditScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) }
                )
            }
            composable(Routes.MENU) {
                MenuScreen(
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenCalendar = { navController.navigate(Routes.CALENDAR) },
                    onOpenDuas = { navController.navigate(Routes.DUAS) },
                    onOpenProfile = { navController.navigate(Routes.PROFILE) }
                )
            }
            composable(Routes.SETTINGS) { SettingsScreen(viewModel = viewModel, onOpenDesignSystem = { navController.navigate(Routes.DESIGN_SYSTEM) }, onOpenProfile = { navController.navigate(Routes.PROFILE) }) }
            composable(Routes.DESIGN_SYSTEM) { DesignSystemScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
