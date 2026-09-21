package com.example.data.local.model

/**
 * Model item hasil pencarian kata kunci hukum offline
 */
data class SearchResultItem(
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
    val konten: String,
    val matchedSnippet: String = "",
    val isBookmarked: Boolean = false
)

/**
 * Tata urutan peraturan perundang-undangan RI menurut UU No. 12 Tahun 2011 Pasal 7
 */
enum class HierarchyLevel(
    val code: String,
    val levelNumber: Int,
    val displayName: String,
    val shortName: String,
    val description: String
) {
    UUD_1945(
        code = "UUD_1945",
        levelNumber = 1,
        displayName = "Undang-Undang Dasar Negara Republik Indonesia Tahun 1945",
        shortName = "UUD 1945",
        description = "Hukum dasar tertulis tertinggi di Negara Kesatuan Republik Indonesia."
    ),
    TAP_MPR(
        code = "TAP_MPR",
        levelNumber = 2,
        displayName = "Ketetapan Majelis Permusyawaratan Rakyat",
        shortName = "TAP MPR",
        description = "Ketetapan MPR yang masih berlaku sebagaimana diatur dalam Ketetapan MPR RI No. I/MPR/2003."
    ),
    UU_PERPPU(
        code = "UU",
        levelNumber = 3,
        displayName = "Undang-Undang / Peraturan Pemerintah Pengganti Undang-Undang",
        shortName = "UU / Perppu",
        description = "Peraturan perundang-undangan yang dibentuk DPR dengan persetujuan bersama Presiden."
    ),
    PP(
        code = "PP",
        levelNumber = 4,
        displayName = "Peraturan Pemerintah",
        shortName = "PP",
        description = "Peraturan yang ditetapkan Presiden untuk menjalankan Undang-Undang sebagaimana mestinya."
    ),
    PERPRES(
        code = "PERPRES",
        levelNumber = 5,
        displayName = "Peraturan Presiden",
        shortName = "Perpres",
        description = "Peraturan yang ditetapkan oleh Presiden untuk menjalankan materi yang diperintahkan UU atau PP."
    ),
    PERDA_PROV(
        code = "PERDA_PROV",
        levelNumber = 6,
        displayName = "Peraturan Daerah Provinsi",
        shortName = "Perda Provinsi",
        description = "Peraturan yang dibentuk DPRD Provinsi dengan persetujuan bersama Gubernur."
    ),
    PERDA_KAB(
        code = "PERDA_KAB",
        levelNumber = 7,
        displayName = "Peraturan Daerah Kabupaten/Kota",
        shortName = "Perda Kab/Kota",
        description = "Peraturan yang dibentuk DPRD Kabupaten/Kota dengan persetujuan bersama Bupati/Walikota."
    );

    companion object {
        fun fromCode(code: String): HierarchyLevel {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: UU_PERPPU
        }
    }
}
