package com.smartbudget.firebase

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager(private val context: Context) {
    
    private val auth = FirebaseAuth.getInstance()
    private val prefs = context.getSharedPreferences("firebase_auth", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_USER_ID = "firebase_user_id"
    }
    
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    fun isSignedIn(): Boolean = auth.currentUser != null
    
    fun getUserId(): String? = auth.currentUser?.uid
    
    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            val user = result.user ?: return Result.failure(Exception("No user returned"))
            prefs.edit().putString(KEY_USER_ID, user.uid).apply()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun linkWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
            val result = auth.currentUser?.linkWithCredential(credential)?.await()
            val user = result?.user ?: return Result.failure(Exception("Failed to link account"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("No user returned"))
            prefs.edit().putString(KEY_USER_ID, user.uid).apply()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createAccount(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("No user returned"))
            prefs.edit().putString(KEY_USER_ID, user.uid).apply()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun signOut() {
        auth.signOut()
        prefs.edit().remove(KEY_USER_ID).apply()
    }
}
