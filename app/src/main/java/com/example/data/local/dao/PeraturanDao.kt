package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PeraturanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeraturanDao {
    @Query("SELECT * FROM peraturan ORDER BY urutanHierarki ASC, tahun DESC")
    fun getAllPeraturan(): Flow<List<PeraturanEntity>>

    @Query("SELECT * FROM peraturan WHERE jenis = :jenis ORDER BY tahun DESC, nomor ASC")
    fun getPeraturanByJenis(jenis: String): Flow<List<PeraturanEntity>>

    @Query("SELECT * FROM peraturan WHERE id = :id LIMIT 1")
    fun getPeraturanById(id: Long): Flow<PeraturanEntity?>

    @Query("SELECT * FROM peraturan WHERE id = :id LIMIT 1")
    suspend fun getPeraturanByIdSync(id: Long): PeraturanEntity?

    @Query("SELECT COUNT(*) FROM peraturan")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PeraturanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PeraturanEntity>): List<Long>
}
