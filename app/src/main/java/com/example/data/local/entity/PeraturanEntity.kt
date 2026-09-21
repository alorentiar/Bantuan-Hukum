package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entitas induk peraturan perundang-undangan Indonesia mengacu pada
 * tata urutan UU No. 12 Tahun 2011.
 */
@Entity(
    tableName = "peraturan",
    indices = [
        Index(value = ["jenis"]),
        Index(value = ["nomor"]),
        Index(value = ["tahun"]),
        Index(value = ["urutanHierarki"])
    ]
)
data class PeraturanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val jenis: String,             // UUD_1945, TAP_MPR, UU, PERPPU, PP, PERPRES, PERDA_PROV, PERDA_KAB
    val nomor: String,             // Contoh: "Tahun 1945", "Nomor 12", "Nomor 1"
    val tahun: Int,                // Contoh: 1945, 2011, 2024
    val judul: String,             // Contoh: "Undang-Undang Dasar Negara Republik Indonesia Tahun 1945"
    val tentang: String,           // Contoh: "Pembentukan Peraturan Perundang-undangan"
    val tanggalPenetapan: String,  // Contoh: "18 Agustus 1945"
    val status: String,            // "Berlaku", "Perubahan", "Dicabut"
    val urutanHierarki: Int,       // 1 (UUD) s/d 7 (Perda Kab/Kota)
    val deskripsiSingkat: String
)
