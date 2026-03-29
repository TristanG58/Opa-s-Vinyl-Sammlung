package com.opasvinyl.sammlung.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opasvinyl.sammlung.data.local.entity.RecordStatus
import com.opasvinyl.sammlung.data.local.entity.VinylRecord
import com.opasvinyl.sammlung.data.repository.VinylRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: VinylRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGenre = MutableStateFlow<String?>(null)
    val selectedGenre: StateFlow<String?> = _selectedGenre.asStateFlow()

    private val _showArchived = MutableStateFlow(false)
    val showArchived: StateFlow<Boolean> = _showArchived.asStateFlow()

    val genres: StateFlow<List<String>> = repository.getGenres()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recordCount: StateFlow<Int> = repository.getActiveRecordCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val records: StateFlow<List<VinylRecord>> = combine(
        _searchQuery,
        _selectedGenre,
        _showArchived
    ) { query, genre, showArchived ->
        Triple(query, genre, showArchived)
    }.flatMapLatest { (query, genre, showArchived) ->
        val status = if (showArchived) RecordStatus.ARCHIVED else RecordStatus.OWNED
        when {
            query.isNotBlank() -> repository.searchRecords(query, status)
            genre != null -> repository.getRecordsByGenre(genre, status)
            else -> repository.getRecordsByStatus(status)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onGenreSelected(genre: String?) {
        _selectedGenre.value = genre
    }

    fun toggleShowArchived() {
        _showArchived.value = !_showArchived.value
        _selectedGenre.value = null
        _searchQuery.value = ""
    }
}
