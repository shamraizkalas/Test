package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firestoreId: String? = null,
    val title: String,
    val content: String,
    val authorName: String,
    val authorEmailOrPhone: String = "",
    val authorRole: String = "ADMIN", // "ADMIN" or "SUPER_ADMIN"
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
