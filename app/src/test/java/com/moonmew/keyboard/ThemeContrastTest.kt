package com.moonmew.keyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeContrastTest {
    @Test fun lightPastelPalettesUseDarkText() {
        val pink = ThemeContrast.foregroundFor(0xFFFFD5E2.toInt(), 0xFFFFF8F5.toInt())
        val glass = ThemeContrast.foregroundFor(0xFFFF83BA.toInt(), 0xFFFFF8FC.toInt())
        val kitty = ThemeContrast.foregroundFor(0xFFD8C2E8.toInt(), 0xFFF4D7E8.toInt())

        assertEquals(0xFF49334E.toInt(), pink)
        assertEquals(0xFF49334E.toInt(), glass)
        assertEquals(0xFF49334E.toInt(), kitty)
    }

    @Test fun darkPalettesUseLightText() {
        val blue = ThemeContrast.foregroundFor(0xFF416B8B.toInt(), 0xFF1D2B3E.toInt())
        val lavender = ThemeContrast.foregroundFor(0xFF795A99.toInt(), 0xFF302541.toInt())
        val midnight = ThemeContrast.foregroundFor(0xFF17151B.toInt(), 0xFF09090F.toInt())

        assertEquals(0xFFF8F2FF.toInt(), blue)
        assertEquals(0xFFF8F2FF.toInt(), lavender)
        assertEquals(0xFFF8F2FF.toInt(), midnight)
    }

    @Test fun newBrightAndDarkPalettesChooseReadableForegrounds() {
        assertEquals(
            0xFF49334E.toInt(),
            ThemeContrast.foregroundFor(0xFFFFFFFF.toInt(), 0xFFEFEFEF.toInt())
        )
        assertEquals(
            0xFFF8F2FF.toInt(),
            ThemeContrast.foregroundFor(0xFF101010.toInt(), 0xFF202020.toInt())
        )
    }

    @Test fun resultIsStableRegardlessOfColorOrder() {
        val first = ThemeContrast.foregroundFor(0xFFFFD5E2.toInt(), 0xFF17151B.toInt())
        val reversed = ThemeContrast.foregroundFor(0xFF17151B.toInt(), 0xFFFFD5E2.toInt())
        assertEquals(first, reversed)
    }
}
