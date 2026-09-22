package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.LegalRepository
import com.example.ui.LegalAppUiState
import com.example.ui.LegalAppViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.LegalTopBar
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.BrowseHierarchyScreen
import com.example.ui.screens.LegalGuideScreen
import com.example.ui.screens.PasalDetailScreen
import com.example.ui.screens.PeraturanDetailScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.MyApplicationTheme

import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inisialisasi Google Mobile Ads SDK secara asynchronous di latar belakang
        CoroutineScope(Dispatchers.IO).launch {
            try {
                MobileAds.initialize(this@MainActivity) {}
            } catch (e: Exception) {
                // Graceful fallback jika Google Play Services belum siap
            }
        }

        setContent {
            MyApplicationTheme {
                BantuanHukumApp()
            }
        }
    }
}

@Composable
fun BantuanHukumApp() {
    val context = LocalContext.current
    val repository = remember { LegalRepository(context) }
    val viewModel: LegalAppViewModel = viewModel { LegalAppViewModel(repository) }
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userNotice) {
        uiState.userNotice?.let { notice ->
            Toast.makeText(context, notice, Toast.LENGTH_SHORT).show()
            viewModel.clearNotice()
        }
    }

    // Intercept back button for nested screens
    BackHandler(enabled = uiState.currentScreen == ScreenDestination.PASAL_DETAIL || uiState.selectedPeraturan != null) {
        if (uiState.currentScreen == ScreenDestination.PASAL_DETAIL) {
            viewModel.navigateTo(ScreenDestination.SEARCH)
        } else if (uiState.selectedPeraturan != null) {
            viewModel.onSearchQueryChanged(uiState.searchQuery)
            // clear selectedPeraturan
            viewModel.navigateTo(uiState.currentScreen)
        }
    }

    Scaffold(
        topBar = {
            if (uiState.currentScreen != ScreenDestination.PASAL_DETAIL && uiState.selectedPeraturan == null) {
                LegalTopBar(
                    onInfoClick = { viewModel.setDisclaimerDialogVisible(true) }
                )
            }
        },
        bottomBar = {
            if (uiState.currentScreen != ScreenDestination.PASAL_DETAIL && uiState.selectedPeraturan == null) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Iklan Banner Google AdMob non-intrusif di atas bottom navigation
                    com.example.ui.components.BannerAdView()

                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp,
                        modifier = Modifier.testTag("main_bottom_nav")
                    ) {
                        val navItems = listOf(
                            Triple(ScreenDestination.SEARCH, "Cari", Icons.Default.Search),
                            Triple(ScreenDestination.HIERARCHY, "Hierarki", Icons.Default.AccountTree),
                            Triple(ScreenDestination.BOOKMARKS, "Tersimpan", Icons.Default.Bookmark),
                            Triple(ScreenDestination.GUIDE, "Panduan", Icons.Default.MenuBook)
                        )

                        navItems.forEach { (destination, label, icon) ->
                            val isSelected = uiState.currentScreen == destination
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.navigateTo(destination) },
                                icon = { Icon(imageVector = icon, contentDescription = label) },
                                label = { Text(label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        when {
            uiState.currentScreen == ScreenDestination.PASAL_DETAIL -> {
                PasalDetailScreen(
                    uiState = uiState,
                    onBackClick = { viewModel.navigateTo(ScreenDestination.SEARCH) },
                    onToggleBookmark = { id, current -> viewModel.toggleBookmark(id, current) },
                    onExportPdf = { ctx -> viewModel.exportCurrentPasalToPdf(ctx) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            uiState.selectedPeraturan != null -> {
                PeraturanDetailScreen(
                    peraturan = uiState.selectedPeraturan!!,
                    babs = uiState.selectedPeraturanBabs,
                    onBackClick = {
                        // Clear selected regulation
                        viewModel.onCategorySelected("SEMUA")
                    },
                    onSelectPasal = { id -> viewModel.selectPasal(id) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            uiState.currentScreen == ScreenDestination.SEARCH -> {
                SearchScreen(
                    uiState = uiState,
                    onQueryChange = { query -> viewModel.onSearchQueryChanged(query) },
                    onCategoryChange = { cat -> viewModel.onCategorySelected(cat) },
                    onSelectPasal = { id -> viewModel.selectPasal(id) },
                    onSelectPeraturan = { reg -> viewModel.selectPeraturan(reg) },
                    onQuickSearchKeyword = { kw -> viewModel.onSearchQueryChanged(kw) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            uiState.currentScreen == ScreenDestination.HIERARCHY -> {
                BrowseHierarchyScreen(
                    uiState = uiState,
                    onSelectPeraturan = { reg -> viewModel.selectPeraturan(reg) },
                    onSelectPasal = { id -> viewModel.selectPasal(id) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            uiState.currentScreen == ScreenDestination.BOOKMARKS -> {
                BookmarksScreen(
                    uiState = uiState,
                    onSelectPasal = { id -> viewModel.selectPasal(id) },
                    onToggleBookmark = { id, current -> viewModel.toggleBookmark(id, current) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            uiState.currentScreen == ScreenDestination.GUIDE -> {
                LegalGuideScreen(
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        // Info / Mandatory Legal Disclaimer Dialog
        if (uiState.showDisclaimerDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.setDisclaimerDialogVisible(false) },
                title = {
                    Text(
                        text = "Tentang Bantuan Hukumku",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Aplikasi Bantuan Hukumku adalah basis data peraturan perundang-undangan Indonesia (UUD 1945, UU, PP, dsb.) yang bekerja 100% offline dengan pencarian cepat Full-Text Search (FTS).\n\nDISCLAIMER & SUMBER DATA RESMI:\n1. Hasil pencarian dan rujukan pasal adalah referensi informatif, bukan pengganti nasihat advokat berlisensi.\n2. Aplikasi ini dikembangkan secara independen dan BUKAN merupakan aplikasi resmi pemerintah Republik Indonesia.\n3. Sumber resmi dokumen hukum: Jaringan Dokumentasi dan Informasi Hukum Nasional (JDIHN - jdihn.go.id) & Lembaran Negara RI.\n4. Iklan: Dilengkapi iklan banner Google AdMob non-intrusif guna mendukung keberlanjutan pengembangan aplikasi.",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3f
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.setDisclaimerDialogVisible(false) },
                        modifier = Modifier.testTag("dismiss_disclaimer_button")
                    ) {
                        Text("Mengerti & Setuju")
                    }
                }
            )
        }
    }
}
