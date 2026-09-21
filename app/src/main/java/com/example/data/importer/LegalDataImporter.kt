package com.example.data.importer

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AyatEntity
import com.example.data.local.entity.BabEntity
import com.example.data.local.entity.HukumSearchFts
import com.example.data.local.entity.PasalEntity
import com.example.data.local.entity.PenjelasanEntity
import com.example.data.local.entity.PeraturanEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Mekanisme impor data awal (pre-populated database) dari aset JSON
 * ke Room Database dan pembangunan indeks FTS (Full-Text Search).
 */
class LegalDataImporter(
    private val context: Context,
    private val database: AppDatabase
) {
    companion object {
        private const val TAG = "LegalDataImporter"
        private const val JSON_FILE_NAME = "hukum_database.json"
    }

    suspend fun importIfNeeded(): Boolean = withContext(Dispatchers.IO) {
        val count = database.peraturanDao().getCount()
        if (count > 0) {
            val ftsCount = database.hukumSearchDao().getIndexCount()
            if (ftsCount > 0) {
                Log.d(TAG, "Database already populated: $count regulations, $ftsCount FTS records.")
                return@withContext false
            }
        }

        Log.d(TAG, "Starting initial legal database import and FTS indexing...")
        try {
            val jsonString = context.assets.open(JSON_FILE_NAME).bufferedReader().use { it.readText() }
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val adapter = moshi.adapter(LegalDatabaseJson::class.java)
            val data = adapter.fromJson(jsonString) ?: return@withContext false

            val searchIndexList = mutableListOf<HukumSearchFts>()
            var ftsRowId = 1

            for (peraturanJson in data.peraturanList) {
                val peraturanEntity = PeraturanEntity(
                    jenis = peraturanJson.jenis,
                    nomor = peraturanJson.nomor,
                    tahun = peraturanJson.tahun,
                    judul = peraturanJson.judul,
                    tentang = peraturanJson.tentang,
                    tanggalPenetapan = peraturanJson.tanggalPenetapan,
                    status = peraturanJson.status,
                    urutanHierarki = peraturanJson.urutanHierarki,
                    deskripsiSingkat = peraturanJson.deskripsiSingkat
                )
                val peraturanId = database.peraturanDao().insert(peraturanEntity)

                for (babJson in peraturanJson.babs) {
                    val babEntity = BabEntity(
                        peraturanId = peraturanId,
                        nomorBab = babJson.nomorBab,
                        judulBab = babJson.judulBab,
                        urutan = babJson.urutan
                    )
                    val babId = database.babDao().insert(babEntity)

                    for (pasalJson in babJson.pasals) {
                        val pasalEntity = PasalEntity(
                            peraturanId = peraturanId,
                            babId = babId,
                            nomorPasal = pasalJson.nomorPasal,
                            judulPasal = pasalJson.judulPasal,
                            urutan = pasalJson.urutan,
                            isBookmarked = false
                        )
                        val pasalId = database.pasalDao().insert(pasalEntity)

                        // Index Pasal header
                        searchIndexList.add(
                            HukumSearchFts(
                                rowid = ftsRowId++,
                                peraturanId = peraturanId,
                                babId = babId,
                                pasalId = pasalId,
                                ayatId = 0L,
                                jenisPeraturan = peraturanJson.jenis,
                                judulPeraturan = peraturanJson.judul,
                                namaBab = "${babJson.nomorBab}: ${babJson.judulBab}",
                                nomorPasal = pasalJson.nomorPasal,
                                tipeElemen = "PASAL",
                                labelAyat = "Judul",
                                konten = "${pasalJson.nomorPasal} ${pasalJson.judulPasal.orEmpty()} ${babJson.judulBab} ${peraturanJson.tentang}"
                            )
                        )

                        // Insert Ayats (Entity terpisah)
                        for (ayatJson in pasalJson.ayats) {
                            val ayatEntity = AyatEntity(
                                pasalId = pasalId,
                                nomorAyat = ayatJson.nomorAyat,
                                labelAyat = ayatJson.labelAyat,
                                isiAyat = ayatJson.isiAyat
                            )
                            val ayatId = database.ayatDao().insert(ayatEntity)

                            // Index Ayat ke FTS
                            searchIndexList.add(
                                HukumSearchFts(
                                    rowid = ftsRowId++,
                                    peraturanId = peraturanId,
                                    babId = babId,
                                    pasalId = pasalId,
                                    ayatId = ayatId,
                                    jenisPeraturan = peraturanJson.jenis,
                                    judulPeraturan = peraturanJson.judul,
                                    namaBab = "${babJson.nomorBab}: ${babJson.judulBab}",
                                    nomorPasal = pasalJson.nomorPasal,
                                    tipeElemen = "AYAT",
                                    labelAyat = ayatJson.labelAyat,
                                    konten = ayatJson.isiAyat
                                )
                            )
                        }

                        // Insert Penjelasans (Entity terpisah)
                        for (penjelasanJson in pasalJson.penjelasans) {
                            val penjelasanEntity = PenjelasanEntity(
                                pasalId = pasalId,
                                ayatId = null,
                                nomorPenjelasan = penjelasanJson.nomorPenjelasan,
                                isiPenjelasan = penjelasanJson.isiPenjelasan
                            )
                            val penjelasanId = database.penjelasanDao().insert(penjelasanEntity)

                            // Index Penjelasan ke FTS
                            searchIndexList.add(
                                HukumSearchFts(
                                    rowid = ftsRowId++,
                                    peraturanId = peraturanId,
                                    babId = babId,
                                    pasalId = pasalId,
                                    ayatId = penjelasanId,
                                    jenisPeraturan = peraturanJson.jenis,
                                    judulPeraturan = peraturanJson.judul,
                                    namaBab = "${babJson.nomorBab}: ${babJson.judulBab}",
                                    nomorPasal = pasalJson.nomorPasal,
                                    tipeElemen = "PENJELASAN",
                                    labelAyat = "Penjelasan ${penjelasanJson.nomorPenjelasan}",
                                    konten = "${penjelasanJson.nomorPenjelasan}: ${penjelasanJson.isiPenjelasan}"
                                )
                            )
                        }
                    }
                }
            }

            // Batch insert ke FTS Virtual Table
            database.hukumSearchDao().insertAllIndex(searchIndexList)
            Log.d(TAG, "Import completed successfully! Indexed ${searchIndexList.size} elements.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import legal database", e)
            false
        }
    }
}
