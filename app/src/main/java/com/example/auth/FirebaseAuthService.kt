package com.example.auth

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

/**
 * Service to manage Firebase Authentication instances and operations
 * including Email/Password Login, Sign Up, and User Profile sync.
 */
class FirebaseAuthService {

    private val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w("FirebaseAuthService", "Firebase Auth initialization skipped or unavailable: ${e.message}")
            null
        }

    val currentFirebaseUser: FirebaseUser?
        get() = try {
            auth?.currentUser
        } catch (e: Exception) {
            null
        }

    fun isAvailable(): Boolean = auth != null

    /**
     * Authenticate existing user with Email and Password
     */
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser?> {
        val authInstance = auth ?: return Result.failure(IllegalStateException("Firebase Auth is not available"))
        return try {
            val authResult = authInstance.signInWithEmailAndPassword(email.trim(), password).await()
            Result.success(authResult.user)
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Sign in failed", e)
            Result.failure(e)
        }
    }

    /**
     * Register a new user with Email, Password, and Display Name
     */
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<FirebaseUser?> {
        val authInstance = auth ?: return Result.failure(IllegalStateException("Firebase Auth is not available"))
        return try {
            val authResult = authInstance.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user
            if (user != null && displayName.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                user.updateProfile(profileUpdates).await()
            }
            Result.success(user)
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Sign up failed", e)
            Result.failure(e)
        }
    }

    /**
     * Send password reset email to user via Firebase Authentication
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        val authInstance = auth ?: return Result.failure(IllegalStateException("Firebase Auth is not available"))
        return try {
            authInstance.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Password reset failed", e)
            Result.failure(e)
        }
    }

    /**
     * Update display name of the currently logged-in Firebase user
     */
    suspend fun updateDisplayName(displayName: String): Result<Unit> {
        val user = currentFirebaseUser ?: return Result.failure(IllegalStateException("لاگ ان شدہ صارف موجود نہیں ہے"))
        return try {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName.trim())
                .build()
            user.updateProfile(profileUpdates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Display name update failed", e)
            Result.failure(e)
        }
    }

    /**
     * Update password of the currently logged-in Firebase user
     */
    suspend fun updatePassword(newPassword: String): Result<Unit> {
        val user = currentFirebaseUser ?: return Result.failure(IllegalStateException("لاگ ان شدہ صارف موجود نہیں ہے"))
        return try {
            user.updatePassword(newPassword.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Password update failed", e)
            Result.failure(e)
        }
    }

    /**
     * Sign out current Firebase user
     */
    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Sign out error", e)
        }
    }
}
