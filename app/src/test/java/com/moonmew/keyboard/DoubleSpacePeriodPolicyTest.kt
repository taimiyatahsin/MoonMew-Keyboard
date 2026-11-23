package com.moonmew.keyboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DoubleSpacePeriodPolicyTest {
    @Test fun convertsSecondSpaceAfterWordWithinWindow() {
        assertTrue(DoubleSpacePeriodPolicy.shouldInsertPeriod("a ", 250L, true))
        assertTrue(DoubleSpacePeriodPolicy.shouldInsertPeriod("9 ", 500L, true))
        assertTrue(DoubleSpacePeriodPolicy.shouldInsertPeriod("ক ", 120L, true))
    }

    @Test fun rejectsDisabledOrExpiredShortcut() {
        assertFalse(DoubleSpacePeriodPolicy.shouldInsertPeriod("a ", 250L, false))
        assertFalse(DoubleSpacePeriodPolicy.shouldInsertPeriod("a ", 0L, true))
        assertFalse(DoubleSpacePeriodPolicy.shouldInsertPeriod("a ", 501L, true))
    }

    @Test fun rejectsRepeatedSpacesAndPunctuation() {
        assertFalse(DoubleSpacePeriodPolicy.shouldInsertPeriod("  ", 200L, true))
        assertFalse(DoubleSpacePeriodPolicy.shouldInsertPeriod(". ", 200L, true))
        assertFalse(DoubleSpacePeriodPolicy.shouldInsertPeriod("a.", 200L, true))
        assertFalse(DoubleSpacePeriodPolicy.shouldInsertPeriod("a", 200L, true))
    }
}
