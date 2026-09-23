package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.PeraturanEntity
import com.example.data.local.model.HierarchyLevel
import com.example.data.repository.LegalRepository
import com.example.util.PdfExporter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LegalAppViewModel(
    private val repository: LegalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LegalAppUiState())
    val uiState: StateFlow<LegalAppUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var pasalDetailJob: Job? = null
    private var peraturanBabJob: Job? = null

    init {
        initDatabaseAndData()
    }

    private fun initDatabaseAndData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDbInitializing = true) }
            repository.initializeDatabase()
            _uiState.update { it.copy(isDbInitializing = false) }

            // Observe all regulations
            launch {
                repository.getAllPeraturan().collectLatest { regs ->
                    _uiState.update { it.copy(regulations = regs) }
                }
            }

            // Observe bookmarks
            launch {
                repository.getBookmarkedPasals().collectLatest { bookmarks ->
                    _uiState.update { it.copy(bookmarkedPasals = bookmarks) }
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(180) // Debounce for smooth offline search
            _uiState.update { it.copy(isSearching = true) }
            val category = _uiState.value.selectedCategory
            val results = repository.searchLaws(query, category)
            _uiState.update { it.copy(searchResults = results, isSearching = false) }
        }
    }

    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        val currentQuery = _uiState.value.searchQuery
        if (currentQuery.isNotBlank()) {
            onSearchQueryChanged(currentQuery)
        }
    }

    fun navigateTo(destination: ScreenDestination) {
        pasalDetailJob?.cancel()
        peraturanBabJob?.cancel()
        _uiState.update {
            it.copy(
                currentScreen = destination,
                selectedPeraturan = null,
                selectedPeraturanBabs = emptyList(),
                selectedPasalId = null,
                currentPasalDetails = null
            )
        }
    }

    /**
     * Navigasi kembali yang aman dan andal ke layar sebelumnya atau halaman utama (SEARCH).
     */
    fun navigateBack() {
        pasalDetailJob?.cancel()
        _uiState.update { state ->
            when {
                // Jika sedang di detail pasal, kembali ke layar asal (bisa SEARCH, HIERARCHY, BOOKMARKS, atau detail Peraturan)
                state.currentScreen == ScreenDestination.PASAL_DETAIL -> {
                    state.copy(
                        currentScreen = state.previousScreen,
                        selectedPasalId = null,
                        currentPasalDetails = null
                    )
                }
                // Jika sedang melihat rincian suatu Peraturan, tutup detail peraturan dan kembali ke daftar
                state.selectedPeraturan != null -> {
                    peraturanBabJob?.cancel()
                    state.copy(
                        selectedPeraturan = null,
                        selectedPeraturanBabs = emptyList()
                    )
                }
                // Jika di tab selain Beranda/Pencarian, kembali ke Beranda/Pencarian
                state.currentScreen != ScreenDestination.SEARCH -> {
                    state.copy(currentScreen = ScreenDestination.SEARCH)
                }
                else -> state
            }
        }
    }

    fun selectPasal(pasalId: Long) {
        pasalDetailJob?.cancel()
        _uiState.update {
            val prev = if (it.currentScreen != ScreenDestination.PASAL_DETAIL) it.currentScreen else it.previousScreen
            it.copy(
                selectedPasalId = pasalId,
                previousScreen = prev,
                currentScreen = ScreenDestination.PASAL_DETAIL
            )
        }

        pasalDetailJob = viewModelScope.launch {
            repository.getPasalWithDetails(pasalId).collectLatest { details ->
                _uiState.update {
                    it.copy(currentPasalDetails = details)
                }
            }
        }
    }

    fun selectPeraturan(peraturan: PeraturanEntity) {
        peraturanBabJob?.cancel()
        _uiState.update {
            it.copy(
                selectedPeraturan = peraturan,
                selectedPeraturanBabs = emptyList()
            )
        }

        peraturanBabJob = viewModelScope.launch {
            repository.getBabsByPeraturan(peraturan.id).collectLatest { babs ->
                _uiState.update {
                    it.copy(selectedPeraturanBabs = babs)
                }
            }
        }
    }

    fun selectHierarchyLevel(level: HierarchyLevel) {
        _uiState.update {
            it.copy(
                selectedHierarchyLevel = level,
                currentScreen = ScreenDestination.HIERARCHY
            )
        }
    }

    fun toggleBookmark(pasalId: Long, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.toggleBookmark(pasalId, currentStatus)
            val msg = if (currentStatus) "Pasal dihapus dari koleksi tersimpan" else "Pasal berhasil disimpan ke koleksi!"
            _uiState.update { it.copy(userNotice = msg) }
        }
    }

    fun exportCurrentPasalToPdf(context: Context) {
        val details = _uiState.value.currentPasalDetails ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isExportingPdf = true) }
            val file = PdfExporter.exportPasalToPdf(context, details)
            _uiState.update {
                it.copy(
                    isExportingPdf = false,
                    lastExportedFile = file,
                    userNotice = if (file != null) "Dokumen PDF berhasil dibuat: ${file.name}" else "Gagal mengekspor dokumen PDF"
                )
            }
            if (file != null) {
                PdfExporter.openPdf(context, file)
            }
        }
    }

    fun clearNotice() {
        _uiState.update { it.copy(userNotice = null) }
    }

    fun setDisclaimerDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showDisclaimerDialog = visible) }
    }
}
