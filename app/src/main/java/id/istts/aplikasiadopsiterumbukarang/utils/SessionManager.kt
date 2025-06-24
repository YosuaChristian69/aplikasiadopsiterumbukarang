package id.istts.aplikasiadopsiterumbukarang.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    // Keep only the primary SharedPreferences instance
    private var prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = prefs.edit()
    // REMOVE: private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        const val PREF_NAME = "coral_reef_app_prefs"
        const val USER_TOKEN = "user_token"
        const val USER_NAME = "user_name"
        const val USER_EMAIL = "user_email"
        const val USER_STATUS = "user_status"
        const val USER_ID = "id_user"
        const val IS_LOGGED_IN = "is_logged_in"
    }

    fun saveAuthToken(token: String) {
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    // FIX: This function should return the main 'prefs' instance
    fun getSharedPreferences(): SharedPreferences {
        return prefs
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun fetchUserName(): String? {
        return prefs.getString(USER_NAME, null)
    }

    fun fetchUserId(): Int {
        return prefs.getInt(USER_ID, -1)
    }

    fun fetchUserEmail(): String? {
        return prefs.getString(USER_EMAIL, null)
    }

    fun fetchUserStatus(): String? {
        return prefs.getString(USER_STATUS, null)
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