package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import com.example.util.AnnouncementNotificationHelper

data class SyncResult(
    val success: Boolean,
    val membersSynced: Int = 0,
    val requestsSynced: Int = 0,
    val message: String = ""
)

class FirestoreSyncManager private constructor() {

    private var appContext: Context? = null

    fun setApplicationContext(context: Context) {
        this.appContext = context.applicationContext
    }

    /**
     * Safely retrieves or initializes the FirebaseFirestore instance.
     * Configures disk persistence so family tree data is cached and available offline.
     * Returns null if Firebase is not configured or not initialized, preventing unhandled crashes.
     */
    fun getFirestore(): FirebaseFirestore? {
        val ctx = appContext
        if (ctx != null && !FirebaseManager.isInitialized(ctx)) {
            FirebaseManager.initialize(ctx)
        }
        return try {
            val db = FirebaseFirestore.getInstance()
            try {
                // Enable modern persistent disk cache for offline access
                val settings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(
                        PersistentCacheSettings.newBuilder()
                            .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                            .build()
                    )
                    .build()
                db.firestoreSettings = settings
                Log.d(TAG, "Firestore offline persistence enabled with unlimited cache size")
            } catch (e: Exception) {
                try {
                    val fallbackSettings = FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .build()
                    db.firestoreSettings = fallbackSettings
                } catch (e2: Exception) {
                    Log.w(TAG, "Firestore settings configuration note: ${e2.message}")
                }
            }
            db
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore instance unavailable: ${e.message}")
            null
        }
    }

    val firestoreInstance: FirebaseFirestore?
        get() = getFirestore()

