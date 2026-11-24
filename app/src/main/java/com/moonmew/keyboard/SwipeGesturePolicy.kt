package com.moonmew.keyboard

/** Swipe typing must begin on a letter, not a toolbar or action key. */
internal object SwipeGesturePolicy {
    fun canStart(keyValue: String?): Boolean =
        keyValue?.length == 1 && keyValue[0] in 'a'..'z'
}
