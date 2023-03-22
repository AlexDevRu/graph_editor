package com.example.mobilepaint

import android.content.Context
import android.graphics.Color
import androidx.core.content.edit

class SharedPrefsUtils(context: Context) {

    companion object {
        private const val strokeWidthKey = "strokeWidth"
        private const val colorKey = "color"
    }

    private val preferences by lazy {
        context.getSharedPreferences("MobilePaint", Context.MODE_PRIVATE)
    }

    var strokeWidth: Float
        get() = preferences.getFloat(strokeWidthKey, 5f)
        set(value) = preferences.edit { putFloat(strokeWidthKey, value) }

    var color: Int
        get() = preferences.getInt(colorKey, Color.BLACK)
        set(value) = preferences.edit { putInt(colorKey, value) }

}