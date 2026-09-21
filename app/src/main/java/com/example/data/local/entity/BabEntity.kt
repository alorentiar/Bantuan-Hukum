package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entitas Bab dalam suatu peraturan hukum.
 */
@Entity(
    tableName = "bab",
    foreignKeys = [
        ForeignKey(
            entity = PeraturanEntity::class,
            parentColumns = ["id"],
            childColumns = ["peraturanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["peraturanId"]),
        Index(value = ["nomorBab"])
    ]
)
data class BabEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val peraturanId: Long,
    val nomorBab: String,    // Contoh: "Bab I", "Bab XA", "Pembukaan", "Aturan Peralihan"
    val judulBab: String,    // Contoh: "Bentuk dan Kedaulatan", "Hak Asasi Manusia"
    val urutan: Int
)
