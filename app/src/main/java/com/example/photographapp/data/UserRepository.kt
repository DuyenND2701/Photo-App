package com.example.photographapp.data

import android.content.Context
import android.content.SharedPreferences

class UserRepository(private val context: Context) {
    object PrefsManager {

        private const val PREF_NAME = "app"

        private fun getPrefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }

        fun setPremium(context: Context, value: Boolean) {
            getPrefs(context).edit().putBoolean("is_premium", value).apply()
        }

        fun isPremium(context: Context): Boolean {
            return getPrefs(context).getBoolean("is_premium", false)
        }
    }

}