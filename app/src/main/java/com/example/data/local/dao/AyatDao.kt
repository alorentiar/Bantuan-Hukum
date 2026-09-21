package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.AyatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AyatDao {
    @Query("SELECT * FROM ayat WHERE pasalId = :pasalId ORDER BY nomorAyat ASC")
    fun getAyatByPasal(pasalId: Long): Flow<List<AyatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ayat: AyatEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ayats: List<AyatEntity>): List<Long>
}
