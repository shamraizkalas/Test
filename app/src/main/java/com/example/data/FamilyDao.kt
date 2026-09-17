package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyDao {
    @Query("SELECT * FROM family_members ORDER BY generation ASC, displayOrder ASC, id ASC")
    fun getAllMembersFlow(): Flow<List<FamilyMember>>

    @Query("SELECT * FROM family_members ORDER BY generation ASC, displayOrder ASC, id ASC")
    suspend fun getAllMembers(): List<FamilyMember>

    @Query("SELECT * FROM family_members WHERE fatherId IS NULL ORDER BY displayOrder ASC, id ASC")
    fun getRootMembersFlow(): Flow<List<FamilyMember>>

    @Query("SELECT * FROM family_members WHERE fatherId = :parentId ORDER BY displayOrder ASC, id ASC")
    fun getChildrenOfFlow(parentId: Long): Flow<List<FamilyMember>>

    @Query("SELECT * FROM family_members WHERE fatherId = :parentId ORDER BY displayOrder ASC, id ASC")
    suspend fun getChildrenOf(parentId: Long): List<FamilyMember>

    @Query("SELECT * FROM family_members WHERE id = :id LIMIT 1")
    suspend fun getMemberById(id: Long): FamilyMember?

    @Query("""
        SELECT * FROM family_members 
        WHERE name LIKE '%' || :query || '%' 
           OR fatherName LIKE '%' || :query || '%' 
           OR spouse LIKE '%' || :query || '%' 
           OR location LIKE '%' || :query || '%' 
           OR notes LIKE '%' || :query || '%'
           OR phone LIKE '%' || :query || '%'
           OR occupation LIKE '%' || :query || '%'
        ORDER BY generation ASC, displayOrder ASC
    """)
    fun searchMembers(query: String): Flow<List<FamilyMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: FamilyMember): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(member: FamilyMember): Long = insert(member)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<FamilyMember>)

    @Update
    suspend fun update(member: FamilyMember)

    @Delete
    suspend fun delete(member: FamilyMember)

    @Query("DELETE FROM family_members")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM family_members")
    suspend fun getCount(): Int
}
