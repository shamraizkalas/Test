package com.example.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.FirebaseAuthService
import com.example.data.Announcement
import com.example.data.AppDatabase
import com.example.data.FamilyMember
import com.example.data.FamilyRepository
import com.example.data.FamilyStatistics
import com.example.data.FirebaseConfigData
import com.example.data.FirebaseManager
import com.example.data.FirestoreSyncManager
import com.example.data.Gender
import com.example.data.PendingMemberRequest
import com.example.data.RequestStatus
import com.example.data.UserAccount
import com.example.util.AnnouncementNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import android.util.Log

enum class ViewMode {
    TREE,
    DIRECTORY,
    STATS
}

enum class FilterGender {
    ALL,
    MALES,
    FEMALES
}

@OptIn(ExperimentalCoroutinesApi::class)
class FamilyTreeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FamilyRepository
    private val firebaseAuthService = FirebaseAuthService()
    private val prefs = application.getSharedPreferences("shajra_prefs", Context.MODE_PRIVATE)
    private val userDao = AppDatabase.getDatabase(application).userDao()

    val allMembers: StateFlow<List<FamilyMember>>

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedBranchId = MutableStateFlow<Long?>(null)
    val selectedBranchId: StateFlow<Long?> = _selectedBranchId.asStateFlow()

    private val _genderFilter = MutableStateFlow(FilterGender.ALL)
    val genderFilter: StateFlow<FilterGender> = _genderFilter.asStateFlow()

    private val _generationFilter = MutableStateFlow<Int?>(null)
    val generationFilter: StateFlow<Int?> = _generationFilter.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.TREE)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _selectedMemberForDetail = MutableStateFlow<FamilyMember?>(null)
    val selectedMemberForDetail: StateFlow<FamilyMember?> = _selectedMemberForDetail.asStateFlow()

    // Relationship Calculator State
    private val _showRelationshipDialog = MutableStateFlow(false)
    val showRelationshipDialog: StateFlow<Boolean> = _showRelationshipDialog.asStateFlow()

    private val _personAForRelation = MutableStateFlow<FamilyMember?>(null)
    val personAForRelation: StateFlow<FamilyMember?> = _personAForRelation.asStateFlow()

    private val _personBForRelation = MutableStateFlow<FamilyMember?>(null)
    val personBForRelation: StateFlow<FamilyMember?> = _personBForRelation.asStateFlow()

    private val _relationshipResult = MutableStateFlow<com.example.data.RelationshipResult?>(null)
    val relationshipResult: StateFlow<com.example.data.RelationshipResult?> = _relationshipResult.asStateFlow()

    // Backup & Restore Dialog State
    private val _showBackupRestoreDialog = MutableStateFlow(false)
    val showBackupRestoreDialog: StateFlow<Boolean> = _showBackupRestoreDialog.asStateFlow()

    // Export PDF Dialog State
    private val _showExportPdfDialog = MutableStateFlow(false)
    val showExportPdfDialog: StateFlow<Boolean> = _showExportPdfDialog.asStateFlow()

    // Announcements Notice Board Dialog State
    private val _showAnnouncementsDialog = MutableStateFlow(false)
    val showAnnouncementsDialog: StateFlow<Boolean> = _showAnnouncementsDialog.asStateFlow()

    // Super Admin User Management Dialog State
    private val _showUserManagementDialog = MutableStateFlow(false)
    val showUserManagementDialog: StateFlow<Boolean> = _showUserManagementDialog.asStateFlow()
    private val _targetUserApprovalQuery = MutableStateFlow<String?>("")
    val targetUserApprovalQuery: StateFlow<String?> = _targetUserApprovalQuery.asStateFlow()

    // Auto-focus / Scroll to newly added member
    private val _scrollToMemberId = MutableStateFlow<Long?>(null)
    val scrollToMemberId: StateFlow<Long?> = _scrollToMemberId.asStateFlow()

    // Cloud Sync Configuration Dialog State
    private val _showCloudConfigDialog = MutableStateFlow(false)
    val showCloudConfigDialog: StateFlow<Boolean> = _showCloudConfigDialog.asStateFlow()

    // User Authentication States
    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

    private val _showUserProfileDialog = MutableStateFlow(false)
    val showUserProfileDialog: StateFlow<Boolean> = _showUserProfileDialog.asStateFlow()

    private val _authInitialTab = MutableStateFlow(0)
    val authInitialTab: StateFlow<Int> = _authInitialTab.asStateFlow()

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _showPinDialog = MutableStateFlow(false)
    val showPinDialog: StateFlow<Boolean> = _showPinDialog.asStateFlow()

    private val _showAddMemberDialog = MutableStateFlow(false)
    val showAddMemberDialog: StateFlow<Boolean> = _showAddMemberDialog.asStateFlow()
    val addMemberInitialParentId = MutableStateFlow<Long?>(null)

    private val _memberToEdit = MutableStateFlow<FamilyMember?>(null)
    val memberToEdit: StateFlow<FamilyMember?> = _memberToEdit.asStateFlow()

    private val _memberToDelete = MutableStateFlow<FamilyMember?>(null)
    val memberToDelete: StateFlow<FamilyMember?> = _memberToDelete.asStateFlow()

    private val _showResetConfirmDialog = MutableStateFlow(false)
    val showResetConfirmDialog: StateFlow<Boolean> = _showResetConfirmDialog.asStateFlow()

    private val _showChangePinDialog = MutableStateFlow(false)
    val showChangePinDialog: StateFlow<Boolean> = _showChangePinDialog.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Pending Approval & Submission States
    val allPendingRequests: StateFlow<List<PendingMemberRequest>>
    val activePendingRequests: StateFlow<List<PendingMemberRequest>>
    val pendingCount: StateFlow<Int>
    val mySubmittedRequests: StateFlow<List<PendingMemberRequest>>
    val allUsers: StateFlow<List<UserAccount>>
    val allAnnouncements: StateFlow<List<Announcement>>

    private val _showApprovalsDialog = MutableStateFlow(false)
    val showApprovalsDialog: StateFlow<Boolean> = _showApprovalsDialog.asStateFlow()

    private val _showMySubmissionsDialog = MutableStateFlow(false)
    val showMySubmissionsDialog: StateFlow<Boolean> = _showMySubmissionsDialog.asStateFlow()

    // Firestore Cloud Sync States
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncStatus = MutableStateFlow(CloudSyncStatus.SYNCED)
    val syncStatus: StateFlow<CloudSyncStatus> = _syncStatus.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    // Theme Mode (Light / Dark) for enhanced readability & accessibility
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        val newMode = !_isDarkMode.value
        _isDarkMode.value = newMode
        prefs.edit().putBoolean("is_dark_mode", newMode).apply()
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs.edit().putBoolean("is_dark_mode", enabled).apply()
    }

    // Filtered members for directory view or search
    val filteredMembers: StateFlow<List<FamilyMember>>

    // Statistics
    private val _statistics = MutableStateFlow<FamilyStatistics?>(null)
    val statistics: StateFlow<FamilyStatistics?> = _statistics.asStateFlow()

    // Quick map for quick parent/lineage lookups
    val membersMap: StateFlow<Map<Long, FamilyMember>>

    init {
        FirestoreSyncManager.getInstance(application)
        FirebaseManager.initialize(application)

        val database = AppDatabase.getDatabase(application)
        repository = FamilyRepository(
            dao = database.familyDao(),
            userDao = database.userDao(),
            pendingRequestDao = database.pendingRequestDao(),
            announcementDao = database.announcementDao()
        )

        allMembers = repository.allMembersFlow.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

        allPendingRequests = repository.allPendingRequestsFlow.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

        activePendingRequests = repository.activePendingRequestsFlow.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

        pendingCount = repository.pendingCountFlow.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            0
        )

        mySubmittedRequests = _currentUser.flatMapLatest { user ->
            if (user != null) {
                repository.getRequestsByUserFlow(user.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        allUsers = repository.allUsersFlow.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

        allAnnouncements = repository.allAnnouncementsFlow.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

        membersMap = allMembers.combine(MutableStateFlow(Unit)) { members, _ ->
            members.associateBy { it.id }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

        filteredMembers = combine(
            allMembers,
            _searchQuery,
            _selectedBranchId,
            _genderFilter,
            _generationFilter
        ) { members, query, branchId, gender, genFilter ->
            var list = members

            // Branch filter
            if (branchId != null) {
                val descendantIds = getDescendantIdsIncludingSelf(branchId, members)
                list = list.filter { it.id in descendantIds }
            }

            // Gender filter
            when (gender) {
                FilterGender.MALES -> list = list.filter { it.isMale }
                FilterGender.FEMALES -> list = list.filter { it.isFemale }
                FilterGender.ALL -> {}
            }

            // Generation filter
            if (genFilter != null) {
                list = list.filter { it.generation == genFilter }
            }

            // Search query filter (Live Search by name and father's name)
            if (query.isNotBlank()) {
                fun norm(text: String) = text.lowercase()
                    .replace('آ', 'ا').replace('أ', 'ا').replace('إ', 'ا').replace('ٱ', 'ا')
                    .replace('ي', 'ی').replace('ى', 'ی').replace('ة', 'ہ').replace('ك', 'ک').trim()
                val q = norm(query)
                list = list.filter { member ->
                    norm(member.name).contains(q) ||
                            (member.fatherName?.let { norm(it).contains(q) } == true) ||
                            (member.spouse?.let { norm(it).contains(q) } == true) ||
                            (member.location?.let { norm(it).contains(q) } == true) ||
                            (member.notes?.let { norm(it).contains(q) } == true) ||
                            (member.deathNote?.let { norm(it).contains(q) } == true) ||
                            (member.phone?.let { norm(it).contains(q) } == true) ||
                            (member.occupation?.let { norm(it).contains(q) } == true)
                }
            }

            list
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

        // Initialize database if empty and calculate initial stats
        val savedPin = prefs.getString("admin_pin", null)
        if (savedPin == null || savedPin == "1234") {
            prefs.edit().putString("admin_pin", DEFAULT_ADMIN_PIN).apply()
        }

        viewModelScope.launch {
            repository.checkAndInitializeDatabase()
            val savedUserId = prefs.getLong("logged_in_user_id", -1L)
            val isAdminSaved = prefs.getBoolean("is_admin_session_active", false)
            if (isAdminSaved) {
                _isAdminLoggedIn.value = true
            }
            if (savedUserId != -1L) {
                val user = repository.getUserById(savedUserId)
                if (user != null) {
                    val updatedUser = if (user.fullName.contains("محمد شمریز")) {
                        val fixed = user.copy(
                            fullName = if (user.isAdmin) "شمریز ایوب (ایڈمن)" else "شمریز ایوب",
                            linkedMemberName = "شمریز ایوب"
                        )
                        repository.updateUser(fixed)
                        fixed
                    } else user
                    _currentUser.value = updatedUser
                    if (updatedUser.isAdmin) {
                        _isAdminLoggedIn.value = true
                    }
                }
            } else if (isAdminSaved) {
                val adminUser = repository.getUserByEmailOrPhone("admin")
                if (adminUser != null) {
                    _currentUser.value = adminUser
                }
            }
            // Auto sync online data in background
            try {
                repository.syncWithFirestore(getApplication())
            } catch (e: Exception) {
                // Offline fallback
            }
        }

        viewModelScope.launch {
            allMembers.collect { members ->
                if (members.isNotEmpty()) {
                    _statistics.value = repository.getStatistics(members)
                }
            }
        }
    }

    private fun getDescendantIdsIncludingSelf(rootId: Long, all: List<FamilyMember>): Set<Long> {
        val result = mutableSetOf(rootId)
        val queue = ArrayDeque<Long>()
        queue.add(rootId)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val children = all.filter { it.fatherId == current }
            for (child in children) {
                if (result.add(child.id)) {
                    queue.add(child.id)
                }
            }
        }
        return result
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank() && _viewMode.value == ViewMode.TREE) {
            // Auto switch to directory/search list when searching for ease of scanning
            _viewMode.value = ViewMode.DIRECTORY
        }
    }

    fun setBranchFilter(branchId: Long?) {
        _selectedBranchId.value = branchId
    }

    fun setGenderFilter(filter: FilterGender) {
        _genderFilter.value = filter
    }

    fun setGenerationFilter(gen: Int?) {
        _generationFilter.value = gen
    }

    fun clearAllFilters() {
        _searchQuery.value = ""
        _genderFilter.value = FilterGender.ALL
        _generationFilter.value = null
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun selectMemberForDetail(member: FamilyMember?) {
        _selectedMemberForDetail.value = member
    }

    // Relationship Calculator
    fun openRelationshipDialog(initialA: FamilyMember? = null, initialB: FamilyMember? = null) {
        _personAForRelation.value = initialA ?: allMembers.value.firstOrNull()
        _personBForRelation.value = initialB ?: allMembers.value.getOrNull(1)
        recalculateRelationship()
        _showRelationshipDialog.value = true
    }

    fun setPersonAForRelation(member: FamilyMember?) {
        _personAForRelation.value = member
        recalculateRelationship()
    }

    fun setPersonBForRelation(member: FamilyMember?) {
        _personBForRelation.value = member
        recalculateRelationship()
    }

    private fun recalculateRelationship() {
        val a = _personAForRelation.value
        val b = _personBForRelation.value
        if (a != null && b != null) {
            _relationshipResult.value = repository.calculateRelationship(a, b, membersMap.value)
        } else {
            _relationshipResult.value = null
        }
    }

    fun dismissRelationshipDialog() {
        _showRelationshipDialog.value = false
    }

    // Backup & Restore (Restricted to Admin Only)
    fun openBackupRestoreDialog() {
        val isAdmin = _isAdminLoggedIn.value || (_currentUser.value?.isAdmin == true)
        if (isAdmin) {
            _showBackupRestoreDialog.value = true
        } else {
            _snackbarMessage.value = "صرف ایڈمن کے پاس بیک اپ اور JSON فائل تک رسائی کی اجازت ہے۔"
        }
    }

    fun dismissBackupRestoreDialog() {
        _showBackupRestoreDialog.value = false
    }

    // Export to PDF
    fun openExportPdfDialog() {
        if (_currentUser.value == null) {
            _snackbarMessage.value = "PDF بنانے کے لیے لاگ ان کریں"
            _authInitialTab.value = 0
            _showAuthDialog.value = true
            return
        }
        _showExportPdfDialog.value = true
    }

    fun dismissExportPdfDialog() {
        _showExportPdfDialog.value = false
    }

    fun exportJson(): String {
        val isAdmin = _isAdminLoggedIn.value || (_currentUser.value?.isAdmin == true)
        if (!isAdmin) {
            return "{}"
        }
        return repository.exportToJson(allMembers.value)
    }

    fun exportReadableTree(): String {
        return repository.exportReadableTextTree(allMembers.value)
    }

    fun importBackup(jsonString: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.importFromJson(jsonString)
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                _snackbarMessage.value = "کامیابی: $count افراد کا بیک اپ بحال کر دیا گیا"
                onComplete(true, "کامیابی: $count افراد کا ڈیٹا بحال ہو گیا")
            } else {
                val errorMsg = result.exceptionOrNull()?.localizedMessage ?: "خرابی پیش آگئی"
                onComplete(false, "بیک اپ بحال کرنے میں خرابی: $errorMsg")
            }
        }
    }

    fun getFullLineageChain(member: FamilyMember): String {
        return repository.getFullLineageChainString(member, membersMap.value)
    }

    // Authentication Methods
    fun openAuthDialog(tab: Int = 0) {
        _authInitialTab.value = tab
        _showAuthDialog.value = true
    }

    fun dismissAuthDialog() {
        _showAuthDialog.value = false
    }

    fun openUserProfileDialog() {
        _showUserProfileDialog.value = true
    }

    fun dismissUserProfileDialog() {
        _showUserProfileDialog.value = false
    }

    fun loginUser(
        identifier: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val trimmedId = identifier.trim()
        val trimmedPass = password.trim()

        if (trimmedId.isBlank() || trimmedPass.isBlank()) {
            val err = "براہ کرم تمام خانے پُر کریں"
            _snackbarMessage.value = "❌ $err"
            onResult(false, err)
            return
        }

        viewModelScope.launch {
            // 1. Check user accounts in local database first for instant login
            val localUser = repository.getUserByEmailOrPhone(trimmedId)
            if (localUser != null && localUser.passwordHash == trimmedPass) {
                _currentUser.value = localUser
                if (localUser.isAdmin) {
                    _isAdminLoggedIn.value = true
                }
                prefs.edit().putLong("logged_in_user_id", localUser.id).apply()
                _showAuthDialog.value = false
                val statusNotice = if (localUser.isPending) " (اکاؤنٹ زیرِ التواء ہے)" else " (فعال)"
                _snackbarMessage.value = "✅ خوش آمدید، ${localUser.fullName}!$statusNotice"
                onResult(true, "کامیابی سے لاگ ان ہو گئے")
                // Auto sync online data in background on login
                syncWithCloud()
                return@launch
            }

            // 2. Also check if admin master credentials entered
            val savedPin = prefs.getString("admin_pin", DEFAULT_ADMIN_PIN) ?: DEFAULT_ADMIN_PIN
            val isAdminIdentifier = trimmedId.equals("admin", ignoreCase = true) ||
                    trimmedId == "1234" ||
                    trimmedId.equals("شمریز", ignoreCase = true) ||
                    trimmedId.equals("شمریز ایوب", ignoreCase = true) ||
                    trimmedId.equals("shamraizkalas@gmail.com", ignoreCase = true)

            if (isAdminIdentifier && trimmedPass == savedPin) {
                var adminUser = repository.getUserByEmailOrPhone("admin")
                if (adminUser == null) {
                    val newAdmin = UserAccount(
                        fullName = "شمریز ایوب (ایڈمن)",
                        emailOrPhone = "admin",
                        passwordHash = savedPin,
                        role = "ADMIN",
                        status = "approved",
                        linkedMemberName = "شمریز ایوب"
                    )
                    val id = repository.registerUser(newAdmin)
                    adminUser = newAdmin.copy(id = id)
                } else if (adminUser.fullName.contains("محمد شمریز")) {
                    adminUser = adminUser.copy(fullName = "شمریز ایوب (ایڈمن)", linkedMemberName = "شمریز ایوب")
                    repository.updateUser(adminUser)
                }
                _currentUser.value = adminUser
                _isAdminLoggedIn.value = true
                prefs.edit().putLong("logged_in_user_id", adminUser.id).apply()
                _showAuthDialog.value = false
                _snackbarMessage.value = "✅ ایڈمن لاگ ان کامیاب! خوش آمدید"
                onResult(true, "ایڈمن کے طور پر لاگ ان ہو گئے")
                return@launch
            }

            // 3. Attempt Firebase Auth sign in if identifier is an email (with short timeout)
            if (trimmedId.contains("@") && firebaseAuthService.isAvailable()) {
                val fbResult = withTimeoutOrNull(3000L) {
                    firebaseAuthService.signInWithEmail(trimmedId, trimmedPass)
                }
                if (fbResult != null && fbResult.isSuccess) {
                    val fbUser = fbResult.getOrNull()
                    var user = repository.getUserByEmailOrPhone(trimmedId)
                    val uid = fbUser?.uid ?: "fb_${System.currentTimeMillis()}"

                    // Check if user exists in Firestore to sync status
                    val firestoreData = withTimeoutOrNull(2000L) {
                        FirestoreSyncManager.getInstance().fetchUserFromFirestore(uid)
                    }
                    val firestoreStatus = (firestoreData?.get("status") as? String) ?: "pending"

                    if (user == null) {
                        val isAdmin = trimmedId.equals("shamraizkalas@gmail.com", ignoreCase = true)
                        val newUser = UserAccount(
                            firebaseUid = uid,
                            fullName = fbUser?.displayName ?: trimmedId.substringBefore("@"),
                            emailOrPhone = trimmedId,
                            passwordHash = trimmedPass,
                            role = if (isAdmin) "ADMIN" else "MEMBER",
                            status = if (isAdmin) "approved" else firestoreStatus
                        )
                        val newId = repository.registerUser(newUser)
                        user = newUser.copy(id = newId)
                    } else {
                        // Update status from cloud if changed
                        if (user.status != firestoreStatus && !user.isAdmin) {
                            val updatedUser = user.copy(status = firestoreStatus, firebaseUid = uid)
                            repository.updateUser(updatedUser)
                            user = updatedUser
                        }
                    }
                    _currentUser.value = user
                    if (user.isAdmin) {
                        _isAdminLoggedIn.value = true
                    }
                    prefs.edit().putLong("logged_in_user_id", user.id).apply()
                    _showAuthDialog.value = false
                    val statusNotice = if (user.isPending) " (اکاؤنٹ زیرِ التواء ہے)" else " (فعال)"
                    _snackbarMessage.value = "✅ خوش آمدید، ${user.fullName}!$statusNotice"
                    onResult(true, "Firebase کے ذریعے کامیابی سے لاگ ان ہو گئے")
                    // Auto sync online data in background on login
                    syncWithCloud()
                    return@launch
                }
            }

            val failMsg = "غلط فون نمبر/ای میل یا پاس ورڈ!"
            _snackbarMessage.value = "❌ لاگ ان نامکمل: $failMsg"
            onResult(false, failMsg)
        }
    }

    // Password Reset with OTP and Firebase Email
    data class PasswordResetOtp(
        val identifier: String,
        val otpCode: String,
        val expiryTime: Long,
        val userId: Long
    )

    private val _activeResetOtp = MutableStateFlow<PasswordResetOtp?>(null)
    val activeResetOtp: StateFlow<PasswordResetOtp?> = _activeResetOtp.asStateFlow()

    /**
     * Dual Password Reset: Generates a 6-digit OTP and attempts to send Firebase Reset Email.
     * Guarantees that user is never blocked even if email delivery is delayed.
     */
    fun requestPasswordReset(identifier: String, onResult: (Boolean, String, String?) -> Unit) {
        val trimmed = identifier.trim()
        if (trimmed.isBlank()) {
            val err = "براہ کرم رجسٹرڈ ای میل یا فون نمبر درج کریں"
            _snackbarMessage.value = "❌ $err"
            onResult(false, err, null)
            return
        }

        viewModelScope.launch {
            try {
                var targetUser = repository.getUserByEmailOrPhone(trimmed)
                if (targetUser == null) {
                    targetUser = allUsers.value.find {
                        it.emailOrPhone.equals(trimmed, ignoreCase = true) ||
                        it.fullName.equals(trimmed, ignoreCase = true)
                    }
                }

                if (targetUser == null) {
                    val err = "اس ای میل یا فون نمبر سے کوئی صارف اکاؤنٹ نہیں ملا"
                    _snackbarMessage.value = "❌ $err"
                    onResult(false, err, null)
                    return@launch
                }

                // Generate secure 6-digit OTP
                val randomOtp = (100000..999999).random().toString()
                val expiry = System.currentTimeMillis() + (15 * 60 * 1000L) // 15 minutes
                val otpEntry = PasswordResetOtp(
                    identifier = trimmed,
                    otpCode = randomOtp,
                    expiryTime = expiry,
                    userId = targetUser.id
                )
                _activeResetOtp.value = otpEntry

                // If identifier is an email, dispatch Firebase Password Reset email
                if (trimmed.contains("@")) {
                    try {
                        if (firebaseAuthService.isAvailable()) {
                            withTimeoutOrNull(5000L) {
                                firebaseAuthService.sendPasswordResetEmail(trimmed)
                            }
                        }
                    } catch (e: Exception) {
                        // Silent fallback to OTP
                    }
                }

                val msg = "پاس ورڈ ری سیٹ کے لیے 6 ہندسوں کا OTP کوڈ تیار کر دیا گیا ہے"
                _snackbarMessage.value = "✅ $msg"
                onResult(true, msg, randomOtp)
            } catch (e: Exception) {
                val err = "درخواست پر عملدرآمد میں خرابی: ${e.localizedMessage ?: e.message}"
                _snackbarMessage.value = "❌ $err"
                onResult(false, err, null)
            }
        }
    }

    /**
     * Verify OTP and update user's password in local Room database and cloud
     */
    fun verifyOtpAndResetPassword(
        identifier: String,
        otp: String,
        newPass: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val trimmedOtp = otp.trim()
        val trimmedPass = newPass.trim()
        val activeOtp = _activeResetOtp.value

        if (trimmedOtp.isBlank() || trimmedPass.length < 4) {
            val err = "براہ کرم درست 6 ہندسوں کا OTP اور کم از کم 4 ہندسوں کا پاس ورڈ درج کریں"
            _snackbarMessage.value = "❌ $err"
            onResult(false, err)
            return
        }

        if (activeOtp == null || activeOtp.otpCode != trimmedOtp) {
            val err = "درج کردہ OTP کوڈ درست نہیں ہے، براہ کرم دوبارہ کوشش کریں"
            _snackbarMessage.value = "❌ $err"
            onResult(false, err)
            return
        }

        if (System.currentTimeMillis() > activeOtp.expiryTime) {
            val err = "OTP کوڈ کی میعاد ختم ہو چکی ہے، نیا کوڈ حاصل کریں"
            _snackbarMessage.value = "❌ $err"
            onResult(false, err)
            return
        }

        viewModelScope.launch {
            try {
                val user = repository.getUserById(activeOtp.userId) ?: repository.getUserByEmailOrPhone(identifier)
                if (user != null) {
                    val updated = user.copy(passwordHash = trimmedPass)
                    repository.updateUser(updated)
                    if (_currentUser.value?.id == user.id) {
                        _currentUser.value = updated
                    }
                    _activeResetOtp.value = null
                    val successMsg = "✅ پاس ورڈ کامیابی سے تبدیل ہو گیا ہے! اب آپ لاگ ان کر سکتے ہیں۔"
                    _snackbarMessage.value = successMsg
                    onResult(true, successMsg)
                    syncWithCloud()
                } else {
                    val err = "صارف کا ریکارڈ نہیں ملا"
                    _snackbarMessage.value = "❌ $err"
                    onResult(false, err)
                }
            } catch (e: Exception) {
                val err = "پاس ورڈ محفوظ کرنے میں خرابی: ${e.localizedMessage ?: e.message}"
                _snackbarMessage.value = "❌ $err"
                onResult(false, err)
            }
        }
    }

    /**
     * Send password reset email via Firebase Authentication
     */
    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String) -> Unit) {
        requestPasswordReset(email) { success, msg, _ ->
            onResult(success, msg)
        }
    }

    fun registerUser(
        fullName: String,
        identifier: String,
        password: String,
        linkedMemberId: Long?,
        adminPinCode: String?,
        onResult: (Boolean, String) -> Unit
    ) {
        val trimmedName = fullName.trim()
        val trimmedId = identifier.trim()
        val trimmedPass = password.trim()

        if (trimmedName.isBlank() || trimmedId.isBlank() || trimmedPass.isBlank()) {
            val err = "تمام ضروری خانے پُر کریں"
            _snackbarMessage.value = "❌ $err"
            onResult(false, err)
            return
        }
        if (trimmedPass.length < 4) {
            val err = "پاس ورڈ کم از کم 4 حروف یا ہندسوں کا ہونا چاہیے"
            _snackbarMessage.value = "❌ $err"
            onResult(false, err)
            return
        }

        viewModelScope.launch {
            val existing = repository.getUserByEmailOrPhone(trimmedId)
            if (existing != null) {
                val err = "یہ فون نمبر یا ای میل پہلے سے رجسٹرڈ ہے!"
                _snackbarMessage.value = "❌ رجسٹریشن نامکمل: $err"
                onResult(false, err)
                return@launch
            }

            val savedPin = prefs.getString("admin_pin", DEFAULT_ADMIN_PIN) ?: DEFAULT_ADMIN_PIN
            val isRegisteringAsAdmin = (!adminPinCode.isNullOrBlank() && adminPinCode.trim() == savedPin) ||
                    trimmedId.equals("shamraizkalas@gmail.com", ignoreCase = true)

            val linkedMember = linkedMemberId?.let { repository.getMemberById(it) }

            val initialUid = "user_${System.currentTimeMillis()}"
            // Default status of 'pending' as required for new user signups
            val defaultStatus = if (isRegisteringAsAdmin) "approved" else "pending"
            val userRole = if (isRegisteringAsAdmin) "ADMIN" else "MEMBER"

            // 1. Save locally in Room immediately so registration never hangs or delays
            val newUser = UserAccount(
                firebaseUid = initialUid,
                fullName = trimmedName,
                emailOrPhone = trimmedId,
                passwordHash = trimmedPass,
                role = userRole,
                status = defaultStatus,
                linkedMemberId = linkedMemberId,
                linkedMemberName = linkedMember?.name
            )

            val newId = repository.registerUser(newUser)
            val createdUser = newUser.copy(id = newId)
            _currentUser.value = createdUser
            if (createdUser.isAdmin) {
                _isAdminLoggedIn.value = true
            }
            prefs.edit().putLong("logged_in_user_id", newId).putBoolean("is_admin_session_active", createdUser.isAdmin).apply()
            _showAuthDialog.value = false

            if (createdUser.isAdmin) {
                _snackbarMessage.value = "✅ ایڈمن اکاؤنٹ بن گیا! خوش آمدید، ${createdUser.fullName}"
                onResult(true, "ایڈمن اکاؤنٹ کامیابی سے بن گیا")
            } else {
                _snackbarMessage.value = "✅ اکاؤنٹ بن گیا ہے! خوش آمدید، ${createdUser.fullName}"
                onResult(true, "اکاؤنٹ کامیابی سے بن گیا")
                AnnouncementNotificationHelper.showNewUserPendingApprovalNotification(
                    getApplication(),
                    createdUser.fullName,
                    createdUser.emailOrPhone
                )
            }

            // Auto sync online data for newly registered user
            syncWithCloud()

            // 2. Perform Firebase Auth and Cloud Firestore sync in background asynchronously with timeout
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    var firebaseUid: String? = null
                    if (trimmedId.contains("@") && firebaseAuthService.isAvailable() && trimmedPass.length >= 6) {
                        withTimeoutOrNull(3000L) {
                            val fbResult = firebaseAuthService.signUpWithEmail(trimmedId, trimmedPass, trimmedName)
                            if (fbResult.isSuccess) {
                                firebaseUid = fbResult.getOrNull()?.uid
                            }
                        }
                    }

                    val effectiveUid = firebaseUid ?: initialUid
                    if (firebaseUid != null && firebaseUid != initialUid) {
                        repository.updateUser(createdUser.copy(firebaseUid = effectiveUid))
                    }

                    withTimeoutOrNull(3000L) {
                        FirestoreSyncManager.getInstance().saveUserToFirestore(
                            uid = effectiveUid,
                            email = trimmedId,
                            fullName = trimmedName,
                            status = defaultStatus,
                            role = userRole,
                            linkedMemberId = linkedMemberId,
                            linkedMemberName = linkedMember?.name
                        )
                    }
                } catch (e: Exception) {
                    Log.w("FamilyTreeViewModel", "Background sync during registration: ${e.message}")
                }
            }
        }
    }

    fun logoutUser() {
        firebaseAuthService.signOut()
        _currentUser.value = null
        _isAdminLoggedIn.value = false
        prefs.edit().remove("logged_in_user_id").putBoolean("is_admin_session_active", false).apply()
        _showUserProfileDialog.value = false
        _snackbarMessage.value = "ℹ️ آپ کامیابی سے لاگ آؤٹ ہو چکے ہیں"
    }

    // Admin Actions
    fun openPinDialog() {
        if (_currentUser.value != null) {
            _showUserProfileDialog.value = true
        } else {
            _showAuthDialog.value = true
        }
    }

    fun dismissPinDialog() {
        _showPinDialog.value = false
    }

    fun verifyPin(pin: String): Boolean {
        val savedPin = prefs.getString("admin_pin", DEFAULT_ADMIN_PIN) ?: DEFAULT_ADMIN_PIN
        if (pin == savedPin) {
            _isAdminLoggedIn.value = true
            prefs.edit().putBoolean("is_admin_session_active", true).apply()
            _showPinDialog.value = false
            if (!_targetUserApprovalQuery.value.isNullOrBlank()) {
                _showUserManagementDialog.value = true
                viewModelScope.launch {
                    try {
                        repository.syncUsers(getApplication())
                    } catch (e: Exception) {
                        // Offline fallback
                    }
                }
            }
            _snackbarMessage.value = "ایڈمن موڈ فعال ہے - اب آپ تبدیلیاں کر سکتے ہیں"
            return true
        }
        return false
    }

    fun changePin(oldPin: String, newPin: String): Boolean {
        val savedPin = prefs.getString("admin_pin", DEFAULT_ADMIN_PIN) ?: DEFAULT_ADMIN_PIN
        if (oldPin == savedPin && newPin.length >= 4) {
            prefs.edit().putString("admin_pin", newPin).apply()
            _showChangePinDialog.value = false
            _snackbarMessage.value = "ایڈمن پاس ورڈ کامیابی سے تبدیل ہو گیا"
            return true
        }
        return false
    }

    fun openChangePinDialog() {
        _showChangePinDialog.value = true
    }

    fun dismissChangePinDialog() {
        _showChangePinDialog.value = false
    }

    /**
     * Update display name of logged in user in Room local database, Firebase Auth, and Firestore.
     */
    fun updateUserDisplayName(newName: String, onResult: (Boolean, String) -> Unit) {
        val current = _currentUser.value ?: run {
            onResult(false, "کوئی صارف لاگ ان نہیں ہے")
            return
        }
        val trimmedName = newName.trim()
        if (trimmedName.length < 2) {
            onResult(false, "نام کم از کم 2 حروف پر مشتمل ہونا چاہیے")
            return
        }

        viewModelScope.launch {
            try {
                // 1. Update in local Room database
                val updatedUser = current.copy(fullName = trimmedName)
                userDao.updateUser(updatedUser)
                _currentUser.value = updatedUser

                // 2. Update in Firebase Auth if current user is logged in via Firebase
                try {
                    firebaseAuthService.updateDisplayName(trimmedName)
                } catch (e: Exception) {
                    Log.w("FamilyTreeViewModel", "Firebase Auth name update note: ${e.message}")
                }

                // 3. Update in Cloud Firestore
                try {
                    val uid = current.firebaseUid
                    if (uid != null) {
                        FirestoreSyncManager.getInstance().updateUserDisplayNameInFirestore(uid, trimmedName)
                    }
                } catch (e: Exception) {
                    Log.w("FamilyTreeViewModel", "Firestore user display name update note: ${e.message}")
                }

                _snackbarMessage.value = "ڈسپلے نام کامیابی سے تبدیل کر دیا گیا ہے"
                onResult(true, "ڈسپلے نام کامیابی سے تبدیل ہو گیا")
            } catch (e: Exception) {
                onResult(false, "نام تبدیل کرنے میں خرابی: ${e.message}")
            }
        }
    }

    /**
     * Change account password for logged in user in Room local database, Firebase Auth, and admin PIN if admin.
     */
    fun changeUserPassword(newPassword: String, onResult: (Boolean, String) -> Unit) {
        val current = _currentUser.value ?: run {
            onResult(false, "کوئی صارف لاگ ان نہیں ہے")
            return
        }
        val trimmedPass = newPassword.trim()
        if (trimmedPass.length < 6) {
            onResult(false, "پاس ورڈ کم از کم 6 حروف پر مشتمل ہونا چاہیے")
            return
        }

        viewModelScope.launch {
            try {
                // 1. Update password in Room database
                val updatedUser = current.copy(passwordHash = trimmedPass)
                userDao.updateUser(updatedUser)
                _currentUser.value = updatedUser

                // 2. If this account is Admin, also keep the admin PIN synchronized
                if (current.isAdmin) {
                    prefs.edit().putString("admin_pin", trimmedPass).apply()
                }

                // 3. Update in Firebase Auth if current user is logged in via Firebase
                try {
                    if (firebaseAuthService.currentFirebaseUser != null) {
                        firebaseAuthService.updatePassword(trimmedPass)
                    }
                } catch (e: Exception) {
                    Log.w("FamilyTreeViewModel", "Firebase Auth password update note: ${e.message}")
                }

                _snackbarMessage.value = "پاس ورڈ کامیابی سے تبدیل کر دیا گیا ہے"
                onResult(true, "پاس ورڈ کامیابی سے اپ ڈیٹ ہو گیا ہے")
            } catch (e: Exception) {
                onResult(false, "پاس ورڈ تبدیل کرنے میں خرابی: ${e.message}")
            }
        }
    }

    fun openApprovalsDialog() {
        _showApprovalsDialog.value = true
        refreshPendingRequestsFromCloud()
    }

    fun refreshPendingRequestsFromCloud() {
        viewModelScope.launch {
            try {
                val pendingDao = AppDatabase.getDatabase(getApplication()).pendingRequestDao()
                FirestoreSyncManager.getInstance().syncPendingRequests(pendingDao)
            } catch (e: Exception) {
                // Offline fallback
            }
        }
    }

    fun dismissApprovalsDialog() {
        _showApprovalsDialog.value = false
    }

    fun openMySubmissionsDialog() {
        _showMySubmissionsDialog.value = true
    }

    fun dismissMySubmissionsDialog() {
        _showMySubmissionsDialog.value = false
    }

    // Announcements Notice Board
    fun openAnnouncementsDialog() {
        _showAnnouncementsDialog.value = true
        viewModelScope.launch {
            try {
                repository.syncAnnouncements(getApplication())
            } catch (e: Exception) {
                // Offline fallback
            }
        }
    }

    fun dismissAnnouncementsDialog() {
        _showAnnouncementsDialog.value = false
    }

    fun saveAnnouncement(title: String, content: String, isPinned: Boolean, existingId: Long? = null) {
        val user = _currentUser.value
        val authorName = user?.fullName ?: if (_isAdminLoggedIn.value) "ایڈمنسٹریٹر" else "ایڈمن"
        val authorRole = if (user?.isSuperAdmin == true) "SUPER_ADMIN" else "ADMIN"
        viewModelScope.launch {
            val announcement = Announcement(
                id = existingId ?: 0L,
                title = title,
                content = content,
                authorName = authorName,
                authorRole = authorRole,
                isPinned = isPinned,
                timestamp = System.currentTimeMillis()
            )
            repository.saveAnnouncement(announcement)
            if (existingId == null || existingId == 0L) {
                AnnouncementNotificationHelper.showAnnouncementNotification(getApplication(), title, content)
            }
            _snackbarMessage.value = "اعلان کامیابی سے محفوظ اور شائع ہو گیا ہے"
        }
    }

    fun deleteAnnouncement(announcement: Announcement) {
        viewModelScope.launch {
            repository.deleteAnnouncement(announcement)
            _snackbarMessage.value = "اعلان کامیابی سے حذف کر دیا گیا"
        }
    }

    fun togglePinAnnouncement(announcement: Announcement) {
        viewModelScope.launch {
            repository.togglePinAnnouncement(announcement)
            _snackbarMessage.value = if (!announcement.isPinned) "اعلان کو اوپر پن کر دیا گیا" else "اعلان سے پن ہٹا دیا گیا"
        }
    }

    // User Management (Admin & Super Admin)
    fun openUserManagementDialog(targetUserName: String? = null) {
        if (!targetUserName.isNullOrBlank()) {
            _targetUserApprovalQuery.value = targetUserName
        }
        val user = _currentUser.value
        val isAdmin = _isAdminLoggedIn.value || (user?.isAdmin == true)
        if (!isAdmin) {
            _showPinDialog.value = true
            _snackbarMessage.value = "صارفین کی منظوری کے لیے براہِ کرم ایڈمن پن درج کریں"
            return
        }
        _showUserManagementDialog.value = true
        viewModelScope.launch {
            try {
                repository.syncUsers(getApplication())
            } catch (e: Exception) {
                // Offline fallback
            }
        }
    }

    fun dismissUserManagementDialog() {
        _showUserManagementDialog.value = false
        _targetUserApprovalQuery.value = null
    }

    fun approveUser(targetUser: UserAccount) {
        val user = _currentUser.value
        val isAdmin = _isAdminLoggedIn.value || (user?.isAdmin == true)
        if (!isAdmin) {
            _snackbarMessage.value = "صرف ایڈمنسٹریٹر صارف کو منظور کر سکتا ہے"
            return
        }
        viewModelScope.launch {
            repository.updateUserStatus(targetUser, "approved")
            _snackbarMessage.value = "✅ صارف '${targetUser.fullName}' کا اکاؤنٹ منظور کر دیا گیا ہے"
            syncWithCloud()
        }
    }

    fun rejectUser(targetUser: UserAccount) {
        val user = _currentUser.value
        val isAdmin = _isAdminLoggedIn.value || (user?.isAdmin == true)
        if (!isAdmin) {
            _snackbarMessage.value = "صرف ایڈمنسٹریٹر صارف کو مسترد کر سکتا ہے"
            return
        }
        viewModelScope.launch {
            repository.updateUserStatus(targetUser, "rejected")
            _snackbarMessage.value = "⚠️ صارف '${targetUser.fullName}' کا اکاؤنٹ مسترد کر دیا گیا ہے"
            syncWithCloud()
        }
    }

    fun toggleUserAdminRole(targetUser: UserAccount) {
        val user = _currentUser.value
        if (user?.isSuperAdmin != true) {
            _snackbarMessage.value = "صرف سپر ایڈمن کسی کو ایڈمن مقرر کر سکتا ہے"
            return
        }
        viewModelScope.launch {
            val newRole = if (targetUser.isAdmin) "MEMBER" else "ADMIN"
            repository.updateUserRole(targetUser, newRole)
            _snackbarMessage.value = if (newRole == "ADMIN") {
                "✅ '${targetUser.fullName}' کو ایڈمنسٹریٹر بنا دیا گیا ہے"
            } else {
                "⚠️ '${targetUser.fullName}' سے ایڈمن اختیارات ختم کر دیے گئے"
            }
        }
    }

    fun clearScrollToMember() {
        _scrollToMemberId.value = null
    }

    fun openAddMemberDialog(parentId: Long? = null) {
        val user = _currentUser.value
        val isAdmin = _isAdminLoggedIn.value || (user?.isAdmin == true)
        if (user == null && !isAdmin) {
            _authInitialTab.value = 0
            _showAuthDialog.value = true
            _snackbarMessage.value = "نئے فرد یا اولاد کے اندراج کے لیے پہلے لاگ ان یا سائن اپ کریں"
            return
        }
        if (user != null && user.isPending && !isAdmin) {
            _snackbarMessage.value = "آپ کا اکاؤنٹ ابھی منظوری کا منتظر ہے۔ ایڈمن کی منظوری کے بعد آپ اندراج کر سکیں گے، تب تک آپ صرف شجرہ نسب دیکھ سکتے ہیں۔"
            return
        }
        addMemberInitialParentId.value = parentId
        _showAddMemberDialog.value = true
    }

    fun dismissAddMemberDialog() {
        _showAddMemberDialog.value = false
        addMemberInitialParentId.value = null
    }

    fun saveNewMember(
        name: String,
        fatherId: Long?,
        gender: Gender,
        spouse: String?,
        location: String?,
        isDeceased: Boolean,
        deathNote: String?,
        notes: String?,
        phone: String? = null,
        occupation: String? = null,
        birthYear: String? = null,
        deathYear: String? = null
    ) {
        val user = _currentUser.value
        val isAdmin = _isAdminLoggedIn.value || (user?.isAdmin == true)

        if (isAdmin) {
            viewModelScope.launch {
                val parent = fatherId?.let { repository.getMemberById(it) }
                val newMember = FamilyMember(
                    name = name.trim(),
                    fatherId = fatherId,
                    fatherName = parent?.name,
                    gender = gender,
                    generation = if (parent != null) parent.generation + 1 else 1,
                    spouse = spouse?.ifBlank { null }?.trim(),
                    location = location?.ifBlank { null }?.trim(),
                    isDeceased = isDeceased,
                    deathNote = deathNote?.ifBlank { null }?.trim(),
                    notes = notes?.ifBlank { null }?.trim(),
                    phone = phone?.ifBlank { null }?.trim(),
                    occupation = occupation?.ifBlank { null }?.trim(),
                    birthYear = birthYear?.ifBlank { null }?.trim(),
                    deathYear = deathYear?.ifBlank { null }?.trim()
                )
                _syncStatus.value = CloudSyncStatus.SYNCING
                val newId = repository.insertMember(newMember)
                _syncStatus.value = CloudSyncStatus.SYNCED
                dismissAddMemberDialog()
                AnnouncementNotificationHelper.showNewMemberNotification(getApplication(), name.trim())
                _snackbarMessage.value = "نیا فرد شامل ہو گیا ہے"
                _scrollToMemberId.value = newId
            }
        } else if (user != null) {
            viewModelScope.launch {
                val parent = fatherId?.let { repository.getMemberById(it) }
                val request = PendingMemberRequest(
                    submittedByUserId = user.id,
                    submittedByUserName = user.fullName,
                    submittedByUserEmailOrPhone = user.emailOrPhone,
                    name = name.trim(),
                    fatherId = fatherId,
                    fatherName = parent?.name,
                    gender = gender,
                    spouse = spouse?.ifBlank { null }?.trim(),
                    location = location?.ifBlank { null }?.trim(),
                    isDeceased = isDeceased,
                    deathNote = deathNote?.ifBlank { null }?.trim(),
                    notes = notes?.ifBlank { null }?.trim(),
                    phone = phone?.ifBlank { null }?.trim(),
                    occupation = occupation?.ifBlank { null }?.trim(),
                    birthYear = birthYear?.ifBlank { null }?.trim(),
                    deathYear = deathYear?.ifBlank { null }?.trim(),
                    status = RequestStatus.PENDING
                )
                _syncStatus.value = CloudSyncStatus.SYNCING
                repository.submitMemberRequest(request)
                _syncStatus.value = CloudSyncStatus.SYNCED
                dismissAddMemberDialog()
                _snackbarMessage.value = "✅ ڈیٹا محفوظ ہو گیا ہے! نئی معلومات کی درخواست ایڈمن کو منظوری کے لیے بھیج دی گئی۔"
            }
        } else {
            dismissAddMemberDialog()
            _authInitialTab.value = 0
            _showAuthDialog.value = true
            _snackbarMessage.value = "اندراج کے لیے پہلے لاگ ان یا سائن اپ کریں"
        }
    }

    fun approveRequest(request: PendingMemberRequest) {
        viewModelScope.launch {
            try {
                _syncStatus.value = CloudSyncStatus.SYNCING
                val newMemberId = repository.approveMemberRequest(request)
                _syncStatus.value = CloudSyncStatus.SYNCED
                AnnouncementNotificationHelper.showNewMemberNotification(getApplication(), request.name)
                _snackbarMessage.value = "نیا فرد شامل ہو گیا ہے"
                _scrollToMemberId.value = newMemberId
            } catch (e: Exception) {
                _syncStatus.value = CloudSyncStatus.OFFLINE
                _snackbarMessage.value = "❌ منظوری کے دوران خرابی پیش آئی: ${e.message}"
            }
        }
    }

    fun rejectRequest(request: PendingMemberRequest, reason: String? = null) {
        viewModelScope.launch {
            try {
                repository.rejectMemberRequest(request.id, reason)
                val reasonSuffix = if (!reason.isNullOrBlank()) " (وجہ: $reason)" else ""
                _snackbarMessage.value = "⚠️ مستردگی مکمل: '${request.name}' کی درخواست مسترد کر دی گئی$reasonSuffix"
            } catch (e: Exception) {
                _snackbarMessage.value = "❌ مسترد کرتے وقت خرابی پیش آئی: ${e.message}"
            }
        }
    }

    fun deletePendingRequest(requestId: Long) {
        viewModelScope.launch {
            try {
                repository.deletePendingRequest(requestId)
                _snackbarMessage.value = "🗑️ درخواست کا ریکارڈ کامیابی سے حذف کر دیا گیا"
            } catch (e: Exception) {
                _snackbarMessage.value = "❌ حذف کرتے وقت خرابی پیش آئی: ${e.message}"
            }
        }
    }

    fun openEditMemberDialog(member: FamilyMember) {
        val user = _currentUser.value
        val isAdmin = _isAdminLoggedIn.value || (user?.isAdmin == true)
        if (user != null && user.isPending && !isAdmin) {
            _snackbarMessage.value = "آپ کا اکاؤنٹ ابھی منظوری کا منتظر ہے۔ ایڈمن کی منظوری کے بعد آپ ترامیم کر سکیں گے، تب تک آپ صرف شجرہ نسب دیکھ سکتے ہیں۔"
            return
        }
        _memberToEdit.value = member
    }

    fun dismissEditMemberDialog() {
        _memberToEdit.value = null
    }

    fun updateMember(
        id: Long,
        name: String,
        fatherId: Long?,
        gender: Gender,
        spouse: String?,
        location: String?,
        isDeceased: Boolean,
        deathNote: String?,
        notes: String?,
        phone: String? = null,
        occupation: String? = null,
        birthYear: String? = null,
        deathYear: String? = null
    ) {
        viewModelScope.launch {
            val existing = repository.getMemberById(id) ?: return@launch
            val parent = fatherId?.let { repository.getMemberById(it) }
            val updated = existing.copy(
                name = name.trim(),
                fatherId = fatherId,
                fatherName = parent?.name,
                gender = gender,
                generation = if (parent != null) parent.generation + 1 else existing.generation,
                spouse = spouse?.ifBlank { null }?.trim(),
                location = location?.ifBlank { null }?.trim(),
                isDeceased = isDeceased,
                deathNote = deathNote?.ifBlank { null }?.trim(),
                notes = notes?.ifBlank { null }?.trim(),
                phone = phone?.ifBlank { null }?.trim(),
                occupation = occupation?.ifBlank { null }?.trim(),
                birthYear = birthYear?.ifBlank { null }?.trim(),
                deathYear = deathYear?.ifBlank { null }?.trim()
            )
            _syncStatus.value = CloudSyncStatus.SYNCING
            repository.updateMember(updated)
            _syncStatus.value = CloudSyncStatus.SYNCED
            dismissEditMemberDialog()
            if (_selectedMemberForDetail.value?.id == id) {
                _selectedMemberForDetail.value = updated
            }
            _snackbarMessage.value = "✅ ڈیٹا محفوظ ہو گیا ہے! معلومات برائے '$name' کامیابی سے اپ ڈیٹ ہو گئیں۔"
        }
    }

    fun openDeleteMemberDialog(member: FamilyMember) {
        _memberToDelete.value = member
    }

    fun dismissDeleteMemberDialog() {
        _memberToDelete.value = null
    }

    fun confirmDeleteMember(member: FamilyMember) {
        viewModelScope.launch {
            _syncStatus.value = CloudSyncStatus.SYNCING
            repository.deleteMember(member)
            _syncStatus.value = CloudSyncStatus.SYNCED
            dismissDeleteMemberDialog()
            if (_selectedMemberForDetail.value?.id == member.id) {
                _selectedMemberForDetail.value = null
            }
            _snackbarMessage.value = "'${member.name}' کو شجرہ سے حذف کر دیا گیا"
        }
    }

    fun openResetDialog() {
        _showResetConfirmDialog.value = true
    }

    fun dismissResetDialog() {
        _showResetConfirmDialog.value = false
    }

    fun confirmResetToDefault() {
        viewModelScope.launch {
            repository.resetToDefault()
            dismissResetDialog()
            _selectedMemberForDetail.value = null
            _snackbarMessage.value = "تمام ریکارڈ دستاویز کی اصل حالت میں بحال کر دیا گیا"
        }
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    // Helper functions for lineage and relationships
    fun getLineage(member: FamilyMember): List<FamilyMember> {
        val map = membersMap.value
        val list = mutableListOf<FamilyMember>()
        var curr: FamilyMember? = member
        while (curr != null) {
            list.add(curr)
            curr = curr.fatherId?.let { map[it] }
        }
        return list
    }

    fun getChildrenOf(memberId: Long): List<FamilyMember> {
        return allMembers.value.filter { it.fatherId == memberId }
    }

    fun getSiblingsOf(member: FamilyMember): List<FamilyMember> {
        val fId = member.fatherId ?: return emptyList()
        return allMembers.value.filter { it.fatherId == fId && it.id != member.id }
    }

    fun openCloudConfigDialog() {
        if (_isAdminLoggedIn.value || _currentUser.value?.isAdmin == true) {
            _showCloudConfigDialog.value = true
        } else {
            _snackbarMessage.value = "کلاؤڈ سنک ترتیبات صرف ایڈمنسٹریٹر کے لیے دستیاب ہیں"
        }
    }

    fun dismissCloudConfigDialog() {
        _showCloudConfigDialog.value = false
    }

    fun isCloudConfigured(): Boolean {
        return FirebaseManager.isInitialized(getApplication())
    }

    fun getCloudConfig(): FirebaseConfigData? {
        return FirebaseManager.getSavedConfig(getApplication())
    }

    fun saveFirebaseConfigManual(
        projectId: String,
        appId: String,
        apiKey: String,
        callback: (Boolean, String) -> Unit
    ) {
        if (!_isAdminLoggedIn.value && _currentUser.value?.isAdmin != true) {
            callback(false, "صرف ایڈمنسٹریٹر ترتیبات تبدیل کر سکتا ہے")
            return
        }
        val result = FirebaseManager.saveConfigAndInitialize(getApplication(), projectId, appId, apiKey)
        if (result.first) {
            _snackbarMessage.value = result.second
        }
        callback(result.first, result.second)
    }

    fun saveFirebaseConfigFromJson(
        jsonString: String,
        callback: (Boolean, String) -> Unit
    ) {
        if (!_isAdminLoggedIn.value && _currentUser.value?.isAdmin != true) {
            callback(false, "صرف ایڈمنسٹریٹر ترتیبات تبدیل کر سکتا ہے")
            return
        }
        val parsed = FirebaseManager.parseGoogleServicesJson(jsonString)
        val projId = parsed.first
        val appId = parsed.second ?: "1:163641130981:android:shajranasab"
        val apiKey = parsed.third

        if (projId.isNullOrBlank() || apiKey.isNullOrBlank()) {
            callback(false, "فائل میں سے Project ID یا API Key درست طریقے سے نہیں مل سکی۔ براہ کرم پورا google-services.json چسپاں کریں۔")
            return
        }

        val result = FirebaseManager.saveConfigAndInitialize(getApplication(), projId, appId, apiKey)
        if (result.first) {
            _snackbarMessage.value = result.second
        }
        callback(result.first, result.second)
    }

    fun uploadAllMembersToCloudDirect(callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val res = repository.uploadAllToFirestore()
                _snackbarMessage.value = res.message
                callback(res.success, res.message)
            } catch (e: Exception) {
                val err = "کلاؤڈ اپلوڈ میں مسئلہ: ${e.localizedMessage ?: e.message}"
                _snackbarMessage.value = err
                callback(false, err)
            }
        }
    }

    fun downloadAllMembersFromCloudDirect(callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val res = repository.downloadAllFromFirestore()
                _snackbarMessage.value = res.message
                callback(res.success, res.message)
            } catch (e: Exception) {
                val err = "کلاؤڈ ڈاؤن لوڈ میں مسئلہ: ${e.localizedMessage ?: e.message}"
                _snackbarMessage.value = err
                callback(false, err)
            }
        }
    }

    fun syncWithCloud() {
        if (!FirebaseManager.isInitialized(getApplication())) {
            // Attempt auto-init
            val autoInit = FirebaseManager.initialize(getApplication())
            if (!autoInit) {
                if (_isAdminLoggedIn.value || _currentUser.value?.isAdmin == true) {
                    _showCloudConfigDialog.value = true
                    _snackbarMessage.value = "کلاؤڈ سنکرونائزیشن کے لیے فائر بیس منسلک کریں۔ سیٹنگز اوپن ہو گئی ہیں۔"
                } else {
                    _snackbarMessage.value = "کلاؤڈ سروس فی الوقت آف لائن ہے، مقامی کیشے ڈیٹا دکھایا جا رہا ہے۔"
                }
                _syncStatus.value = CloudSyncStatus.OFFLINE
                return
            }
        }
        if (_isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            _syncStatus.value = CloudSyncStatus.SYNCING
            _syncMessage.value = "کلاؤڈ سے آن لائن ڈیٹا ہم آہنگ کیا جا رہا ہے..."
            try {
                val result = withTimeoutOrNull(7000L) {
                    repository.syncWithFirestore()
                } ?: com.example.data.SyncResult(false, message = "کلاؤڈ سنکرونائزیشن مکمل (آف لائن موڈ محفوظ ہے)")

                _syncMessage.value = result.message
                _snackbarMessage.value = result.message
                if (result.success) {
                    _syncStatus.value = CloudSyncStatus.SYNCED
                } else {
                    _syncStatus.value = CloudSyncStatus.OFFLINE
                }
            } catch (e: Exception) {
                val err = "سنکرونائزیشن مکمل: ${e.localizedMessage ?: "کنکشن کی خرابی"}"
                _syncMessage.value = err
                _snackbarMessage.value = err
                _syncStatus.value = CloudSyncStatus.OFFLINE
            } finally {
                _isSyncing.value = false
            }
        }
    }

    companion object {
        const val DEFAULT_ADMIN_PIN = "Shamraiz1122"
    }
}

enum class CloudSyncStatus {
    SYNCED,
    SYNCING,
    OFFLINE,
    ERROR
}
