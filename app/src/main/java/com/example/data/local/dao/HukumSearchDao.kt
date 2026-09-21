package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.HukumSearchFts

@Dao
interface HukumSearchDao {

    /**
     * Pencarian berkecepatan tinggi menggunakan FTS MATCH (Inverted Index).
     * Kolom 'rowid' secara eksplisit disertakan karena SQLite FTS tidak menyertakan rowid pada 'SELECT *'.
     */
    @Query("""
        SELECT rowid, * FROM hukum_search_fts 
        WHERE hukum_search_fts MATCH :query 
        LIMIT :limit
    """)
    suspend fun searchFts(query: String, limit: Int = 100): List<HukumSearchFts>

    /**
     * Pencarian dengan filter jenis peraturan (misal hanya UUD 1945 atau UU)
     */
    @Query("""
        SELECT rowid, * FROM hukum_search_fts 
        WHERE hukum_search_fts MATCH :query AND jenisPeraturan = :jenis
        LIMIT :limit
    """)
    suspend fun searchFtsWithCategory(query: String, jenis: String, limit: Int = 100): List<HukumSearchFts>

    /**
     * Fallback pencarian bila query pengguna memiliki karakter khusus yang tidak kompatibel
     * dengan sintaks ekspresi FTS.
     */
    @Query("""
        SELECT rowid, * FROM hukum_search_fts 
        WHERE konten LIKE '%' || :keyword || '%' 
           OR nomorPasal LIKE '%' || :keyword || '%' 
           OR judulPeraturan LIKE '%' || :keyword || '%'
        LIMIT :limit
    """)
    suspend fun searchFallback(keyword: String, limit: Int = 100): List<HukumSearchFts>

    @Query("SELECT COUNT(*) FROM hukum_search_fts")
    suspend fun getIndexCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIndex(item: HukumSearchFts): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllIndex(items: List<HukumSearchFts>)

    @Query("DELETE FROM hukum_search_fts")
    suspend fun clearIndex()
}
