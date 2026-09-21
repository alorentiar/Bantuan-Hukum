package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PenjelasanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PenjelasanDao {
    @Query("SELECT * FROM penjelasan WHERE pasalId = :pasalId ORDER BY id ASC")
    fun getPenjelasanByPasal(pasalId: Long): Flow<List<PenjelasanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(penjelasan: PenjelasanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(penjelasans: List<PenjelasanEntity>): List<Long>
}
