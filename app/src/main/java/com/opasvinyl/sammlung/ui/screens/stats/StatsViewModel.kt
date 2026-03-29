package com.opasvinyl.sammlung.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opasvinyl.sammlung.data.local.DecadeStat
import com.opasvinyl.sammlung.data.local.GenreStat
import com.opasvinyl.sammlung.data.local.entity.RecordStatus
import com.opasvinyl.sammlung.data.repository.VinylRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatsUiState(
    val ownedCount: Int = 0,
    val wishlistCount: Int = 0,
    val archivedCount: Int = 0,
    val genreStats: List<GenreStat> = emptyList(),
    val decadeStats: List<DecadeStat> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: VinylRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = StatsUiState(
                ownedCount = repository.getCountByStatus(RecordStatus.OWNED),
                wishlistCount = repository.getCountByStatus(RecordStatus.WISHLIST),
                archivedCount = repository.getCountByStatus(RecordStatus.ARCHIVED),
                genreStats = repository.getGenreStats(),
                decadeStats = repository.getDecadeStats(),
                isLoading = false
            )
        }
    }
}
