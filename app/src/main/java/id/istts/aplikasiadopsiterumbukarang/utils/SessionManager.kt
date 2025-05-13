package id.istts.aplikasiadopsiterumbukarang.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = prefs.edit()

    companion object {
        const val PREF_NAME = "coral_reef_app_prefs"
        const val USER_TOKEN = "user_token"
        const val USER_NAME = "user_name"
        const val USER_EMAIL = "user_email"
        const val USER_STATUS = "user_status"
        const val IS_LOGGED_IN = "is_logged_in"
    }

    fun saveAuthToken(token: String) {
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun saveUserDetails(name: String, email: String, status: String) {
        editor.putString(USER_NAME, name)
        editor.putString(USER_EMAIL, email)
        editor.putString(USER_STATUS, status)
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGGED_IN, false)
    }

    fun clearSession() {
        editor.clear()
        editor.apply()
    }
}