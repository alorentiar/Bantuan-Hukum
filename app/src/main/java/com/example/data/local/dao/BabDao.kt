package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.BabEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BabDao {
    @Query("SELECT * FROM bab WHERE peraturanId = :peraturanId ORDER BY urutan ASC")
    fun getBabsByPeraturan(peraturanId: Long): Flow<List<BabEntity>>

    @Query("SELECT * FROM bab WHERE id = :id LIMIT 1")
    suspend fun getBabById(id: Long): BabEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bab: BabEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(babs: List<BabEntity>): List<Long>
}
