package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firebaseUid: String? = null,
    val fullName: String,
    val emailOrPhone: String,
    val passwordHash: String,
    val role: String = "MEMBER", // "ADMIN" or "MEMBER"
    val status: String = "pending", // "pending", "approved", "rejected"
    val linkedMemberId: Long? = null,
    val linkedMemberName: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isSuperAdmin: Boolean
        get() = emailOrPhone.trim().equals("shamraizkalas@gmail.com", ignoreCase = true) ||
                emailOrPhone.trim().equals("admin", ignoreCase = true) ||
                role.equals("SUPER_ADMIN", ignoreCase = true)

    val isAdmin: Boolean get() = role.equals("ADMIN", ignoreCase = true) || isSuperAdmin
    val isApproved: Boolean get() = status.equals("approved", ignoreCase = true) || isAdmin
    val isPending: Boolean get() = status.equals("pending", ignoreCase = true) && !isAdmin
}

