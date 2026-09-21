package com.example.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.data.local.entity.AyatEntity
import com.example.data.local.entity.BabEntity
import com.example.data.local.entity.PasalEntity
import com.example.data.local.entity.PenjelasanEntity
import com.example.data.local.entity.PeraturanEntity

/**
 * Model relasional lengkap untuk sebuah pasal:
 * Menggabungkan Pasal, Bab induk, Peraturan induk, daftar Ayat, dan daftar Penjelasan.
 */
data class PasalWithDetails(
    @Embedded
    val pasal: PasalEntity,

    @Relation(
        parentColumn = "peraturanId",
        entityColumn = "id"
    )
    val peraturan: PeraturanEntity,

    @Relation(
        parentColumn = "babId",
        entityColumn = "id"
    )
    val bab: BabEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "pasalId"
    )
    val ayatList: List<AyatEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "pasalId"
    )
    val penjelasanList: List<PenjelasanEntity>
)