    /**
     * Bidirectional sync for family members with Firestore.
     * Falls back to offline cache when internet connection is lost.
     */
    suspend fun syncMembers(dao: FamilyDao): SyncResult = withContext(Dispatchers.IO) {
        val fs = getFirestore()
        if (fs == null) {
            return@withContext SyncResult(
                success = false,
                message = "کلاؤڈ سنکرونائزیشن دستیاب نہیں: فائر بیس کلاؤڈ کنکشن درکار ہے۔ اوپر کلاؤڈ سیٹنگز سے اپنا فائر بیس پروجیکٹ منسلک کریں۔"
            )
        }

        try {
            val membersCollection = fs.collection(COLLECTION_MEMBERS)
            
            // 1. Fetch cloud members with offline cache fallback
            var isOfflineCache = false
            val cloudSnapshot = try {
                membersCollection.get(Source.DEFAULT).await()
            } catch (netEx: Exception) {
                Log.w(TAG, "Network unavailable, retrieving members from Firestore offline persistence: ${netEx.message}")
                try {
                    isOfflineCache = true
                    membersCollection.get(Source.CACHE).await()
                } catch (cacheEx: Exception) {
                    Log.w(TAG, "Firestore cache unavailable: ${cacheEx.message}")
                    null
                }
            }
            var downloadedCount = 0

            if (cloudSnapshot != null && !cloudSnapshot.isEmpty) {
                for (doc in cloudSnapshot.documents) {
                    val id = doc.getLong("id") ?: continue
                    val name = doc.getString("name") ?: ""
                    val fatherId = doc.getLong("fatherId")
                    val fatherName = doc.getString("fatherName")
                    val motherName = doc.getString("motherName")
                    val genderStr = doc.getString("gender") ?: Gender.MALE.name
                    val gender = try { Gender.valueOf(genderStr) } catch (e: Exception) { Gender.MALE }
                    val generation = doc.getLong("generation")?.toInt() ?: 1
                    val spouse = doc.getString("spouse")
                    val location = doc.getString("location")
                    val isDeceased = doc.getBoolean("isDeceased") ?: false
                    val deathNote = doc.getString("deathNote")
                    val notes = doc.getString("notes")
                    val displayOrder = doc.getLong("displayOrder")?.toInt() ?: 0
                    val phone = doc.getString("phone")
                    val occupation = doc.getString("occupation")
                    val birthYear = doc.getString("birthYear")
                    val deathYear = doc.getString("deathYear")

                    val member = FamilyMember(
                        id = id,
                        name = name,
                        fatherId = fatherId,
                        fatherName = fatherName,
                        motherName = motherName,
                        gender = gender,
                        generation = generation,
                        spouse = spouse,
                        location = location,
                        isDeceased = isDeceased,
                        deathNote = deathNote,
                        notes = notes,
                        displayOrder = displayOrder,
                        phone = phone,
                        occupation = occupation,
                        birthYear = birthYear,
                        deathYear = deathYear
                    )
                    dao.insertOrReplace(member)
                    downloadedCount++
                }
            }

            // 2. Upload local members to Cloud
            val localMembers = dao.getAllMembers()
            val batch = fs.batch()
            for (member in localMembers) {
                val docRef = membersCollection.document(member.id.toString())
                val map = hashMapOf<String, Any?>(
                    "id" to member.id,
                    "name" to member.name,
                    "fatherId" to member.fatherId,
                    "fatherName" to member.fatherName,
                    "motherName" to member.motherName,
                    "gender" to member.gender.name,
                    "generation" to member.generation,
                    "spouse" to member.spouse,
                    "location" to member.location,
                    "isDeceased" to member.isDeceased,
                    "deathNote" to member.deathNote,
                    "notes" to member.notes,
                    "displayOrder" to member.displayOrder,
                    "phone" to member.phone,
                    "occupation" to member.occupation,
                    "birthYear" to member.birthYear,
                    "deathYear" to member.deathYear,
                    "lastUpdated" to System.currentTimeMillis()
                )
                batch.set(docRef, map, SetOptions.merge())
            }
            try {
                batch.commit().await()
            } catch (commitEx: Exception) {
                if (isOfflineCache) {
                    Log.w(TAG, "Offline mode active: updates stored in Firestore offline cache: ${commitEx.message}")
                    return@withContext SyncResult(
                        success = true,
                        membersSynced = localMembers.size,
                        message = "آف لائن موڈ: انٹرنیٹ دستیاب نہ ہونے پر بھی فائر بیس کیشے سے ڈیٹا مکمل دستیاب ہے"
                    )
                } else {
                    throw commitEx
                }
            }

            SyncResult(
                success = true,
                membersSynced = localMembers.size,
                message = if (isOfflineCache) {
                    "آف لائن موڈ: فائر بیس کیشے سے خاندانی شجرہ کا ڈیٹا کامیابی سے لوڈ کر لیا گیا ہے (${localMembers.size} افراد)"
                } else {
                    "آن لائن فائر بیس کلاؤڈ کے ساتھ ${localMembers.size} افراد کی معلومات مکمل ہم آہنگ (Sync) ہو گئیں"
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Sync members failed", e)
            SyncResult(
                success = false,
                message = "کلاؤڈ سنکرونائزیشن میں مسئلہ: ${e.localizedMessage ?: "کنکشن کی خرابی"}"
            )
        }
    }

    /**
     * Upload all local members to Cloud Firestore
     */
    suspend fun uploadAllMembersToCloud(dao: FamilyDao): SyncResult = withContext(Dispatchers.IO) {
        val fs = getFirestore()
        if (fs == null) {
            return@withContext SyncResult(
                success = false,
                message = "کلاؤڈ سروس غیر فعال ہے: فائر بیس منسلک نہیں ہے۔ براہ کرم فائر بیس سیٹنگز کھول کر منسلک کریں۔"
            )
        }

        try {
            val localMembers = dao.getAllMembers()
            val membersCollection = fs.collection(COLLECTION_MEMBERS)
            val batch = fs.batch()
            for (member in localMembers) {
                val docRef = membersCollection.document(member.id.toString())
                val map = hashMapOf<String, Any?>(
                    "id" to member.id,
                    "name" to member.name,
                    "fatherId" to member.fatherId,
                    "fatherName" to member.fatherName,
                    "motherName" to member.motherName,
                    "gender" to member.gender.name,
                    "generation" to member.generation,
                    "spouse" to member.spouse,
                    "location" to member.location,
                    "isDeceased" to member.isDeceased,
                    "deathNote" to member.deathNote,
                    "notes" to member.notes,
                    "displayOrder" to member.displayOrder,
                    "phone" to member.phone,
                    "occupation" to member.occupation,
                    "birthYear" to member.birthYear,
                    "deathYear" to member.deathYear,
                    "lastUpdated" to System.currentTimeMillis()
                )
                batch.set(docRef, map, SetOptions.merge())
            }
            batch.commit().await()

            SyncResult(
                success = true,
                membersSynced = localMembers.size,
                message = "✅ تمام ${localMembers.size} افراد کا ڈیٹا فائر بیس کلاؤڈ پر کامیابی سے اپلوڈ اور محفوظ ہو گیا!"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Upload all members failed", e)
            SyncResult(
                success = false,
                message = "کلاؤڈ اپلوڈ میں خرابی: ${e.localizedMessage ?: e.message}"
            )
        }
    }

    /**
     * Download all members from Cloud Firestore
     */
    suspend fun downloadAllMembersFromCloud(dao: FamilyDao): SyncResult = withContext(Dispatchers.IO) {
        val fs = getFirestore()
        if (fs == null) {
            return@withContext SyncResult(
                success = false,
                message = "کلاؤڈ سروس غیر فعال ہے: فائر بیس منسلک نہیں ہے۔"
            )
        }

        try {
            val membersCollection = fs.collection(COLLECTION_MEMBERS)
            val cloudSnapshot = membersCollection.get().await()
            if (cloudSnapshot.isEmpty) {
                return@withContext SyncResult(
                    success = true,
                    membersSynced = 0,
                    message = "کلاؤڈ پر فی الوقت کوئی محفوظ شدہ ڈیٹا موجود نہیں ہے"
                )
            }

            var downloadedCount = 0
            for (doc in cloudSnapshot.documents) {
                val id = doc.getLong("id") ?: continue
                val name = doc.getString("name") ?: ""
                val fatherId = doc.getLong("fatherId")
                val fatherName = doc.getString("fatherName")
                val motherName = doc.getString("motherName")
                val genderStr = doc.getString("gender") ?: Gender.MALE.name
                val gender = try { Gender.valueOf(genderStr) } catch (e: Exception) { Gender.MALE }
                val generation = doc.getLong("generation")?.toInt() ?: 1
                val spouse = doc.getString("spouse")
                val location = doc.getString("location")
                val isDeceased = doc.getBoolean("isDeceased") ?: false
                val deathNote = doc.getString("deathNote")
                val notes = doc.getString("notes")
                val displayOrder = doc.getLong("displayOrder")?.toInt() ?: 0
                val phone = doc.getString("phone")
                val occupation = doc.getString("occupation")
                val birthYear = doc.getString("birthYear")
                val deathYear = doc.getString("deathYear")

                val member = FamilyMember(
                    id = id,
                    name = name,
                    fatherId = fatherId,
                    fatherName = fatherName,
                    motherName = motherName,
                    gender = gender,
                    generation = generation,
                    spouse = spouse,
                    location = location,
                    isDeceased = isDeceased,
                    deathNote = deathNote,
                    notes = notes,
                    displayOrder = displayOrder,
                    phone = phone,
                    occupation = occupation,
                    birthYear = birthYear,
                    deathYear = deathYear
                )
                dao.insertOrReplace(member)
                downloadedCount++
            }

            SyncResult(
                success = true,
                membersSynced = downloadedCount,
                message = "✅ کلاؤڈ سے $downloadedCount افراد کا ڈیٹا کامیابی سے ڈاؤن لوڈ ہو گیا"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Download all members failed", e)
            SyncResult(
                success = false,
                message = "کلاؤڈ ڈاؤن لوڈ میں خرابی: ${e.localizedMessage ?: e.message}"
            )
        }
    }

    suspend fun syncPendingRequests(pendingDao: PendingRequestDao): SyncResult = withContext(Dispatchers.IO) {
        val fs = getFirestore() ?: return@withContext SyncResult(false, message = "کلاؤڈ دستیاب نہیں")

        try {
            val requestsCollection = fs.collection(COLLECTION_REQUESTS)

            // 1. Fetch cloud requests
            val cloudSnapshot = requestsCollection.get().await()
            for (doc in cloudSnapshot.documents) {
                val id = doc.getLong("id") ?: continue
                val submittedByUserId = doc.getLong("submittedByUserId") ?: 0L
                val submittedByUserName = doc.getString("submittedByUserName") ?: ""
                val submittedByUserEmailOrPhone = doc.getString("submittedByUserEmailOrPhone") ?: ""
                val name = doc.getString("name") ?: ""
                val fatherId = doc.getLong("fatherId")
                val fatherName = doc.getString("fatherName")
                val motherName = doc.getString("motherName")
                val genderStr = doc.getString("gender") ?: Gender.MALE.name
                val gender = try { Gender.valueOf(genderStr) } catch (e: Exception) { Gender.MALE }
                val spouse = doc.getString("spouse")
                val location = doc.getString("location")
                val isDeceased = doc.getBoolean("isDeceased") ?: false
                val deathNote = doc.getString("deathNote")
                val notes = doc.getString("notes")
                val phone = doc.getString("phone")
                val occupation = doc.getString("occupation")
                val birthYear = doc.getString("birthYear")
                val deathYear = doc.getString("deathYear")
                val submittedAt = doc.getLong("submittedAt") ?: System.currentTimeMillis()
                val statusStr = doc.getString("status") ?: RequestStatus.PENDING.name
                val status = try { RequestStatus.valueOf(statusStr) } catch (e: Exception) { RequestStatus.PENDING }
                val reviewedAt = doc.getLong("reviewedAt")
                val rejectionReason = doc.getString("rejectionReason")

                val req = PendingMemberRequest(
                    id = id,
                    submittedByUserId = submittedByUserId,
                    submittedByUserName = submittedByUserName,
                    submittedByUserEmailOrPhone = submittedByUserEmailOrPhone,
                    name = name,
                    fatherId = fatherId,
                    fatherName = fatherName,
                    motherName = motherName,
                    gender = gender,
                    spouse = spouse,
                    location = location,
                    isDeceased = isDeceased,
                    deathNote = deathNote,
                    notes = notes,
                    phone = phone,
                    occupation = occupation,
                    birthYear = birthYear,
                    deathYear = deathYear,
                    submittedAt = submittedAt,
                    status = status,
                    reviewedAt = reviewedAt,
                    rejectionReason = rejectionReason
                )
                pendingDao.insertOrReplace(req)
            }

            // 2. Upload local requests to cloud
            val localRequests = pendingDao.getAllRequests()
            val batch = fs.batch()
            for (req in localRequests) {
                val docRef = requestsCollection.document(req.id.toString())
                val map = hashMapOf<String, Any?>(
                    "id" to req.id,
                    "submittedByUserId" to req.submittedByUserId,
                    "submittedByUserName" to req.submittedByUserName,
                    "submittedByUserEmailOrPhone" to req.submittedByUserEmailOrPhone,
                    "name" to req.name,
                    "fatherId" to req.fatherId,
                    "fatherName" to req.fatherName,
                    "motherName" to req.motherName,
                    "gender" to req.gender.name,
                    "spouse" to req.spouse,
                    "location" to req.location,
                    "isDeceased" to req.isDeceased,
                    "deathNote" to req.deathNote,
                    "notes" to req.notes,
                    "phone" to req.phone,
                    "occupation" to req.occupation,
                    "birthYear" to req.birthYear,
                    "deathYear" to req.deathYear,
                    "submittedAt" to req.submittedAt,
                    "status" to req.status.name,
                    "reviewedAt" to req.reviewedAt,
                    "rejectionReason" to req.rejectionReason
                )
                batch.set(docRef, map, SetOptions.merge())
            }
            batch.commit().await()

            SyncResult(
                success = true,
                requestsSynced = localRequests.size,
                message = "درخواستیں فائر بیس کلاؤڈ پر ہم آہنگ ہو گئیں"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Sync requests failed", e)
            SyncResult(
                success = false,
                message = "درخواستوں کی ہم آہنگی میں مسئلہ: ${e.localizedMessage ?: "کنکشن کی خرابی"}"
            )
        }
    }

    suspend fun uploadSingleMember(member: FamilyMember) = withContext(Dispatchers.IO) {
        val fs = getFirestore() ?: return@withContext
        try {
            val docRef = fs.collection(COLLECTION_MEMBERS).document(member.id.toString())
            val map = hashMapOf<String, Any?>(
                "id" to member.id,
                "name" to member.name,
                "fatherId" to member.fatherId,
                "fatherName" to member.fatherName,
                "motherName" to member.motherName,
                "gender" to member.gender.name,
                "generation" to member.generation,
                "spouse" to member.spouse,
                "location" to member.location,
                "isDeceased" to member.isDeceased,
                "deathNote" to member.deathNote,
                "notes" to member.notes,
                "displayOrder" to member.displayOrder,
                "phone" to member.phone,
                "occupation" to member.occupation,
                "birthYear" to member.birthYear,
                "deathYear" to member.deathYear,
                "lastUpdated" to System.currentTimeMillis()
            )
            withTimeoutOrNull(2500L) {
                docRef.set(map, SetOptions.merge()).await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to upload member to firestore: ${e.message}")
        }
    }

    suspend fun uploadSingleRequest(req: PendingMemberRequest) = withContext(Dispatchers.IO) {
        val fs = getFirestore() ?: return@withContext
        try {
            val docRef = fs.collection(COLLECTION_REQUESTS).document(req.id.toString())
            val map = hashMapOf<String, Any?>(
                "id" to req.id,
                "submittedByUserId" to req.submittedByUserId,
                "submittedByUserName" to req.submittedByUserName,
                "submittedByUserEmailOrPhone" to req.submittedByUserEmailOrPhone,
                "name" to req.name,
                "fatherId" to req.fatherId,
                "fatherName" to req.fatherName,
                "motherName" to req.motherName,
                "gender" to req.gender.name,
                "spouse" to req.spouse,
                "location" to req.location,
                "isDeceased" to req.isDeceased,
                "deathNote" to req.deathNote,
                "notes" to req.notes,
                "phone" to req.phone,
                "occupation" to req.occupation,
                "birthYear" to req.birthYear,
                "deathYear" to req.deathYear,
                "submittedAt" to req.submittedAt,
                "status" to req.status.name,
                "reviewedAt" to req.reviewedAt,
                "rejectionReason" to req.rejectionReason
            )
            withTimeoutOrNull(2500L) {
                docRef.set(map, SetOptions.merge()).await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to upload request to firestore: ${e.message}")
        }
    }

    suspend fun deletePendingRequestFromCloud(requestId: Long) = withContext(Dispatchers.IO) {
        val fs = getFirestore() ?: return@withContext
        try {
            withTimeoutOrNull(2500L) {
                fs.collection(COLLECTION_REQUESTS).document(requestId.toString()).delete().await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete request from firestore: ${e.message}")
        }
    }

    suspend fun fetchPendingRequestsFromCloud(): List<PendingMemberRequest> = withContext(Dispatchers.IO) {
        val fs = getFirestore() ?: return@withContext emptyList()
        try {
            val snapshot = fs.collection(COLLECTION_REQUESTS)
                .whereEqualTo("status", RequestStatus.PENDING.name)
                .get()
                .await()
            val list = mutableListOf<PendingMemberRequest>()
            for (doc in snapshot.documents) {
                val id = doc.getLong("id") ?: continue
                val submittedByUserId = doc.getLong("submittedByUserId") ?: 0L
                val submittedByUserName = doc.getString("submittedByUserName") ?: ""
                val submittedByUserEmailOrPhone = doc.getString("submittedByUserEmailOrPhone") ?: ""
                val name = doc.getString("name") ?: ""
                val fatherId = doc.getLong("fatherId")
                val fatherName = doc.getString("fatherName")
                val motherName = doc.getString("motherName")
                val genderStr = doc.getString("gender") ?: Gender.MALE.name
                val gender = try { Gender.valueOf(genderStr) } catch (e: Exception) { Gender.MALE }
                val spouse = doc.getString("spouse")
                val location = doc.getString("location")
                val isDeceased = doc.getBoolean("isDeceased") ?: false
                val deathNote = doc.getString("deathNote")
                val notes = doc.getString("notes")
                val phone = doc.getString("phone")
                val occupation = doc.getString("occupation")
                val birthYear = doc.getString("birthYear")
                val deathYear = doc.getString("deathYear")
                val submittedAt = doc.getLong("submittedAt") ?: System.currentTimeMillis()
                val statusStr = doc.getString("status") ?: RequestStatus.PENDING.name
                val status = try { RequestStatus.valueOf(statusStr) } catch (e: Exception) { RequestStatus.PENDING }
                val reviewedAt = doc.getLong("reviewedAt")
                val rejectionReason = doc.getString("rejectionReason")

                list.add(
                    PendingMemberRequest(
                        id = id,
                        submittedByUserId = submittedByUserId,
                        submittedByUserName = submittedByUserName,
                        submittedByUserEmailOrPhone = submittedByUserEmailOrPhone,
                        name = name,
                        fatherId = fatherId,
                        fatherName = fatherName,
                        motherName = motherName,
                        gender = gender,
                        spouse = spouse,
                        location = location,
                        isDeceased = isDeceased,
                        deathNote = deathNote,
                        notes = notes,
                        phone = phone,
                        occupation = occupation,
                        birthYear = birthYear,
                        deathYear = deathYear,
                        submittedAt = submittedAt,
                        status = status,
                        reviewedAt = reviewedAt,
                        rejectionReason = rejectionReason
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching pending requests from cloud: ${e.message}")
            emptyList()
        }
    }

    suspend fun updateRequestStatusInCloud(requestId: Long, status: RequestStatus, rejectionReason: String? = null) = withContext(Dispatchers.IO) {
        val fs = getFirestore() ?: return@withContext
        try {
            val docRef = fs.collection(COLLECTION_REQUESTS).document(requestId.toString())
            val updates = hashMapOf<String, Any?>(
                "status" to status.name,
                "reviewedAt" to System.currentTimeMillis(),
                "rejectionReason" to rejectionReason
            )
            docRef.set(updates, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update request status in cloud: ${e.message}")
        }
    }

    suspend fun deleteMemberFromCloud(memberId: Long) = withContext(Dispatchers.IO) {
        val fs = getFirestore() ?: return@withContext
        try {
            withTimeoutOrNull(2500L) {
                fs.collection(COLLECTION_MEMBERS).document(memberId.toString()).delete().await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete member from firestore: ${e.message}")
        }
    }

    suspend fun saveUserToFirestore(
        uid: String,
        email: String,
        fullName: String,
        status: String = "pending",
        role: String = "MEMBER",
        linkedMemberId: Long? = null,
        linkedMemberName: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val fs = getFirestore() ?: return@withContext Result.failure(IllegalStateException("کلاؤڈ سروس دستیاب نہیں"))
        try {
            val docRef = fs.collection(COLLECTION_USERS).document(uid)
            val userMap = hashMapOf<String, Any?>(
                "uid" to uid,
                "email" to email,
                "fullName" to fullName,
                "status" to status,
                "role" to role,
                "linkedMemberId" to linkedMemberId,
                "linkedMemberName" to linkedMemberName,
                "createdAt" to System.currentTimeMillis(),
                "lastUpdated" to System.currentTimeMillis()
            )
            docRef.set(userMap, SetOptions.merge()).await()
            Log.d(TAG, "User saved to Firestore with status '$status': $email ($uid)")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchUserFromFirestore(uid: String): Map<String, Any?>? = withContext(Dispatchers.IO) {
        val fs = getFirestore() ?: return@withContext null
        try {
            val doc = fs.collection(COLLECTION_USERS).document(uid).get().await()
            if (doc.exists()) {
                doc.data
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch user from Firestore: ${e.message}")
            null
        }
    }

    suspend fun updateUserStatusInFirestore(uid: String, newStatus: String): Result<Unit> = withContext(Dispatchers.IO) {
        val fs = getFirestore() ?: return@withContext Result.failure(IllegalStateException("کلاؤڈ سروس دستیاب نہیں"))
        try {
            val docRef = fs.collection(COLLECTION_USERS).document(uid)
            val updates = hashMapOf<String, Any?>(
                "status" to newStatus,
                "lastUpdated" to System.currentTimeMillis()
            )
            docRef.set(updates, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update user status in Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateUserDisplayNameInFirestore(uid: String, fullName: String): Result<Unit> = withContext(Dispatchers.IO) {
        val fs = getFirestore() ?: return@withContext Result.failure(IllegalStateException("کلاؤڈ سروس دستیاب نہیں"))
        try {
            val docRef = fs.collection(COLLECTION_USERS).document(uid)
            val updates = hashMapOf<String, Any?>(
                "fullName" to fullName,
                "lastUpdated" to System.currentTimeMillis()
            )
            withTimeoutOrNull(2500L) {
                docRef.set(updates, SetOptions.merge()).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update user display name in Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateUserRoleInFirestore(uid: String, role: String): Result<Unit> = withContext(Dispatchers.IO) {
        val fs = getFirestore() ?: return@withContext Result.failure(IllegalStateException("کلاؤڈ سروس دستیاب نہیں"))
        try {
            val docRef = fs.collection(COLLECTION_USERS).document(uid)
            val updates = hashMapOf<String, Any?>(
                "role" to role,
                "lastUpdated" to System.currentTimeMillis()
            )
            withTimeoutOrNull(2500L) {
                docRef.set(updates, SetOptions.merge()).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update user role in Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun syncUsers(userDao: UserDao, context: Context? = null): SyncResult = withContext(Dispatchers.IO) {
        val fs = getFirestore() ?: return@withContext SyncResult(false, message = "کلاؤڈ سروس دستیاب نہیں")
        try {
            val col = fs.collection(COLLECTION_USERS)
            val snapshot = try {
                col.get(Source.DEFAULT).await()
            } catch (e: Exception) {
                try { col.get(Source.CACHE).await() } catch (ce: Exception) { null }
            }
            if (snapshot != null) {
                var syncedCount = 0
                var hasNewPending = false
                var pendingUserName = ""
                var pendingUserEmail = ""
                for (doc in snapshot.documents) {
                    val uid = doc.getString("uid") ?: doc.id
                    val email = doc.getString("email") ?: continue
                    val fullName = doc.getString("fullName") ?: "صارف"
                    val status = doc.getString("status") ?: "pending"
                    val role = doc.getString("role") ?: "MEMBER"
                    val linkedId = doc.getLong("linkedMemberId")
                    val linkedName = doc.getString("linkedMemberName")

                    val local = userDao.getUserByEmailOrPhone(email)
                    if (local == null) {
                        userDao.insertUser(
                            UserAccount(
                                firebaseUid = uid,
                                fullName = fullName,
                                emailOrPhone = email,
                                passwordHash = "",
                                role = role,
                                status = status,
                                linkedMemberId = linkedId,
                                linkedMemberName = linkedName
                            )
                        )
                        syncedCount++
                        if (status == "pending") {
                            hasNewPending = true
                            pendingUserName = fullName
                            pendingUserEmail = email
                        }
                    } else {
                        if (local.status != status || local.role != role || local.fullName != fullName) {
                            userDao.updateUser(
                                local.copy(
                                    status = status,
                                    role = role,
                                    fullName = fullName,
                                    linkedMemberId = linkedId ?: local.linkedMemberId,
                                    linkedMemberName = linkedName ?: local.linkedMemberName
                                )
                            )
                            syncedCount++
                        }
                    }
                }
                if (hasNewPending && context != null) {
                    com.example.util.AnnouncementNotificationHelper.showNewUserPendingApprovalNotification(
                        context,
                        pendingUserName,
                        pendingUserEmail
                    )
                }
                SyncResult(true, membersSynced = syncedCount, message = "$syncedCount صارفین کلاؤڈ سے ہم آہنگ ہو گئے")
            } else {
                SyncResult(false, message = "صارفین کا ڈیٹا حاصل نہ ہو سکا")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing users: ${e.message}")
            SyncResult(false, message = "صارفین کی سنکرونائزیشن میں خرابی: ${e.message}")
        }
    }

    suspend fun syncAnnouncements(dao: AnnouncementDao, context: Context? = null): SyncResult = withContext(Dispatchers.IO) {
        val fs = getFirestore() ?: return@withContext SyncResult(false, message = "کلاؤڈ سروس دستیاب نہیں")
        try {
            val col = fs.collection(COLLECTION_ANNOUNCEMENTS)
            val snapshot = try {
                col.get(Source.DEFAULT).await()
            } catch (e: Exception) {
                try { col.get(Source.CACHE).await() } catch (ce: Exception) { null }
            }
            val existingIds = dao.getAllAnnouncements().map { it.id }.toSet()
            var count = 0
            if (snapshot != null && !snapshot.isEmpty) {
                for (doc in snapshot.documents) {
                    val id = doc.getLong("id") ?: continue
                    val title = doc.getString("title") ?: ""
                    val content = doc.getString("content") ?: ""
                    val authorName = doc.getString("authorName") ?: ""
                    val authorEmailOrPhone = doc.getString("authorEmailOrPhone") ?: ""
                    val authorRole = doc.getString("authorRole") ?: "ADMIN"
                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    val isPinned = doc.getBoolean("isPinned") ?: false
                    val updatedAt = doc.getLong("updatedAt") ?: timestamp

                    val announcement = Announcement(
                        id = id,
                        firestoreId = doc.id,
                        title = title,
                        content = content,
                        authorName = authorName,
                        authorEmailOrPhone = authorEmailOrPhone,
                        authorRole = authorRole,
                        timestamp = timestamp,
                        isPinned = isPinned,
                        updatedAt = updatedAt
                    )
                    dao.insert(announcement)
                    count++

                    if (!existingIds.contains(id) && context != null && existingIds.isNotEmpty()) {
                        AnnouncementNotificationHelper.showAnnouncementNotification(
                            context,
                            title = title,
                            content = content
                        )
                    }
                }
            }

            // Sync local announcements up to Firestore
            val localItems = dao.getAllAnnouncements()
            for (item in localItems) {
                uploadSingleAnnouncement(item)
            }

            SyncResult(true, membersSynced = count, message = "اعلانات ہم آہنگ ہو گئے ہیں ($count)")
        } catch (e: Exception) {
            Log.w(TAG, "syncAnnouncements error: ${e.message}")
            SyncResult(false, message = e.message ?: "اعلانات کی ہم آہنگی میں مسئلہ")
        }
    }

    suspend fun uploadSingleAnnouncement(announcement: Announcement) = withContext(Dispatchers.IO) {
        val fs = getFirestore() ?: return@withContext
        try {
            val docRef = fs.collection(COLLECTION_ANNOUNCEMENTS).document(announcement.id.toString())
            val map = hashMapOf<String, Any?>(
                "id" to announcement.id,
                "title" to announcement.title,
                "content" to announcement.content,
                "authorName" to announcement.authorName,
                "authorEmailOrPhone" to announcement.authorEmailOrPhone,
                "authorRole" to announcement.authorRole,
                "timestamp" to announcement.timestamp,
                "isPinned" to announcement.isPinned,
                "updatedAt" to announcement.updatedAt
            )
            withTimeoutOrNull(2500L) {
                docRef.set(map, SetOptions.merge()).await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "uploadSingleAnnouncement failed: ${e.message}")
        }
    }

    suspend fun deleteAnnouncementFromCloud(announcementId: Long) = withContext(Dispatchers.IO) {
        val fs = getFirestore() ?: return@withContext
        try {
            withTimeoutOrNull(2500L) {
                fs.collection(COLLECTION_ANNOUNCEMENTS).document(announcementId.toString()).delete().await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "deleteAnnouncementFromCloud failed: ${e.message}")
        }
    }

    /**
     * Directly retrieve cached family tree members from Firestore offline persistence
     */
    suspend fun getCachedMembersFromFirestore(): List<FamilyMember> = withContext(Dispatchers.IO) {
        val fs = getFirestore() ?: return@withContext emptyList()
        try {
            val snapshot = fs.collection(COLLECTION_MEMBERS).get(Source.CACHE).await()
            val list = mutableListOf<FamilyMember>()
            for (doc in snapshot.documents) {
                val id = doc.getLong("id") ?: continue
                val name = doc.getString("name") ?: ""
                val fatherId = doc.getLong("fatherId")
                val fatherName = doc.getString("fatherName")
                val motherName = doc.getString("motherName")
                val genderStr = doc.getString("gender") ?: Gender.MALE.name
                val gender = try { Gender.valueOf(genderStr) } catch (e: Exception) { Gender.MALE }
                val generation = doc.getLong("generation")?.toInt() ?: 1
                val spouse = doc.getString("spouse")
                val location = doc.getString("location")
                val isDeceased = doc.getBoolean("isDeceased") ?: false
                val deathNote = doc.getString("deathNote")
                val notes = doc.getString("notes")
                val displayOrder = doc.getLong("displayOrder")?.toInt() ?: 0
                val phone = doc.getString("phone")
                val occupation = doc.getString("occupation")
                val birthYear = doc.getString("birthYear")
                val deathYear = doc.getString("deathYear")

                list.add(
                    FamilyMember(
                        id = id,
                        name = name,
                        fatherId = fatherId,
                        fatherName = fatherName,
                        motherName = motherName,
                        gender = gender,
                        generation = generation,
                        spouse = spouse,
                        location = location,
                        isDeceased = isDeceased,
                        deathNote = deathNote,
                        notes = notes,
                        displayOrder = displayOrder,
                        phone = phone,
                        occupation = occupation,
                        birthYear = birthYear,
                        deathYear = deathYear
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching from Firestore offline cache: ${e.message}")
            emptyList()
        }
    }

    companion object {
        private const val TAG = "FirestoreSyncManager"
        private const val COLLECTION_MEMBERS = "shajra_family_members"
        private const val COLLECTION_REQUESTS = "shajra_pending_requests"
        private const val COLLECTION_USERS = "shajra_users"
        private const val COLLECTION_ANNOUNCEMENTS = "shajra_announcements"

        @Volatile
        private var INSTANCE: FirestoreSyncManager? = null

        fun getInstance(context: Context? = null): FirestoreSyncManager {
            return INSTANCE ?: synchronized(this) {
                val instance = FirestoreSyncManager()
                if (context != null) {
                    instance.setApplicationContext(context)
                }
                INSTANCE = instance
                instance
            }
        }
    }
}
