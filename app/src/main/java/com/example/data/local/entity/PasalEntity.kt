package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entitas Pasal dalam suatu Bab dan Peraturan.
 */
@Entity(
    tableName = "pasal",
    foreignKeys = [
        ForeignKey(
            entity = PeraturanEntity::class,
            parentColumns = ["id"],
            childColumns = ["peraturanId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BabEntity::class,
            parentColumns = ["id"],
            childColumns = ["babId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["peraturanId"]),
        Index(value = ["babId"]),
        Index(value = ["nomorPasal"]),
        Index(value = ["isBookmarked"])
    ]
)
data class PasalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val peraturanId: Long,
    val babId: Long,
    val nomorPasal: String,         // Contoh: "Pasal 1", "Pasal 27", "Pasal 28E", "Pasal 33"
    val judulPasal: String? = null,  // Contoh opsional: "Bentuk Negara dan Kedaulatan"
    val urutan: Int,
    val isBookmarked: Boolean = false
)
