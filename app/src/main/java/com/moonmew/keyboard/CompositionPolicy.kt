package com.moonmew.keyboard

/**
 * Bounds the in-memory phonetic word buffer so transliteration and cursor
 * validation stay predictable even when a user holds a key or types a very
 * long unbroken sequence.
 */
internal object CompositionPolicy {
    const val MAX_COMPOSING_CHARACTERS = 64

    fun shouldCommitBeforeAppend(currentLength: Int): Boolean =
        currentLength >= MAX_COMPOSING_CHARACTERS
}
