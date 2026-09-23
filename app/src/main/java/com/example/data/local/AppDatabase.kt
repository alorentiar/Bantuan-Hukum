package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AyatDao
import com.example.data.local.dao.BabDao
import com.example.data.local.dao.HukumSearchDao
import com.example.data.local.dao.PasalDao
import com.example.data.local.dao.PenjelasanDao
import com.example.data.local.dao.PeraturanDao
import com.example.data.local.entity.AyatEntity
import com.example.data.local.entity.BabEntity
import com.example.data.local.entity.HukumSearchFts
import com.example.data.local.entity.PasalEntity
import com.example.data.local.entity.PenjelasanEntity
import com.example.data.local.entity.PeraturanEntity

@Database(
    entities = [
        PeraturanEntity::class,
        BabEntity::class,
        PasalEntity::class,
        AyatEntity::class,
        PenjelasanEntity::class,
        HukumSearchFts::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun peraturanDao(): PeraturanDao
    abstract fun babDao(): BabDao
    abstract fun pasalDao(): PasalDao
    abstract fun ayatDao(): AyatDao
    abstract fun penjelasanDao(): PenjelasanDao
    abstract fun hukumSearchDao(): HukumSearchDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bantuan_hukumku_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
