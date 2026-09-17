package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_accounts WHERE LOWER(emailOrPhone) = LOWER(:identifier) LIMIT 1")
    suspend fun getUserByEmailOrPhone(identifier: String): UserAccount?

    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserAccount?

    @Query("SELECT * FROM user_accounts ORDER BY id ASC")
    fun getAllUsersFlow(): Flow<List<UserAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount): Long

    @Update
    suspend fun updateUser(user: UserAccount)

    @Query("SELECT COUNT(*) FROM user_accounts")
    suspend fun getUserCount(): Int
}
