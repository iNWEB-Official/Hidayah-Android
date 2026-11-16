package com.hidayah.android.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hidayah.android.R

val NotoSerifBengali = FontFamily(
    Font(R.font.noto_serif_bengali_semibold, FontWeight.SemiBold),
    Font(R.font.noto_serif_bengali_bold, FontWeight.Bold)
)
val NotoSansBengali = FontFamily(
    Font(R.font.noto_sans_bengali_regular, FontWeight.Normal),
    Font(R.font.noto_sans_bengali_medium, FontWeight.Medium),
    Font(R.font.noto_sans_bengali_semi_bold, FontWeight.SemiBold),
    Font(R.font.noto_sans_bengali_bold, FontWeight.Bold)
)
val Amiri = FontFamily(
    Font(R.font.amiri_regular, FontWeight.Normal),
    Font(R.font.amiri_bold, FontWeight.Bold)
)

object HidayahType {
    val Display = TextStyle(fontFamily = NotoSerifBengali, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 40.sp)
    val H1 = TextStyle(fontFamily = NotoSerifBengali, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 30.sp)
    val H2 = TextStyle(fontFamily = NotoSerifBengali, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 26.sp)
    val TitleSans = TextStyle(fontFamily = NotoSansBengali, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp)
    val Body = TextStyle(fontFamily = NotoSansBengali, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 24.sp)
    val BodyMedium = TextStyle(fontFamily = NotoSansBengali, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 22.sp)
    val Caption = TextStyle(fontFamily = NotoSansBengali, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp)
    val Overline = TextStyle(fontFamily = NotoSansBengali, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.4.sp)
    val Arabic = TextStyle(fontFamily = Amiri, fontWeight = FontWeight.Normal, fontSize = 24.sp, lineHeight = 44.sp)
    val ArabicLarge = TextStyle(fontFamily = Amiri, fontWeight = FontWeight.Normal, fontSize = 30.sp, lineHeight = 52.sp)
}
