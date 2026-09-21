package com.example.data.importer

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LegalDatabaseJson(
    val peraturanList: List<PeraturanJson>
)

@JsonClass(generateAdapter = true)
data class PeraturanJson(
    val jenis: String,
    val nomor: String,
    val tahun: Int,
    val judul: String,
    val tentang: String,
    val tanggalPenetapan: String,
    val status: String,
    val urutanHierarki: Int,
    val deskripsiSingkat: String,
    val babs: List<BabJson>
)

@JsonClass(generateAdapter = true)
data class BabJson(
    val nomorBab: String,
    val judulBab: String,
    val urutan: Int,
    val pasals: List<PasalJson>
)

@JsonClass(generateAdapter = true)
data class PasalJson(
    val nomorPasal: String,
    val judulPasal: String? = null,
    val urutan: Int,
    val ayats: List<AyatJson>,
    val penjelasans: List<PenjelasanJson> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AyatJson(
    val nomorAyat: Int,
    val labelAyat: String,
    val isiAyat: String
)

@JsonClass(generateAdapter = true)
data class PenjelasanJson(
    val nomorPenjelasan: String,
    val isiPenjelasan: String
)
