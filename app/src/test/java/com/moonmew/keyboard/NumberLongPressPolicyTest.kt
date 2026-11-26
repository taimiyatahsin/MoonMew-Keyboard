package com.moonmew.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NumberLongPressPolicyTest {
    @Test fun numberRowOffersSuperscriptsAndCommonFractions() {
        assertEquals("⁰", NumberLongPressPolicy.alternateFor('0'))
        assertEquals("¹", NumberLongPressPolicy.alternateFor('1'))
        assertEquals("²", NumberLongPressPolicy.alternateFor('2'))
        assertEquals("⅔", NumberLongPressPolicy.alternateFor('3'))
        assertEquals("¾", NumberLongPressPolicy.alternateFor('4'))
        assertEquals("½", NumberLongPressPolicy.alternateFor('5'))
        assertEquals("⅚", NumberLongPressPolicy.alternateFor('6'))
        assertEquals("⅞", NumberLongPressPolicy.alternateFor('7'))
        assertEquals("⅜", NumberLongPressPolicy.alternateFor('8'))
        assertEquals("⅝", NumberLongPressPolicy.alternateFor('9'))
    }

    @Test fun nonDigitsHaveNoNumberAlternative() {
        assertNull(NumberLongPressPolicy.alternateFor('a'))
        assertNull(NumberLongPressPolicy.alternateFor(' '))
    }
}
