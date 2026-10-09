package com.hoaxdetector.app.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("hoax_detector_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_IS_GUEST = "is_guest"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
    }

    fun login(name: String, email: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putBoolean(KEY_IS_GUEST, false)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    fun loginAsGuest() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putBoolean(KEY_IS_GUEST, true)
            .putString(KEY_USER_NAME, "Pengguna Tamu")
            .putString(KEY_USER_EMAIL, "tamu@hoaxdetector.app")
            .apply()
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    fun isSessionActive(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun isGuest(): Boolean {
        return prefs.getBoolean(KEY_IS_GUEST, false)
    }

    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, if (isGuest()) "Pengguna Tamu" else "Pengguna") ?: "Pengguna"
    }

    fun getUserEmail(): String {
        return prefs.getString(KEY_USER_EMAIL, if (isGuest()) "tamu@hoaxdetector.app" else "") ?: ""
    }
}
