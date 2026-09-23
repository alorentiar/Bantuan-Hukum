package com.example.ui.components

import android.util.Log
import android.view.View
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
import com.startapp.sdk.ads.banner.Banner
import com.startapp.sdk.ads.banner.BannerListener

/**
 * Konfigurasi Jaringan Iklan Start.io
 */
object StartIoConfig {
    /**
     * ID Akun / Aplikasi Start.io pengembang dari file app-ads.txt:
     * start.io, 192739336, DIRECT
     */
    const val APP_ID = "192739336"
}

/**
 * Komponen Banner Iklan Eksklusif Start.io:
 * Hanya menampilkan iklan banner resmi dari Start.io (App ID: 192739336).
 * Memiliki tata letak bersih dan responsif di atas bilah navigasi utama.
 */
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier
) {
    var isStartIoLoaded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.surface)
            .testTag("startio_banner_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )

        Text(
            text = if (isStartIoLoaded) "IKLAN START.IO" else "START.IO ADS",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 2.dp, bottom = 1.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("startio_banner_view"),
                factory = { context ->
                    Banner(context).apply {
                        setBannerListener(object : BannerListener {
                            override fun onReceiveAd(banner: View?) {
                                isStartIoLoaded = true
                                Log.d("StartIoAds", "Start.io banner ad received successfully!")
                            }

                            override fun onFailedToReceiveAd(banner: View?) {
                                Log.w("StartIoAds", "Start.io banner ad failed to receive")
                            }

                            override fun onClick(banner: View?) {
                                Log.d("StartIoAds", "Start.io banner clicked")
                            }

                            override fun onImpression(banner: View?) {
                                Log.d("StartIoAds", "Start.io banner impression registered")
                            }
                        })
                    }
                }
            )
        }
    }
}
