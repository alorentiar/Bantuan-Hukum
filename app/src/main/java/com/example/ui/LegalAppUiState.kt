package com.example.ui

import com.example.data.local.entity.BabEntity
import com.example.data.local.entity.PeraturanEntity
import com.example.data.local.model.HierarchyLevel
import com.example.data.local.model.PasalWithDetails
import com.example.data.local.model.SearchResultItem
import java.io.File

enum class ScreenDestination {
    SEARCH,
    HIERARCHY,
    PASAL_DETAIL,
    BOOKMARKS,
    GUIDE
}

data class LegalAppUiState(
    val isDbInitializing: Boolean = true,
    val searchQuery: String = "",
    val selectedCategory: String = "SEMUA",
    val searchResults: List<SearchResultItem> = emptyList(),
    val isSearching: Boolean = false,
    val regulations: List<PeraturanEntity> = emptyList(),
    val currentScreen: ScreenDestination = ScreenDestination.SEARCH,
    val selectedPeraturan: PeraturanEntity? = null,
    val selectedPeraturanBabs: List<BabEntity> = emptyList(),
    val selectedPasalId: Long? = null,
    val currentPasalDetails: PasalWithDetails? = null,
    val bookmarkedPasals: List<PasalWithDetails> = emptyList(),
    val isExportingPdf: Boolean = false,
    val lastExportedFile: File? = null,
    val userNotice: String? = null,
    val showDisclaimerDialog: Boolean = false,
    val selectedHierarchyLevel: HierarchyLevel? = null
)
