package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entitas terpisah untuk setiap elemen teks Penjelasan Resmi Pasal/Ayat.
 * Dokumen hukum Indonesia memiliki teks penjelasan resmi (misal: "Cukup jelas",
 * atau penjelasan otentik penafsiran kata).
 */
@Entity(
    tableName = "penjelasan",
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
        Index(value = ["ayatId"])
    ]
)
data class PenjelasanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pasalId: Long,
    val ayatId: Long? = null,
    val nomorPenjelasan: String,   // Contoh: "Umum", "Ayat (1)", "Cukup Jelas"
    val isiPenjelasan: String      // Konten teks penjelasan resmi
)
