package com.bodiart.defense.util

import android.util.Log


fun log(tag: String, message: String, throwable: Throwable? = null) {
    Log.d(tag, message, throwable)
}

fun testLog(message: String) {
    Log.d(TEST_LOG_TAG, message)
}