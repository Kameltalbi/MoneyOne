package com.smartbudget.data

import android.content.Context
import android.content.SharedPreferences

class UserManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val DEFAULT_USER_ID = "default_user"
    }

    fun getCurrentUserId(): String {
        return prefs.getString(KEY_CURRENT_USER_ID, null) ?: DEFAULT_USER_ID
    }

    fun setCurrentUserId(userId: String) {
        prefs.edit().putString(KEY_CURRENT_USER_ID, userId).apply()
    }

    fun hasUser(): Boolean {
        return prefs.contains(KEY_CURRENT_USER_ID)
    }

    fun clearCurrentUser() {
        prefs.edit().remove(KEY_CURRENT_USER_ID).apply()
    }
}
