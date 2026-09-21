package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.PasalEntity
import com.example.data.local.model.PasalWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface PasalDao {
    @Query("SELECT * FROM pasal WHERE babId = :babId ORDER BY urutan ASC")
    fun getPasalByBab(babId: Long): Flow<List<PasalEntity>>

    @Query("SELECT * FROM pasal WHERE peraturanId = :peraturanId ORDER BY urutan ASC")
    fun getPasalByPeraturan(peraturanId: Long): Flow<List<PasalEntity>>

    @Transaction
    @Query("SELECT * FROM pasal WHERE id = :pasalId LIMIT 1")
    fun getPasalWithDetails(pasalId: Long): Flow<PasalWithDetails?>

    @Transaction
    @Query("SELECT * FROM pasal WHERE id = :pasalId LIMIT 1")
    suspend fun getPasalWithDetailsSync(pasalId: Long): PasalWithDetails?

    @Transaction
    @Query("SELECT * FROM pasal WHERE peraturanId = :peraturanId ORDER BY urutan ASC")
    suspend fun getAllPasalWithDetailsByPeraturan(peraturanId: Long): List<PasalWithDetails>

    @Transaction
    @Query("SELECT * FROM pasal WHERE isBookmarked = 1 ORDER BY id DESC")
    fun getBookmarkedPasal(): Flow<List<PasalWithDetails>>

    @Query("UPDATE pasal SET isBookmarked = :isBookmarked WHERE id = :pasalId")
    suspend fun updateBookmarkStatus(pasalId: Long, isBookmarked: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pasal: PasalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pasals: List<PasalEntity>): List<Long>
}
