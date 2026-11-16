package com.hidayah.android.util

import org.junit.Assert.assertEquals
import org.junit.Test

class BnDigitsTest {

    @Test
    fun convertsLatinDigitsToBengali() {
        assertEquals("০১২৩৪৫৬৭৮৯", BnDigits.of("0123456789"))
    }

    @Test
    fun leavesNonDigitCharactersUntouched() {
        assertEquals("সূরা ১০৮: আল-কাউসার", BnDigits.of("সূরা 108: আল-কাউসার"))
        assertEquals("৫:৩০", BnDigits.of("5:30"))
    }

    @Test
    fun convertsIntDirectly() {
        assertEquals("২০২৬", BnDigits.of(2026))
        assertEquals("০", BnDigits.of(0))
    }

    @Test
    fun formatsMinutesAsBengaliTime() {
        assertEquals("৪:৫০", BnDigits.minutesToBnTime(290))   // 04:50 Fajr
        assertEquals("১২:০৫", BnDigits.minutesToBnTime(725))
        assertEquals("০:০০", BnDigits.minutesToBnTime(0))
        assertEquals("২৩:৫৯", BnDigits.minutesToBnTime(23 * 60 + 59))
    }

    @Test
    fun hourPaddingBehaviorIsPreserved() {
        // Implementation detail locked in intentionally: hour is NOT zero-padded, minutes are.
        assertEquals("৯:০৫", BnDigits.minutesToBnTime(9 * 60 + 5))
    }
}
