package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class FamilyStatistics(
    val totalCount: Int,
    val maleCount: Int,
    val femaleCount: Int,
    val deceasedCount: Int,
    val generationCount: Int,
    val branchCounts: Map<String, Int>
)

data class RelationshipResult(
    val personA: FamilyMember,
    val personB: FamilyMember,
    val relationAtoB: String,
    val relationBtoA: String,
    val commonAncestor: FamilyMember?,
    val description: String,
    val pathA: List<FamilyMember>,
    val pathB: List<FamilyMember>
)

class FamilyRepository(
    private val dao: FamilyDao,
    private val userDao: UserDao? = null,
    private val pendingRequestDao: PendingRequestDao? = null,
    private val announcementDao: AnnouncementDao? = null
) {

    val allMembersFlow: Flow<List<FamilyMember>> = dao.getAllMembersFlow()
    val rootMembersFlow: Flow<List<FamilyMember>> = dao.getRootMembersFlow()
    val allUsersFlow: Flow<List<UserAccount>> = userDao?.getAllUsersFlow() ?: kotlinx.coroutines.flow.flowOf(emptyList())
    val allPendingRequestsFlow: Flow<List<PendingMemberRequest>> = pendingRequestDao?.getAllRequestsFlow() ?: kotlinx.coroutines.flow.flowOf(emptyList())
    val activePendingRequestsFlow: Flow<List<PendingMemberRequest>> = pendingRequestDao?.getPendingRequestsFlow() ?: kotlinx.coroutines.flow.flowOf(emptyList())
    val pendingCountFlow: Flow<Int> = pendingRequestDao?.getPendingCountFlow() ?: kotlinx.coroutines.flow.flowOf(0)
    val allAnnouncementsFlow: Flow<List<Announcement>> = announcementDao?.getAllAnnouncementsFlow() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun saveAnnouncement(announcement: Announcement): Long = withContext(Dispatchers.IO) {
        val id = announcementDao?.insert(announcement) ?: 0L
        val toUpload = if (announcement.id == 0L && id > 0) announcement.copy(id = id) else announcement
        try {
            FirestoreSyncManager.getInstance().uploadSingleAnnouncement(toUpload)
        } catch (e: Exception) {
            // Offline mode
        }
        id
    }

    suspend fun deleteAnnouncement(announcement: Announcement) = withContext(Dispatchers.IO) {
        announcementDao?.delete(announcement)
        try {
            FirestoreSyncManager.getInstance().deleteAnnouncementFromCloud(announcement.id)
        } catch (e: Exception) {
            // Offline mode
        }
    }

    suspend fun togglePinAnnouncement(announcement: Announcement) = withContext(Dispatchers.IO) {
        val updated = announcement.copy(isPinned = !announcement.isPinned, updatedAt = System.currentTimeMillis())
        announcementDao?.update(updated)
        try {
            FirestoreSyncManager.getInstance().uploadSingleAnnouncement(updated)
        } catch (e: Exception) {
            // Offline mode
        }
    }

    suspend fun syncAnnouncements(context: android.content.Context? = null): SyncResult = withContext(Dispatchers.IO) {
        if (announcementDao == null) return@withContext SyncResult(false, message = "Announcement DAO unavailable")
        FirestoreSyncManager.getInstance().syncAnnouncements(announcementDao, context)
    }

    suspend fun updateUserRole(user: UserAccount, newRole: String) = withContext(Dispatchers.IO) {
        val updated = user.copy(role = newRole)
        userDao?.updateUser(updated)
        if (!user.firebaseUid.isNullOrBlank()) {
            try {
                FirestoreSyncManager.getInstance().updateUserRoleInFirestore(user.firebaseUid, newRole)
            } catch (e: Exception) {
                // Offline resilience
            }
        }
    }

    suspend fun updateUserStatus(user: UserAccount, newStatus: String) = withContext(Dispatchers.IO) {
        val updated = user.copy(status = newStatus)
        userDao?.updateUser(updated)
        if (!user.firebaseUid.isNullOrBlank()) {
            try {
                FirestoreSyncManager.getInstance().updateUserStatusInFirestore(user.firebaseUid, newStatus)
            } catch (e: Exception) {
                // Offline resilience
            }
        }
    }

    fun getRequestsByUserFlow(userId: Long): Flow<List<PendingMemberRequest>> {
        return pendingRequestDao?.getRequestsByUserFlow(userId) ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }

    suspend fun submitMemberRequest(request: PendingMemberRequest): Long = withContext(Dispatchers.IO) {
        val insertedId = pendingRequestDao?.insert(request) ?: -1L
        try {
            val reqToUpload = if (request.id == 0L && insertedId > 0) request.copy(id = insertedId) else request
            FirestoreSyncManager.getInstance().uploadSingleRequest(reqToUpload)
        } catch (e: Exception) {
            // Ignore cloud failure in offline mode
        }
        insertedId
    }

    suspend fun approveMemberRequest(request: PendingMemberRequest): Long = withContext(Dispatchers.IO) {
        val father = request.fatherId?.let { dao.getMemberById(it) }
        val generation = if (father != null) father.generation + 1 else 1
        val fatherName = father?.name ?: request.fatherName

        // Calculate display order for children of this father
        val siblings = if (request.fatherId != null) dao.getChildrenOf(request.fatherId) else emptyList()
        val displayOrder = (siblings.maxOfOrNull { it.displayOrder } ?: 0) + 1

        val newMember = FamilyMember(
            name = request.name,
            fatherId = request.fatherId,
            fatherName = fatherName,
            motherName = request.motherName,
            gender = request.gender,
            generation = generation,
            spouse = request.spouse,
            location = request.location,
            isDeceased = request.isDeceased,
            deathNote = request.deathNote,
            notes = request.notes,
            displayOrder = displayOrder,
            phone = request.phone,
            occupation = request.occupation,
            birthYear = request.birthYear,
            deathYear = request.deathYear
        )

        val insertedId = dao.insert(newMember)

        // Update request status to APPROVED
        pendingRequestDao?.updateStatus(
            id = request.id,
            status = RequestStatus.APPROVED,
            reviewedAt = System.currentTimeMillis(),
            reason = null
        )

        try {
            val memberWithId = if (newMember.id == 0L && insertedId > 0) newMember.copy(id = insertedId) else newMember
            FirestoreSyncManager.getInstance().uploadSingleMember(memberWithId)
            FirestoreSyncManager.getInstance().updateRequestStatusInCloud(request.id, RequestStatus.APPROVED, null)
        } catch (e: Exception) {
            // Offline resilience
        }

        insertedId
    }

    suspend fun rejectMemberRequest(requestId: Long, reason: String? = null) = withContext(Dispatchers.IO) {
        pendingRequestDao?.updateStatus(
            id = requestId,
            status = RequestStatus.REJECTED,
            reviewedAt = System.currentTimeMillis(),
            reason = reason
        )
        try {
            FirestoreSyncManager.getInstance().updateRequestStatusInCloud(requestId, RequestStatus.REJECTED, reason)
        } catch (e: Exception) {
            // Offline resilience
        }
    }

    suspend fun deletePendingRequest(requestId: Long) = withContext(Dispatchers.IO) {
        pendingRequestDao?.deleteById(requestId)
        try {
            FirestoreSyncManager.getInstance().deletePendingRequestFromCloud(requestId)
        } catch (e: Exception) {
            // Offline resilience
        }
    }

    suspend fun syncWithFirestore(context: android.content.Context? = null): SyncResult = withContext(Dispatchers.IO) {
        val syncManager = FirestoreSyncManager.getInstance()
        val membersResult = syncManager.syncMembers(dao)
        val requestsResult = pendingRequestDao?.let { syncManager.syncPendingRequests(it) }
        val announcementsResult = announcementDao?.let { syncManager.syncAnnouncements(it) }
        val usersResult = userDao?.let { syncManager.syncUsers(it, context) }
        val isSuccess = membersResult.success && (requestsResult?.success ?: true) && (announcementsResult?.success ?: true)
        val msg = if (isSuccess) {
            "فائر بیس آن لائن کلاؤڈ ڈیٹا مکمل ہم آہنگ ہو گیا ہے۔"
        } else {
            membersResult.message.ifBlank { requestsResult?.message ?: announcementsResult?.message ?: usersResult?.message ?: "آن لائن سنکرونائزیشن مکمل نہ ہو سکی" }
        }
        SyncResult(
            success = isSuccess,
            membersSynced = membersResult.membersSynced,
            requestsSynced = requestsResult?.requestsSynced ?: 0,
            message = msg
        )
    }

    suspend fun syncUsers(context: android.content.Context? = null): SyncResult = withContext(Dispatchers.IO) {
        val syncManager = FirestoreSyncManager.getInstance()
        userDao?.let { syncManager.syncUsers(it, context) } ?: SyncResult(false, message = "صارفین کا ڈیٹا بیس دستیاب نہیں")
    }

    suspend fun uploadAllToFirestore(): SyncResult = withContext(Dispatchers.IO) {
        val syncManager = FirestoreSyncManager.getInstance()
        syncManager.uploadAllMembersToCloud(dao)
    }

    suspend fun downloadAllFromFirestore(): SyncResult = withContext(Dispatchers.IO) {
        val syncManager = FirestoreSyncManager.getInstance()
        syncManager.downloadAllMembersFromCloud(dao)
    }

    suspend fun checkAndInitializeDatabase() = withContext(Dispatchers.IO) {
        val count = dao.getCount()
        if (count == 0) {
            dao.insertAll(InitialData.getInitialFamilyMembers())
        } else {
            // Update member 551 if previously named محمد شمریز
            val m551 = dao.getMemberById(551L)
            if (m551 != null && m551.name == "محمد شمریز") {
                dao.update(m551.copy(name = "شمریز ایوب"))
            }
        }
        // Initialize default admin accounts or update existing admin names
        userDao?.let { uDao ->
            val existingAdmin = uDao.getUserByEmailOrPhone("admin")
            if (existingAdmin != null && (existingAdmin.fullName.contains("محمد شمریز") || existingAdmin.linkedMemberName?.contains("محمد شمریز") == true)) {
                uDao.updateUser(existingAdmin.copy(fullName = "شمریز ایوب (ایڈمن)", linkedMemberName = "شمریز ایوب"))
            }
            val existingEmailAdmin = uDao.getUserByEmailOrPhone("shamraizkalas@gmail.com")
            if (existingEmailAdmin != null && (existingEmailAdmin.fullName.contains("محمد شمریز") || existingEmailAdmin.linkedMemberName?.contains("محمد شمریز") == true)) {
                uDao.updateUser(existingEmailAdmin.copy(fullName = "شمریز ایوب", linkedMemberName = "شمریز ایوب"))
            }
            if (uDao.getUserCount() == 0) {
                uDao.insertUser(
                    UserAccount(
                        fullName = "شمریز ایوب (ایڈمن)",
                        emailOrPhone = "admin",
                        passwordHash = "Shamraiz1122",
                        role = "ADMIN",
                        status = "approved",
                        linkedMemberName = "شمریز ایوب"
                    )
                )
                uDao.insertUser(
                    UserAccount(
                        fullName = "شمریز ایوب",
                        emailOrPhone = "shamraizkalas@gmail.com",
                        passwordHash = "Shamraiz1122",
                        role = "ADMIN",
                        status = "approved",
                        linkedMemberName = "شمریز ایوب"
                    )
                )
            }
        }
    }

    suspend fun getUserByEmailOrPhone(identifier: String): UserAccount? = withContext(Dispatchers.IO) {
        userDao?.getUserByEmailOrPhone(identifier.trim())
    }

    suspend fun getUserById(id: Long): UserAccount? = withContext(Dispatchers.IO) {
        userDao?.getUserById(id)
    }

    suspend fun registerUser(user: UserAccount): Long = withContext(Dispatchers.IO) {
        userDao?.insertUser(user) ?: -1L
    }

    suspend fun updateUser(user: UserAccount) = withContext(Dispatchers.IO) {
        userDao?.updateUser(user)
    }

    suspend fun resetToDefault() = withContext(Dispatchers.IO) {
        dao.deleteAll()
        dao.insertAll(InitialData.getInitialFamilyMembers())
    }

    fun searchMembers(query: String): Flow<List<FamilyMember>> {
        return dao.searchMembers(query)
    }

    fun getChildrenFlow(parentId: Long): Flow<List<FamilyMember>> {
        return dao.getChildrenOfFlow(parentId)
    }

    suspend fun getChildren(parentId: Long): List<FamilyMember> = withContext(Dispatchers.IO) {
        dao.getChildrenOf(parentId)
    }

    suspend fun getMemberById(id: Long): FamilyMember? = withContext(Dispatchers.IO) {
        dao.getMemberById(id)
    }

    suspend fun insertMember(member: FamilyMember): Long = withContext(Dispatchers.IO) {
        val parent = member.fatherId?.let { dao.getMemberById(it) }
        val updated = member.copy(
            generation = if (parent != null) parent.generation + 1 else member.generation,
            fatherName = parent?.name ?: member.fatherName
        )
        val id = dao.insert(updated)
        try {
            val toUpload = if (updated.id == 0L && id > 0) updated.copy(id = id) else updated
            FirestoreSyncManager.getInstance().uploadSingleMember(toUpload)
        } catch (e: Exception) {
            // Offline resilience
        }
        id
    }

    suspend fun updateMember(member: FamilyMember) = withContext(Dispatchers.IO) {
        val parent = member.fatherId?.let { dao.getMemberById(it) }
        val updated = member.copy(
            generation = if (parent != null) parent.generation + 1 else member.generation,
            fatherName = parent?.name ?: member.fatherName
        )
        dao.update(updated)
        try {
            FirestoreSyncManager.getInstance().uploadSingleMember(updated)
        } catch (e: Exception) {
            // Offline resilience
        }
    }

    suspend fun deleteMember(member: FamilyMember) = withContext(Dispatchers.IO) {
        dao.delete(member)
        try {
            FirestoreSyncManager.getInstance().deleteMemberFromCloud(member.id)
        } catch (e: Exception) {
            // Offline resilience
        }
    }

    suspend fun getLineage(member: FamilyMember, allMembersMap: Map<Long, FamilyMember>): List<FamilyMember> {
        val lineage = mutableListOf<FamilyMember>()
        var current: FamilyMember? = member
        while (current != null) {
            lineage.add(current)
            current = current.fatherId?.let { allMembersMap[it] }
        }
        return lineage
    }

    fun getFullLineageChainString(member: FamilyMember, allMembersMap: Map<Long, FamilyMember>): String {
        val chain = mutableListOf<String>()
        var current: FamilyMember? = member
        while (current != null) {
            chain.add(current.name)
            current = current.fatherId?.let { allMembersMap[it] }
        }
        return chain.joinToString(separator = " ولد ")
    }

    fun calculateRelationship(
        personA: FamilyMember,
        personB: FamilyMember,
        allMembersMap: Map<Long, FamilyMember>
    ): RelationshipResult {
        if (personA.id == personB.id) {
            return RelationshipResult(
                personA = personA,
                personB = personB,
                relationAtoB = "خود",
                relationBtoA = "خود",
                commonAncestor = personA,
                description = "یہ ایک ہی فرد ہیں",
                pathA = listOf(personA),
                pathB = listOf(personB)
            )
        }

        val ancestorsA = mutableListOf<FamilyMember>()
        var currA: FamilyMember? = personA
        while (currA != null) {
            ancestorsA.add(currA)
            currA = currA.fatherId?.let { allMembersMap[it] }
        }

        val ancestorsB = mutableListOf<FamilyMember>()
        var currB: FamilyMember? = personB
        while (currB != null) {
            ancestorsB.add(currB)
            currB = currB.fatherId?.let { allMembersMap[it] }
        }

        var commonAncestor: FamilyMember? = null
        var distA = -1
        var distB = -1

        for ((idxA, ancA) in ancestorsA.withIndex()) {
            val idxB = ancestorsB.indexOfFirst { it.id == ancA.id }
            if (idxB != -1) {
                commonAncestor = ancA
                distA = idxA
                distB = idxB
                break
            }
        }

        if (commonAncestor == null) {
            return RelationshipResult(
                personA = personA,
                personB = personB,
                relationAtoB = "شجرہ میں براہِ راست تعلق نہیں",
                relationBtoA = "شجرہ میں براہِ راست تعلق نہیں",
                commonAncestor = null,
                description = "ان دونوں افراد کے درمیان مشترکہ جد امجد نہیں ملا",
                pathA = ancestorsA,
                pathB = ancestorsB
            )
        }

        val pathA = ancestorsA.take(distA + 1)
        val pathB = ancestorsB.take(distB + 1)

        val relationAtoB: String
        val relationBtoA: String
        val desc: String

        when {
            distA == 0 && distB == 1 -> {
                // A is parent of B
                relationAtoB = if (personA.isMale) "والد محترم" else "والدہ محترمہ"
                relationBtoA = if (personB.isMale) "بیٹا" else "بیٹی"
                desc = "${personA.name}، ${personB.name} کے والد ہیں"
            }
            distA == 1 && distB == 0 -> {
                // B is parent of A
                relationAtoB = if (personA.isMale) "بیٹا" else "بیٹی"
                relationBtoA = if (personB.isMale) "والد محترم" else "والدہ محترمہ"
                desc = "${personA.name}، ${personB.name} کے فرزند/دختر ہیں"
            }
            distA == 0 && distB == 2 -> {
                // A is grandfather of B
                relationAtoB = if (personA.isMale) "دادا جان" else "دادی جان"
                relationBtoA = if (personB.isMale) "پوتا" else "پوتی"
                desc = "${personA.name}، ${personB.name} کے دادا ہیں"
            }
            distA == 2 && distB == 0 -> {
                // B is grandfather of A
                relationAtoB = if (personA.isMale) "پوتا" else "پوتی"
                relationBtoA = if (personB.isMale) "دادا جان" else "دادی جان"
                desc = "${personA.name}، ${personB.name} کے پوتا/پوتی ہیں"
            }
            distA == 0 && distB >= 3 -> {
                relationAtoB = "پردادا جان"
                relationBtoA = if (personB.isMale) "پڑپوتا" else "پڑپوتی"
                desc = "${personA.name}، ${personB.name} کے پردادا ہیں"
            }
            distA >= 3 && distB == 0 -> {
                relationAtoB = if (personA.isMale) "پڑپوتا" else "پڑپوتی"
                relationBtoA = "پردادا جان"
                desc = "${personA.name}، ${personB.name} کے پڑپوتا/پڑپوتی ہیں"
            }
            distA == 1 && distB == 1 -> {
                // Siblings
                relationAtoB = if (personA.isMale) "سگا بھائی" else "سگی بہن"
                relationBtoA = if (personB.isMale) "سگا بھائی" else "سگی بہن"
                desc = "دونوں سگے بھائی / بہن ہیں (والد: ${commonAncestor.name})"
            }
            distA == 1 && distB == 2 -> {
                // A is uncle/aunt to B
                relationAtoB = if (personA.isMale) "چچا / تایا" else "پھوپھی جان"
                relationBtoA = if (personB.isMale) "بھتیجا" else "بھتیجی"
                desc = "${personA.name}، ${personB.name} کے چچا/تایا ہیں"
            }
            distA == 2 && distB == 1 -> {
                // B is uncle/aunt to A
                relationAtoB = if (personA.isMale) "بھتیجا" else "بھتیجی"
                relationBtoA = if (personB.isMale) "چچا / تایا" else "پھوپھی جان"
                desc = "${personB.name}، ${personA.name} کے چچا/تایا ہیں"
            }
            distA == 2 && distB == 2 -> {
                // First cousins
                relationAtoB = if (personA.isMale) "چچا زاد / تایا زاد بھائی" else "چچا زاد / تایا زاد بہن"
                relationBtoA = if (personB.isMale) "چچا زاد / تایا زاد بھائی" else "چچا زاد / تایا زاد بہن"
                desc = "دونوں آپس میں چچا زاد / تایا زاد بھائی بہن ہیں (مشترکہ دادا: ${commonAncestor.name})"
            }
            distA == 2 && distB == 3 -> {
                relationAtoB = if (personA.isMale) "تایا / چچا (ایک نسل کا فرق)" else "پھوپھی"
                relationBtoA = if (personB.isMale) "بھتیجا" else "بھتیجی"
                desc = "${personA.name} کے والد اور ${personB.name} کے دادا سگے بھائی ہیں"
            }
            distA == 3 && distB == 2 -> {
                relationAtoB = if (personA.isMale) "بھتیجا" else "بھتیجی"
                relationBtoA = if (personB.isMale) "تایا / چچا" else "پھوپھی"
                desc = "${personB.name} کے والد اور ${personA.name} کے دادا سگے بھائی ہیں"
            }
            distA == 3 && distB == 3 -> {
                relationAtoB = "دوسری پشت کے کزن"
                relationBtoA = "دوسری پشت کے کزن"
                desc = "دونوں آپس میں دوسری پشت کے کزن ہیں (مشترکہ پردادا: ${commonAncestor.name})"
            }
            else -> {
                relationAtoB = "خاندانی رشتہ دار"
                relationBtoA = "خاندانی رشتہ دار"
                desc = "مشترکہ جد: ${commonAncestor.name} (${distA} پشت بمقابلہ ${distB} پشت)"
            }
        }

        return RelationshipResult(
            personA = personA,
            personB = personB,
            relationAtoB = relationAtoB,
            relationBtoA = relationBtoA,
            commonAncestor = commonAncestor,
            description = desc,
            pathA = pathA,
            pathB = pathB
        )
    }

    suspend fun getStatistics(allMembers: List<FamilyMember>): FamilyStatistics = withContext(Dispatchers.Default) {
        val total = allMembers.size
        val males = allMembers.count { it.isMale }
        val females = allMembers.count { it.isFemale }
        val deceased = allMembers.count { it.isDeceased }
        val maxGen = allMembers.maxOfOrNull { it.generation } ?: 1

        val branchRoots = allMembers.filter { it.fatherId == 1L }
        val branchMap = mutableMapOf<String, Int>()

        for (branch in branchRoots) {
            val count = countDescendants(branch.id, allMembers) + 1
            branchMap[branch.name] = count
        }

        FamilyStatistics(
            totalCount = total,
            maleCount = males,
            femaleCount = females,
            deceasedCount = deceased,
            generationCount = maxGen,
            branchCounts = branchMap
        )
    }

    fun exportToJson(members: List<FamilyMember>): String {
        val root = org.json.JSONObject()
        root.put("shajra_name", "شجرہ نسب اولاد محمد علی")
        root.put("version", 2)
        root.put("total_members", members.size)
        root.put("exported_at", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date()))

        val array = org.json.JSONArray()
        for (m in members) {
            val obj = org.json.JSONObject()
            obj.put("id", m.id)
            obj.put("name", m.name)
            if (m.fatherId != null) obj.put("fatherId", m.fatherId) else obj.put("fatherId", org.json.JSONObject.NULL)
            if (m.fatherName != null) obj.put("fatherName", m.fatherName) else obj.put("fatherName", org.json.JSONObject.NULL)
            if (m.motherName != null) obj.put("motherName", m.motherName) else obj.put("motherName", org.json.JSONObject.NULL)
            obj.put("gender", m.gender.name)
            obj.put("generation", m.generation)
            if (m.spouse != null) obj.put("spouse", m.spouse) else obj.put("spouse", org.json.JSONObject.NULL)
            if (m.location != null) obj.put("location", m.location) else obj.put("location", org.json.JSONObject.NULL)
            obj.put("isDeceased", m.isDeceased)
            if (m.deathNote != null) obj.put("deathNote", m.deathNote) else obj.put("deathNote", org.json.JSONObject.NULL)
            if (m.notes != null) obj.put("notes", m.notes) else obj.put("notes", org.json.JSONObject.NULL)
            obj.put("displayOrder", m.displayOrder)
            if (m.phone != null) obj.put("phone", m.phone) else obj.put("phone", org.json.JSONObject.NULL)
            if (m.occupation != null) obj.put("occupation", m.occupation) else obj.put("occupation", org.json.JSONObject.NULL)
            if (m.birthYear != null) obj.put("birthYear", m.birthYear) else obj.put("birthYear", org.json.JSONObject.NULL)
            if (m.deathYear != null) obj.put("deathYear", m.deathYear) else obj.put("deathYear", org.json.JSONObject.NULL)
            array.put(obj)
        }
        root.put("members", array)
        return root.toString(2)
    }

    suspend fun importFromJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = org.json.JSONObject(jsonString.trim())
            val array = root.getJSONArray("members")
            val membersList = mutableListOf<FamilyMember>()

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val member = FamilyMember(
                    id = obj.optLong("id", 0L),
                    name = obj.getString("name"),
                    fatherId = if (obj.isNull("fatherId")) null else obj.optLong("fatherId"),
                    fatherName = if (obj.isNull("fatherName")) null else obj.optString("fatherName"),
                    motherName = if (obj.isNull("motherName")) null else obj.optString("motherName"),
                    gender = try { Gender.valueOf(obj.optString("gender", "MALE")) } catch (e: Exception) { Gender.MALE },
                    generation = obj.optInt("generation", 1),
                    spouse = if (obj.isNull("spouse")) null else obj.optString("spouse"),
                    location = if (obj.isNull("location")) null else obj.optString("location"),
                    isDeceased = obj.optBoolean("isDeceased", false),
                    deathNote = if (obj.isNull("deathNote")) null else obj.optString("deathNote"),
                    notes = if (obj.isNull("notes")) null else obj.optString("notes"),
                    displayOrder = obj.optInt("displayOrder", 0),
                    phone = if (obj.isNull("phone")) null else obj.optString("phone"),
                    occupation = if (obj.isNull("occupation")) null else obj.optString("occupation"),
                    birthYear = if (obj.isNull("birthYear")) null else obj.optString("birthYear"),
                    deathYear = if (obj.isNull("deathYear")) null else obj.optString("deathYear")
                )
                membersList.add(member)
            }

            if (membersList.isNotEmpty()) {
                dao.deleteAll()
                dao.insertAll(membersList)
                Result.success(membersList.size)
            } else {
                Result.failure(Exception("فائل میں کوئی ریکارڈ نہیں ملا"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun exportReadableTextTree(allMembers: List<FamilyMember>): String {
        val root = allMembers.find { it.fatherId == null } ?: return "کوئی بانی نہیں ملا"
        val sb = StringBuilder()
        sb.append("═══════════════════════════════════════\n")
        sb.append("    شجرہ نسب اولاد محمد علی    \n")
        sb.append("═══════════════════════════════════════\n\n")

        fun buildTreeText(parentId: Long, indent: String) {
            val children = allMembers.filter { it.fatherId == parentId }.sortedBy { it.displayOrder }
            for ((index, child) in children.withIndex()) {
                val isLast = index == children.size - 1
                val branchSymbol = if (isLast) "└── " else "├── "
                val childIndent = indent + (if (isLast) "    " else "│   ")

                val status = if (child.isDeceased) " (مرحوم)" else ""
                val genStr = "[نسل ${child.generation}]"
                val spouseStr = if (!child.spouse.isNullOrBlank()) " | زوجہ: ${child.spouse}" else ""
                val locStr = if (!child.location.isNullOrBlank()) " | رہائش: ${child.location}" else ""
                val phoneStr = if (!child.phone.isNullOrBlank()) " | فون: ${child.phone}" else ""

                sb.append("$indent$branchSymbol${child.name}$status $genStr$spouseStr$locStr$phoneStr\n")
                buildTreeText(child.id, childIndent)
            }
        }

        sb.append("👑 ${root.name} (بانیٔ شجرہ نسب)\n")
        buildTreeText(root.id, "  ")
        return sb.toString()
    }

    private fun countDescendants(parentId: Long, allMembers: List<FamilyMember>): Int {
        val children = allMembers.filter { it.fatherId == parentId }
        var sum = children.size
        for (child in children) {
            sum += countDescendants(child.id, allMembers)
        }
        return sum
    }
}
