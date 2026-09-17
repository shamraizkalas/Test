package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.json.JSONObject

data class FirebaseConfigData(
    val projectId: String,
    val applicationId: String,
    val apiKey: String,
    val isCustomConfig: Boolean = false
)

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private const val PREFS_NAME = "firebase_cloud_config"
    private const val KEY_PROJECT_ID = "firebase_project_id"
    private const val KEY_APP_ID = "firebase_app_id"
    private const val KEY_API_KEY = "firebase_api_key"

    // Pre-configured default project provided by app owner
    const val DEFAULT_PROJECT_ID = "fintrack-ai-z8r9w"
    const val DEFAULT_APP_ID = "1:1061954864816:android:ced021f49376a101f29d75"
    const val DEFAULT_API_KEY = "AIzaSyAdx3ZCp-SP6vF4sQYBs2C25ve52KOTT1w"
    const val DEFAULT_STORAGE_BUCKET = "fintrack-ai-z8r9w.firebasestorage.app"

    fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Checks if any FirebaseApp is currently initialized in this process
     */
    fun isInitialized(context: Context): Boolean {
        return try {
            FirebaseApp.getApps(context.applicationContext).isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Retrieve stored custom Firebase configuration or default embedded project
     */
    fun getSavedConfig(context: Context): FirebaseConfigData {
        val prefs = getPrefs(context)
        val projectId = prefs.getString(KEY_PROJECT_ID, null)?.trim()
        val appId = prefs.getString(KEY_APP_ID, null)?.trim()
        val apiKey = prefs.getString(KEY_API_KEY, null)?.trim()

        return if (!projectId.isNullOrBlank() && !appId.isNullOrBlank() && !apiKey.isNullOrBlank()) {
            FirebaseConfigData(
                projectId = projectId,
                applicationId = appId,
                apiKey = apiKey,
                isCustomConfig = true
            )
        } else {
            FirebaseConfigData(
                projectId = DEFAULT_PROJECT_ID,
                applicationId = DEFAULT_APP_ID,
                apiKey = DEFAULT_API_KEY,
                isCustomConfig = false
            )
        }
    }

    /**
     * Attempts to initialize FirebaseApp:
     * 1. If already initialized, returns true.
     * 2. Tries default FirebaseApp.initializeApp(context) (from google-services.json if present).
     * 3. Tries saved config or embedded project options.
     */
    fun initialize(context: Context): Boolean {
        val appContext = context.applicationContext
        if (isInitialized(appContext)) {
            return true
        }

        // 1. Try default initialization from packaged resources (google-services.json)
        try {
            val defaultApp = FirebaseApp.initializeApp(appContext)
            if (defaultApp != null) {
                Log.i(TAG, "Firebase initialized using default resources: ${defaultApp.name}")
                return true
            }
        } catch (e: Exception) {
            Log.d(TAG, "Default Firebase initialization note: ${e.message}")
        }

        // 2. Try saved custom configuration or embedded project
        val config = getSavedConfig(appContext)
        val projId = config.projectId.ifBlank { DEFAULT_PROJECT_ID }
        val appId = config.applicationId.ifBlank { DEFAULT_APP_ID }
        val apiKey = config.apiKey.ifBlank { DEFAULT_API_KEY }

        return try {
            val options = FirebaseOptions.Builder()
                .setProjectId(projId)
                .setApplicationId(appId)
                .setApiKey(apiKey)
                .setStorageBucket(DEFAULT_STORAGE_BUCKET)
                .build()
            FirebaseApp.initializeApp(appContext, options)
            Log.i(TAG, "Firebase initialized for project: $projId")
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize Firebase with options: ${e.message}")
            false
        }
    }

    /**
     * Save custom Firebase configuration and initialize FirebaseApp
     */
    fun saveConfigAndInitialize(
        context: Context,
        projectId: String,
        appId: String,
        apiKey: String
    ): Pair<Boolean, String> {
        val appContext = context.applicationContext
        val cleanProjectId = projectId.trim()
        val cleanAppId = appId.trim()
        val cleanApiKey = apiKey.trim()

        if (cleanProjectId.isBlank()) {
            return Pair(false, "پروجیکٹ آئی ڈی (Project ID) درج کرنا لازمی ہے")
        }
        if (cleanApiKey.isBlank()) {
            return Pair(false, "ویب اے پی آئی کی (API Key) درج کرنا لازمی ہے")
        }
        val finalAppId = if (cleanAppId.isBlank()) {
            "1:163641130981:android:shajranasab"
        } else {
            cleanAppId
        }

        return try {
            val options = FirebaseOptions.Builder()
                .setProjectId(cleanProjectId)
                .setApplicationId(finalAppId)
                .setApiKey(cleanApiKey)
                .build()

            // If already initialized with [DEFAULT], we re-initialize by deleting or re-using
            try {
                val existingApps = FirebaseApp.getApps(appContext)
                for (app in existingApps) {
                    if (app.name == FirebaseApp.DEFAULT_APP_NAME) {
                        // Keep or proceed
                    }
                }
            } catch (_: Exception) {}

            try {
                FirebaseApp.initializeApp(appContext, options)
            } catch (e: Exception) {
                // If already initialized, it might throw IllegalStateException "FirebaseApp name [DEFAULT] already exists!"
                // In that case, we delete and re-init
                try {
                    FirebaseApp.getInstance().delete()
                    FirebaseApp.initializeApp(appContext, options)
                } catch (inner: Exception) {
                    Log.w(TAG, "Re-init note: ${inner.message}")
                }
            }

            // Save to preferences
            getPrefs(appContext).edit()
                .putString(KEY_PROJECT_ID, cleanProjectId)
                .putString(KEY_APP_ID, finalAppId)
                .putString(KEY_API_KEY, cleanApiKey)
                .apply()

            Log.i(TAG, "Firebase successfully configured and initialized for project: $cleanProjectId")
            Pair(true, "فائر بیس پروجیکٹ '$cleanProjectId' کامیابی سے منسلک اور محفوظ ہو گیا ہے")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to configure Firebase: ${e.message}", e)
            Pair(false, "فائر بیس کنکشن میں خرابی: ${e.localizedMessage ?: e.message}")
        }
    }

    /**
     * Parse google-services.json text to extract projectId, appId, and apiKey
     */
    fun parseGoogleServicesJson(jsonContent: String): Triple<String?, String?, String?> {
        return try {
            val json = JSONObject(jsonContent.trim())
            val projectInfo = json.optJSONObject("project_info")
            val projectId = projectInfo?.optString("project_id")

            val clientArray = json.optJSONArray("client")
            var appId: String? = null
            var apiKey: String? = null

            if (clientArray != null && clientArray.length() > 0) {
                val clientObj = clientArray.getJSONObject(0)
                val clientInfo = clientObj.optJSONObject("client_info")
                appId = clientInfo?.optString("mobilesdk_app_id")

                val apiKeys = clientObj.optJSONArray("api_key")
                if (apiKeys != null && apiKeys.length() > 0) {
                    val keyObj = apiKeys.getJSONObject(0)
                    apiKey = keyObj.optString("current_key")
                }
            }

            Triple(projectId, appId, apiKey)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing google-services.json: ${e.message}")
            Triple(null, null, null)
        }
    }

    /**
     * Clear stored custom configuration
     */
    fun clearConfig(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
