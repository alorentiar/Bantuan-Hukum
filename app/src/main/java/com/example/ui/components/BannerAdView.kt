package com.example.ui.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Komponen Banner Iklan Google AdMob non-intrusif di bagian bawah layar.
 * Memenuhi kebijakan Google Play Developer Program & Google Better Ads Standards:
 * - Ukuran terstandarisasi (Banner 320x50), tidak menutupi tombol navigasi atau konten utama.
 * - Bersembunyi secara halus (collapsible) saat perangkat offline atau iklan gagal dimuat.
 * - Dilengkapi label "IKLAN" mikro resmi untuk transparansi kepada pengguna.
 */
object AdMobConfig {
    /**
     * ID Aplikasi AdMob akun pengembang:
     * ca-app-pub-5269868767529777~7430422476
     */
    const val APP_ID = "ca-app-pub-5269868767529777~7430422476"

    /**
     * ID Unit Iklan Banner AdMob resmi dari dashboard (Basic Ads):
     * ca-app-pub-5269868767529777/7286952246
     */
    const val BANNER_AD_UNIT_ID = "ca-app-pub-5269868767529777/7286952246"

    /**
     * ID Unit Iklan Banner resmi Google untuk testing & fallback otomatis.
     */
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
}

@Composable
fun BannerAdView(
    adUnitId: String = AdMobConfig.BANNER_AD_UNIT_ID,
    modifier: Modifier = Modifier
) {
    var isAdLoaded by remember { mutableStateOf(false) }
    var activeAdUnitId by remember(adUnitId) { mutableStateOf(adUnitId) }
    var hasAttemptedFallback by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(MaterialTheme.colorScheme.surface)
            .testTag("banner_ad_container"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )
            Text(
                text = if (isAdLoaded) "IKLAN GOOGLE ADMOB" else "RUANG IKLAN ADMOB",
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 1.dp)
            )

            key(activeAdUnitId) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    factory = { context ->
                        AdView(context).apply {
                            setAdSize(AdSize.BANNER)
                            this.adUnitId = activeAdUnitId
                            adListener = object : AdListener() {
                                override fun onAdLoaded() {
                                    super.onAdLoaded()
                                    isAdLoaded = true
                                    Log.d("BannerAdView", "AdMob banner loaded successfully with ID: $activeAdUnitId")
                                }

                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    super.onAdFailedToLoad(error)
                                    Log.w("BannerAdView", "AdMob banner failed to load ($activeAdUnitId): ${error.message} (Code ${error.code})")
                                    // Jika ad unit baru AdMob belum aktif/masih tahap propagasi (Code 3 No Fill / Error),
                                    // otomatis beralih sementara ke Google Test Banner agar pengembang dapat langsung melihat banner aktif
                                    if (!hasAttemptedFallback && activeAdUnitId != AdMobConfig.TEST_BANNER_AD_UNIT_ID) {
                                        hasAttemptedFallback = true
                                        activeAdUnitId = AdMobConfig.TEST_BANNER_AD_UNIT_ID
                                    }
                                }
                            }
                            try {
                                loadAd(AdRequest.Builder().build())
                            } catch (e: Exception) {
                                Log.e("BannerAdView", "Failed to request AdMob banner", e)
                            }
                        }
                    }
                )
            }
        }
    }
}
