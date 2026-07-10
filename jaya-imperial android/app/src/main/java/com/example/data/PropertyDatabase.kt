package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [HousingUnit::class, SalesLog::class, User::class, SalesTeam::class, SoldProposal::class, NotificationEntity::class, GimmickRequest::class, AttendanceEntity::class], version = 15, exportSchema = false)
abstract class PropertyDatabase : RoomDatabase() {
    abstract fun propertyDao(): PropertyDao

    companion object {
        @Volatile
        private var INSTANCE: PropertyDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): PropertyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PropertyDatabase::class.java,
                    "jaya_imperial_park_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance

                // Perbaikan: Memastikan akun tersedia setiap kali database dibuka
                scope.launch(Dispatchers.IO) {
                    seedRequiredUsers(instance.propertyDao())
                }
                instance
            }
        }

        private suspend fun seedRequiredUsers(dao: PropertyDao) {
            val seedUsers = listOf(
                User(username = "superadmin@jip.com", name = "Super Admin", role = "Super Admin", pin = "123456")
            )

            for (user in seedUsers) {
                dao.insertUser(user)
            }
        }
    }
}
