package com.smartbudget.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurityManager(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "security_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    companion object {
        private const val KEY_PIN_ENABLED = "pin_enabled"
        private const val KEY_PIN_CODE = "pin_code"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_REQUIRE_ON_LAUNCH = "require_on_launch"
    }
    
    // PIN settings
    fun isPinEnabled(): Boolean = prefs.getBoolean(KEY_PIN_ENABLED, false)
    
    fun setPinEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PIN_ENABLED, enabled).apply()
    }
    
    fun setPinCode(pin: String) {
        prefs.edit().putString(KEY_PIN_CODE, pin).apply()
        setPinEnabled(true)
    }
    
    fun verifyPin(pin: String): Boolean {
        val savedPin = prefs.getString(KEY_PIN_CODE, null)
        return savedPin == pin
    }
    
    fun clearPin() {
        prefs.edit()
            .remove(KEY_PIN_CODE)
            .putBoolean(KEY_PIN_ENABLED, false)
            .apply()
    }
    
    // Biometric settings
    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }
    
    // Launch requirement
    fun isRequiredOnLaunch(): Boolean = prefs.getBoolean(KEY_REQUIRE_ON_LAUNCH, true)
    
    fun setRequireOnLaunch(required: Boolean) {
        prefs.edit().putBoolean(KEY_REQUIRE_ON_LAUNCH, required).apply()
    }
    
    // Check if any security is enabled
    fun isSecurityEnabled(): Boolean = isPinEnabled() || isBiometricEnabled()
}
