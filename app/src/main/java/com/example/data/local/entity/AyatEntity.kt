package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entitas terpisah untuk setiap elemen teks Ayat dalam Pasal.
 * Memisahkan teks ayat ke entitas tersendiri memaksimalkan efisiensi penyimpanan,
 * granularitas referensi hukum, dan pemetaan ke FTS index.
 */
@Entity(
    tableName = "ayat",
    foreignKeys = [
        ForeignKey(
            entity = PasalEntity::class,
            parentColumns = ["id"],
            childColumns = ["pasalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["pasalId"]),
        Index(value = ["nomorAyat"])
    ]
)
data class AyatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pasalId: Long,
    val nomorAyat: Int,       // 1, 2, 3, atau 0 bila pasal tunggal tanpa penomoran ayat
    val labelAyat: String,    // Contoh: "(1)", "(2)", atau "Tunggal"
    val isiAyat: String       // Konten teks normatif ayat hukum
)
