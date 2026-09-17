package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingRequestDao {
    @Query("SELECT * FROM pending_requests ORDER BY submittedAt DESC")
    fun getAllRequestsFlow(): Flow<List<PendingMemberRequest>>

    @Query("SELECT * FROM pending_requests ORDER BY submittedAt DESC")
    suspend fun getAllRequests(): List<PendingMemberRequest>

    @Query("SELECT * FROM pending_requests WHERE status = 'PENDING' ORDER BY submittedAt ASC")
    fun getPendingRequestsFlow(): Flow<List<PendingMemberRequest>>

    @Query("SELECT * FROM pending_requests WHERE status = 'PENDING'")
    suspend fun getPendingRequests(): List<PendingMemberRequest>

    @Query("SELECT * FROM pending_requests WHERE submittedByUserId = :userId ORDER BY submittedAt DESC")
    fun getRequestsByUserFlow(userId: Long): Flow<List<PendingMemberRequest>>

    @Query("SELECT COUNT(*) FROM pending_requests WHERE status = 'PENDING'")
    fun getPendingCountFlow(): Flow<Int>

    @Query("SELECT * FROM pending_requests WHERE id = :id LIMIT 1")
    suspend fun getRequestById(id: Long): PendingMemberRequest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: PendingMemberRequest): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(request: PendingMemberRequest): Long = insert(request)

    @Update
    suspend fun update(request: PendingMemberRequest)

    @Delete
    suspend fun delete(request: PendingMemberRequest)

    @Query("DELETE FROM pending_requests WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE pending_requests SET status = :status, reviewedAt = :reviewedAt, rejectionReason = :reason WHERE id = :id")
    suspend fun updateStatus(id: Long, status: RequestStatus, reviewedAt: Long, reason: String?)
}
