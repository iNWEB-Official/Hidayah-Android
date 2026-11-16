package com.hidayah.android.ui.screens.cover

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hidayah.android.ui.components.HidayahBackground
import com.hidayah.android.ui.components.HidayahEmblem
import com.hidayah.android.ui.theme.*

private val pal: HidayahPalette @Composable get() = LocalHidayahPalette.current

/**
 * Screen 00-A — install splash, pixel-aligned with mockup 00 (00-cover-design-system.svg).
 * Star lattice + gold double-ring emblem + gold logotype + tagline; single gold CTA
 * enters the onboarding flow (mockup carries no CTA — required entry point in-app).
 */
@Composable
fun CoverScreen(onStart: () -> Unit) {
    HidayahBackground {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(72.dp))
            HidayahEmblem(Modifier.size(150.dp))
            Spacer(Modifier.height(30.dp))
            Text(
                "হিদায়াহ",
                style = HidayahType.Display.copy(
                    fontFamily = NotoSerifBengali,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 54.sp,
                    lineHeight = 64.sp
                ),
                color = Gold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "H I D A Y A H",
                style = HidayahType.Overline.copy(letterSpacing = 9.sp, fontSize = 13.sp),
                color = GoldSoft
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "হিদায়াতের পথে আপনার সঙ্গী",
                style = HidayahType.Body,
                color = Cream
            )
            Spacer(Modifier.height(72.dp))
            Button(
                onClick = onStart,
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = EmeraldDark),
                contentPadding = PaddingValues(horizontal = 78.dp, vertical = 17.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text("শুরু করুন", style = HidayahType.TitleSans.copy(fontSize = 17.sp))
            }
        }
    }
}
