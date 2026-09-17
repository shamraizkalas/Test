package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RequestStatus {
    PENDING,
    APPROVED,
    REJECTED
}

@Entity(tableName = "pending_requests")
data class PendingMemberRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val submittedByUserId: Long,
    val submittedByUserName: String,
    val submittedByUserEmailOrPhone: String,
    val submittedAt: Long = System.currentTimeMillis(),
    val name: String,
    val fatherId: Long? = null,
    val fatherName: String? = null,
    val motherName: String? = null,
    val gender: Gender = Gender.MALE,
    val spouse: String? = null,
    val location: String? = null,
    val isDeceased: Boolean = false,
    val deathNote: String? = null,
    val notes: String? = null,
    val phone: String? = null,
    val occupation: String? = null,
    val birthYear: String? = null,
    val deathYear: String? = null,
    val status: RequestStatus = RequestStatus.PENDING,
    val rejectionReason: String? = null,
    val reviewedAt: Long? = null
) {
    val isPending: Boolean get() = status == RequestStatus.PENDING
    val isApproved: Boolean get() = status == RequestStatus.APPROVED
    val isRejected: Boolean get() = status == RequestStatus.REJECTED
    val isMale: Boolean get() = gender == Gender.MALE

    val statusLabelUrdu: String get() = when (status) {
        RequestStatus.PENDING -> "زیرِ التواء منظوری (Pending)"
        RequestStatus.APPROVED -> "منظور شدہ (Approved)"
        RequestStatus.REJECTED -> "مسترد شدہ (Rejected)"
    }
}
