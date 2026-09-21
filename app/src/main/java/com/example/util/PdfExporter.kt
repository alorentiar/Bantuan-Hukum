package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.local.model.PasalWithDetails
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilitas untuk mengekspor pasal dan rujukan peraturan ke dokumen PDF resmi
 * untuk keperluan pengarsipan pribadi pengguna sesuai instruksi nomor 7.
 */
object PdfExporter {

    private const val TAG = "PdfExporter"
    private const val PAGE_WIDTH = 595   // Standard A4 width in points (72 dpi)
    private const val PAGE_HEIGHT = 842  // Standard A4 height in points
    private const val MARGIN = 40f

    fun exportPasalToPdf(context: Context, pasalDetails: PasalWithDetails): File? {
        val document = PdfDocument()

        try {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val textPaint = TextPaint().apply {
                isAntiAlias = true
                color = Color.rgb(20, 30, 45)
                textSize = 11f
            }

            val titlePaint = TextPaint().apply {
                isAntiAlias = true
                color = Color.rgb(13, 27, 42)
                textSize = 15f
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            }

            val headerSubPaint = TextPaint().apply {
                isAntiAlias = true
                color = Color.rgb(184, 134, 11) // Warm gold
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val labelPaint = TextPaint().apply {
                isAntiAlias = true
                color = Color.rgb(30, 58, 95)
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val disclaimerPaint = TextPaint().apply {
                isAntiAlias = true
                color = Color.rgb(110, 115, 125)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            }

            val linePaint = Paint().apply {
                color = Color.rgb(200, 205, 215)
                strokeWidth = 1f
            }

            val goldLinePaint = Paint().apply {
                color = Color.rgb(212, 175, 55)
                strokeWidth = 2.5f
            }

            var currentY = MARGIN

            // 1. Kop Dokumen
            canvas.drawText("BANTUAN HUKUMKU", MARGIN, currentY + 12f, titlePaint)
            currentY += 18f
            canvas.drawText("REPOSITORI PERATURAN PERUNDANG-UNDANGAN INDONESIA (100% OFFLINE)", MARGIN, currentY + 10f, headerSubPaint)
            currentY += 18f

            canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, goldLinePaint)
            currentY += 14f

            // 2. Info Peraturan
            val peraturan = pasalDetails.peraturan
            canvas.drawText("${peraturan.judul} (${peraturan.nomor})", MARGIN, currentY + 10f, labelPaint)
            currentY += 16f
            canvas.drawText("Tentang: ${peraturan.tentang}", MARGIN, currentY + 10f, textPaint)
            currentY += 15f
            canvas.drawText("Status: ${peraturan.status} | Penetapan: ${peraturan.tanggalPenetapan}", MARGIN, currentY + 10f, disclaimerPaint)
            currentY += 18f

            canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)
            currentY += 14f

            // 3. Bab dan Pasal
            val bab = pasalDetails.bab
            val pasal = pasalDetails.pasal
            canvas.drawText("${bab.nomorBab}: ${bab.judulBab}", MARGIN, currentY + 10f, headerSubPaint)
            currentY += 18f

            val pasalTitle = if (!pasal.judulPasal.isNullOrBlank()) "${pasal.nomorPasal} - ${pasal.judulPasal}" else pasal.nomorPasal
            canvas.drawText(pasalTitle, MARGIN, currentY + 12f, titlePaint)
            currentY += 24f

            // 4. Ayat-Ayat
            val contentWidth = (PAGE_WIDTH - (MARGIN * 2)).toInt()

            for (ayat in pasalDetails.ayatList) {
                if (currentY > PAGE_HEIGHT - 120f) break // Prevent overflow on single sheet

                val ayatHeader = if (ayat.nomorAyat > 0) "Ayat ${ayat.labelAyat}:" else "Isi Pasal:"
                canvas.drawText(ayatHeader, MARGIN, currentY + 10f, labelPaint)
                currentY += 14f

                val layout = createStaticLayout(ayat.isiAyat, textPaint, contentWidth)
                canvas.save()
                canvas.translate(MARGIN, currentY)
                layout.draw(canvas)
                canvas.restore()
                currentY += layout.height + 12f
            }

            // 5. Penjelasan Resmi
            if (pasalDetails.penjelasanList.isNotEmpty() && currentY < PAGE_HEIGHT - 140f) {
                canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)
                currentY += 12f
                canvas.drawText("PENJELASAN RESMI", MARGIN, currentY + 10f, labelPaint)
                currentY += 16f

                for (penjelasan in pasalDetails.penjelasanList) {
                    if (currentY > PAGE_HEIGHT - 100f) break
                    val fullPenjelasanText = "${penjelasan.nomorPenjelasan}: ${penjelasan.isiPenjelasan}"
                    val pLayout = createStaticLayout(fullPenjelasanText, textPaint, contentWidth)
                    canvas.save()
                    canvas.translate(MARGIN, currentY)
                    pLayout.draw(canvas)
                    canvas.restore()
                    currentY += pLayout.height + 8f
                }
            }

            // 6. Disclaimer & Footer
            val footerY = PAGE_HEIGHT - 55f
            canvas.drawLine(MARGIN, footerY, PAGE_WIDTH - MARGIN, footerY, linePaint)
            val sdf = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID"))
            val timestamp = sdf.format(Date())

            val disclaimerText = "Disclaimer Hukum: Dokumen ini dicetak pada $timestamp melalui aplikasi Bantuan Hukumku untuk keperluan pengarsipan pribadi. Seluruh isi pasal bersumber dari perundang-undangan resmi RI. Dokumen ini bukan pengganti nasihat advokat berlisensi."
            val disclaimerLayout = createStaticLayout(disclaimerText, disclaimerPaint, contentWidth)
            canvas.save()
            canvas.translate(MARGIN, footerY + 8f)
            disclaimerLayout.draw(canvas)
            canvas.restore()

            document.finishPage(page)

            // Simpan ke file
            val outputDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val cleanPasalName = pasal.nomorPasal.replace(" ", "_")
            val fileName = "Arsip_${cleanPasalName}_${System.currentTimeMillis()}.pdf"
            val outputFile = File(outputDir, fileName)

            FileOutputStream(outputFile).use { out ->
                document.writeTo(out)
            }

            return outputFile
        } catch (e: Exception) {
            Log.e(TAG, "Gagal membuat dokumen PDF", e)
            return null
        } finally {
            document.close()
        }
    }

    private fun createStaticLayout(text: String, paint: TextPaint, width: Int): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(2f, 1.15f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                text,
                paint,
                width,
                Layout.Alignment.ALIGN_NORMAL,
                1.15f,
                2f,
                false
            )
        }
    }

    fun sharePdf(context: Context, pdfFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Arsip Hukum: ${pdfFile.name}")
            putExtra(Intent.EXTRA_TEXT, "Berikut adalah lampiran arsip pasal peraturan perundang-undangan dari aplikasi Bantuan Hukumku.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Bagikan / Arsipkan Dokumen PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun openPdf(context: Context, pdfFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(openIntent)
        } catch (e: Exception) {
            // Jika tidak ada pembuka PDF langsung, tampilkan dialog share
            sharePdf(context, pdfFile)
        }
    }
}
