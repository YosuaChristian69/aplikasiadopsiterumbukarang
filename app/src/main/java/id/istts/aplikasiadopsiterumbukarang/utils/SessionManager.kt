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
        const val USER_ID = "id_user"
        const val USER_IMAGE_PATH = "user_image_path"
        const val USER_BALANCE = "user_balance"
        const val USER_JOINED_AT = "user_joined_at"
        const val IS_LOGGED_IN = "is_logged_in"
    }

    fun saveAuthToken(token: String) {
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

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

    fun fetchUserImagePath(): String? {
        return prefs.getString(USER_IMAGE_PATH, null)
    }

//    fun fetchUserBalance(): Float {
//        return prefs.getFloat(USER_BALANCE, 0.0f)
//    }
//
//    fun fetchUserJoinedAt(): String? {
//        return prefs.getString(USER_JOINED_AT, null)
//    }

    fun saveUserDetails(id:Int , name: String, email: String, status: String) {
        editor.putInt(USER_ID, id)
        editor.putString(USER_NAME, name)
        editor.putString(USER_EMAIL, email)
        editor.putString(USER_STATUS, status)
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.apply()
    }

    // Enhanced method to save complete user details
    fun saveCompleteUserDetails(
        userId: Int,
        name: String,
        email: String,
        status: String,
        imagePath: String? = null,
        balance: Float = 0.0f,
        joinedAt: String? = null
    ) {
        editor.putInt(USER_ID, userId)
        editor.putString(USER_NAME, name)
        editor.putString(USER_EMAIL, email)
        editor.putString(USER_STATUS, status)
        imagePath?.let { editor.putString(USER_IMAGE_PATH, it) }
        editor.putFloat(USER_BALANCE, balance)
        joinedAt?.let { editor.putString(USER_JOINED_AT, it) }
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.apply()
    }

    // Method to update only profile image
    fun updateUserImagePath(imagePath: String) {
        editor.putString(USER_IMAGE_PATH, imagePath)
        editor.apply()
    }

    // Method to update user details from EditProfile response
    fun updateUserDetailsFromResponse(
        name: String,
        email: String,
        status: String,
        imagePath: String? = null
    ) {
        editor.putString(USER_NAME, name)
        editor.putString(USER_EMAIL, email)
        editor.putString(USER_STATUS, status)
        imagePath?.let { editor.putString(USER_IMAGE_PATH, it) }
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