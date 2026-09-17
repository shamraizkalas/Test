package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun toGender(value: String): Gender = try {
        enumValueOf<Gender>(value)
    } catch (e: Exception) {
        Gender.MALE
    }

    @TypeConverter
    fun fromGender(gender: Gender): String = gender.name

    @TypeConverter
    fun toRequestStatus(value: String): RequestStatus = try {
        enumValueOf<RequestStatus>(value)
    } catch (e: Exception) {
        RequestStatus.PENDING
    }

    @TypeConverter
    fun fromRequestStatus(status: RequestStatus): String = status.name
}

@Database(entities = [FamilyMember::class, UserAccount::class, PendingMemberRequest::class, Announcement::class], version = 6, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun familyDao(): FamilyDao
    abstract fun userDao(): UserDao
    abstract fun pendingRequestDao(): PendingRequestDao
    abstract fun announcementDao(): AnnouncementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shajra_family_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
