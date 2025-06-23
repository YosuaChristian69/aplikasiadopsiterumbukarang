package id.istts.aplikasiadopsiterumbukarang.utils

import android.content.Context
import android.content.SharedPreferences

// SharedPreferenceManager utility class with multiple constructor options
class SharedPreferenceManager {
    private val sharedPreferences: SharedPreferences

    // Primary constructor - takes Context
    constructor(context: Context) {
        this.sharedPreferences = context.getSharedPreferences("worker_prefs", Context.MODE_PRIVATE)
    }

    // Alternative constructor - takes SharedPreferences directly
    constructor(sharedPreferences: SharedPreferences) {
        this.sharedPreferences = sharedPreferences
    }

    fun getWorkerName(): String? {
        return sharedPreferences.getString("worker_name", null)
    }

    fun setWorkerName(name: String) {
        sharedPreferences.edit().putString("worker_name", name).apply()
    }

    fun getWorkerToken(): String? {
        return sharedPreferences.getString("worker_token", null)
    }

    fun setWorkerToken(token: String) {
        sharedPreferences.edit().putString("worker_token", token).apply()
    }

    fun clearUserSession() {
        sharedPreferences.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return getWorkerToken() != null
    }

    fun getWorkerId(): Int? {
        val id = sharedPreferences.getInt("worker_id", -1)
        return if (id != -1) id else null
    }

    fun setWorkerId(id: Int) {
        sharedPreferences.edit().putInt("worker_id", id).apply()
    }
}