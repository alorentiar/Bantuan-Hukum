package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

/**
 * Tabel virtual Full-Text Search (FTS) di Room SQLite untuk korpus hukum Indonesia.
 * Mengindeks seluruh elemen teks normatif (Ayat, Penjelasan, Pasal, Bab)
 * agar pencarian offline bekerja secara instan tanpa konsumsi CPU/baterai berlebih.
 */
@Entity(tableName = "hukum_search_fts")
@Fts4
data class HukumSearchFts(
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    val rowid: Int,
    val peraturanId: Long,
    val babId: Long,
    val pasalId: Long,
    val ayatId: Long,
    val jenisPeraturan: String,
    val judulPeraturan: String,
    val namaBab: String,
    val nomorPasal: String,
    val tipeElemen: String, // "AYAT", "PENJELASAN", "PASAL", "BAB", "PREAMBULE"
    val labelAyat: String,
    val konten: String      // Teks yang diindeks secara inversi (inverted index)
)
