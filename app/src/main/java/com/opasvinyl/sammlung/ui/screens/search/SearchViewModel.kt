package com.opasvinyl.sammlung.ui.screens.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opasvinyl.sammlung.data.local.entity.RecordStatus
import com.opasvinyl.sammlung.data.remote.dto.DiscogsSearchResult
import com.opasvinyl.sammlung.data.repository.VinylRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<DiscogsSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val isSaving: Boolean = false,
    val savedRecordId: Long? = null,
    val error: String? = null,
    val hasSearched: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: VinylRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        val barcode = savedStateHandle.get<String>("barcode") ?: ""
        if (barcode.isNotBlank()) {
            searchByBarcode(barcode)
        }
    }

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun searchByQuery() {
        val query = _uiState.value.query.trim()
        if (query.isBlank()) return

        _uiState.value = _uiState.value.copy(isSearching = true, error = null, hasSearched = true)

        viewModelScope.launch {
            repository.searchDiscogsByQuery(query).fold(
                onSuccess = { results ->
                    _uiState.value = _uiState.value.copy(
                        results = results,
                        isSearching = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        error = "Suche fehlgeschlagen: ${e.message}"
                    )
                }
            )
        }
    }

    fun searchByBarcode(barcode: String) {
        _uiState.value = _uiState.value.copy(
            query = barcode,
            isSearching = true,
            error = null,
            hasSearched = true
        )

        viewModelScope.launch {
            repository.searchDiscogsByBarcode(barcode).fold(
                onSuccess = { results ->
                    _uiState.value = _uiState.value.copy(
                        results = results,
                        isSearching = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        error = "Barcode-Suche fehlgeschlagen: ${e.message}"
                    )
                }
            )
        }
    }

    fun selectResult(result: DiscogsSearchResult, status: RecordStatus = RecordStatus.OWNED) {
        _uiState.value = _uiState.value.copy(isSaving = true, error = null)

        viewModelScope.launch {
            repository.getDiscogsRelease(result.id).fold(
                onSuccess = { release ->
                    val (record, tracks) = repository.discogsReleaseToRecord(release, status)
                    val id = repository.insertRecordWithTracks(record, tracks)
                    _uiState.value = _uiState.value.copy(isSaving = false, savedRecordId = id)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = "Fehler beim Laden: ${e.message}"
                    )
                }
            )
        }
    }
}
