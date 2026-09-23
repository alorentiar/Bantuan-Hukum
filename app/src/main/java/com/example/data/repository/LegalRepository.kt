package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.importer.LegalDataImporter
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BabEntity
import com.example.data.local.entity.HukumSearchFts
import com.example.data.local.entity.PeraturanEntity
import com.example.data.local.model.PasalWithDetails
import com.example.data.local.model.SearchResultItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LegalRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context)
) {
    companion object {
        private const val TAG = "LegalRepository"
    }

    private val importer = LegalDataImporter(context, database)

    suspend fun initializeDatabase(): Boolean {
        return importer.importIfNeeded()
    }

    fun getAllPeraturan(): Flow<List<PeraturanEntity>> =
        database.peraturanDao().getAllPeraturan()

    fun getPeraturanByJenis(jenis: String): Flow<List<PeraturanEntity>> =
        database.peraturanDao().getPeraturanByJenis(jenis)

    fun getPeraturanById(id: Long): Flow<PeraturanEntity?> =
        database.peraturanDao().getPeraturanById(id)

    suspend fun getPeraturanByIdSync(id: Long): PeraturanEntity? =
        database.peraturanDao().getPeraturanByIdSync(id)

    fun getBabsByPeraturan(peraturanId: Long): Flow<List<BabEntity>> =
        database.babDao().getBabsByPeraturan(peraturanId)

    fun getPasalByBab(babId: Long) =
        database.pasalDao().getPasalByBab(babId)

    fun getPasalWithDetails(pasalId: Long): Flow<PasalWithDetails?> =
        database.pasalDao().getPasalWithDetails(pasalId)

    suspend fun getPasalWithDetailsSync(pasalId: Long): PasalWithDetails? =
        database.pasalDao().getPasalWithDetailsSync(pasalId)

    suspend fun getAllPasalWithDetailsByPeraturan(peraturanId: Long): List<PasalWithDetails> =
        database.pasalDao().getAllPasalWithDetailsByPeraturan(peraturanId)

    fun getBookmarkedPasals(): Flow<List<PasalWithDetails>> =
        database.pasalDao().getBookmarkedPasal()

    suspend fun toggleBookmark(pasalId: Long, currentStatus: Boolean) = withContext(Dispatchers.IO) {
        database.pasalDao().updateBookmarkStatus(pasalId, !currentStatus)
    }

    /**
     * Pencarian kata kunci offline berbasis Room FTS.
     * Membersihkan query untuk sintaks FTS4/5 MATCH (menghindari crash jika ada karakter khusus),
     * mendukung wildcard prefiks (*), dan jika FTS menemukan 0 hasil atau gagal,
     * secara otomatis melakukan fallback LIKE query.
     */
    suspend fun searchLaws(
        rawQuery: String,
        selectedCategory: String? = null
    ): List<SearchResultItem> = withContext(Dispatchers.IO) {
        val trimmed = rawQuery.trim()
        if (trimmed.isEmpty()) return@withContext emptyList()

        // Siapkan sanitasi FTS query
        // Pecah kata-kata dan tambahkan wildcard '*' untuk pencarian parsial cepat
        val sanitizedTokens = trimmed
            .replace(Regex("""["'*^$#@!~]"""), " ")
            .split(Regex("""\s+"""))
            .filter { it.isNotBlank() }

        if (sanitizedTokens.isEmpty()) return@withContext emptyList()

        // Buat FTS query: token1* token2* (AND matching antar kata)
        val ftsExpression = sanitizedTokens.joinToString(" ") { "$it*" }

        val ftsResults: List<HukumSearchFts> = try {
            if (selectedCategory != null && selectedCategory.isNotEmpty() && selectedCategory != "SEMUA") {
                database.hukumSearchDao().searchFtsWithCategory(ftsExpression, selectedCategory, limit = 80)
            } else {
                database.hukumSearchDao().searchFts(ftsExpression, limit = 80)
            }
        } catch (e: Exception) {
            Log.w(TAG, "FTS query '$ftsExpression' failed, attempting fallback LIKE search", e)
            emptyList()
        }

        // Cari juga via fallback LIKE query untuk mencakup semua kalimat dengan kata kunci (misal 'perkawinan')
        val fallbackWord = sanitizedTokens.firstOrNull() ?: trimmed
        val rawLikeList = try {
            database.hukumSearchDao().searchFallback(fallbackWord, limit = 60)
        } catch (e: Exception) {
            emptyList()
        }

        val filteredLikeList = if (selectedCategory != null && selectedCategory.isNotEmpty() && selectedCategory != "SEMUA") {
            rawLikeList.filter { it.jenisPeraturan.equals(selectedCategory, ignoreCase = true) }
        } else {
            rawLikeList
        }

        // Gabungkan hasil FTS dan LIKE tanpa duplikasi
        val results = (ftsResults + filteredLikeList).distinctBy { it.rowid }

        // Mapping ke SearchResultItem dengan cuplikan (snippet) kalimat utuh yang memuat kata kunci
        results.map { fts ->
            val snippet = generateSnippet(fts.konten, sanitizedTokens)
            SearchResultItem(
                rowid = fts.rowid,
                peraturanId = fts.peraturanId,
                babId = fts.babId,
                pasalId = fts.pasalId,
                ayatId = fts.ayatId,
                jenisPeraturan = fts.jenisPeraturan,
                judulPeraturan = fts.judulPeraturan,
                namaBab = fts.namaBab,
                nomorPasal = fts.nomorPasal,
                tipeElemen = fts.tipeElemen,
                labelAyat = fts.labelAyat,
                konten = fts.konten,
                matchedSnippet = snippet
            )
        }
    }

    /**
     * Mengekstrak kalimat lengkap yang memuat kata kunci pencarian.
     * Contoh: Jika mencari 'perkawinan', akan mengekstrak seluruh kalimat yang memuat kata 'perkawinan'.
     */
    private fun generateSnippet(text: String, keywords: List<String>): String {
        if (text.length <= 160) return text

        var firstIndex = -1
        var matchedKeywordLen = 0
        for (kw in keywords) {
            val idx = text.indexOf(kw, ignoreCase = true)
            if (idx != -1 && (firstIndex == -1 || idx < firstIndex)) {
                firstIndex = idx
                matchedKeywordLen = kw.length
            }
        }

        if (firstIndex == -1) {
            return text.take(160) + "..."
        }

        // Cari batas awal kalimat (tanda titik, tanda seru, tanya, titik dua, atau baris baru sebelumnya)
        val searchStart = (firstIndex - 1).coerceAtLeast(0)
        val prevBound = text.lastIndexOfAny(charArrayOf('.', '!', '?', ';', '\n', ':'), searchStart)
        val start = if (prevBound != -1) {
            (prevBound + 1).coerceAtMost(text.length)
        } else {
            (firstIndex - 50).coerceAtLeast(0)
        }

        // Cari batas akhir kalimat (tanda titik, tanda seru, tanya, titik dua, atau baris baru sesudahnya)
        val searchEnd = (firstIndex + matchedKeywordLen).coerceAtMost(text.length - 1)
        val nextBound = text.indexOfAny(charArrayOf('.', '!', '?', ';', '\n'), searchEnd)
        val end = if (nextBound != -1) {
            (nextBound + 1).coerceAtMost(text.length)
        } else {
            (firstIndex + 140).coerceAtMost(text.length)
        }

        val extracted = text.substring(start, end).trim()
        val prefix = if (start > 0 && !extracted.startsWith(".")) "... " else ""
        val suffix = if (end < text.length && !extracted.endsWith(".")) " ..." else ""
        return (prefix + extracted + suffix).trim()
    }
}
