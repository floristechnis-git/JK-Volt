package com.example

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("jkvolt_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME = "app_theme" // "system", "light", "dark"
        private const val KEY_TEXT_SIZE = "text_size" // "normal", "large", "xlarge"
        private const val KEY_LANGUAGE = "app_language" // "en", "es", "hi", "mni"
        private const val KEY_ONBOARDED = "onboarded" // boolean
    }

    var theme: String
        get() = prefs.getString(KEY_THEME, "system") ?: "system"
        set(value) {
            prefs.edit().putString(KEY_THEME, value).apply()
        }

    var textSize: String
        get() = prefs.getString(KEY_TEXT_SIZE, "normal") ?: "normal"
        set(value) {
            prefs.edit().putString(KEY_TEXT_SIZE, value).apply()
        }

    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        set(value) {
            prefs.edit().putString(KEY_LANGUAGE, value).apply()
        }

    var isOnboarded: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_ONBOARDED, value).apply()
        }
}
