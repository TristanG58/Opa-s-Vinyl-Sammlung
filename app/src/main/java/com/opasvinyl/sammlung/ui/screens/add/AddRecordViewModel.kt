package com.opasvinyl.sammlung.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opasvinyl.sammlung.data.local.entity.RecordStatus
import com.opasvinyl.sammlung.data.local.entity.Track
import com.opasvinyl.sammlung.data.local.entity.VinylRecord
import com.opasvinyl.sammlung.data.repository.VinylRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddRecordUiState(
    val title: String = "",
    val artist: String = "",
    val year: String = "",
    val genre: String = "",
    val label: String = "",
    val format: String = "LP",
    val isSaving: Boolean = false,
    val savedRecordId: Long? = null,
    val error: String? = null
)

@HiltViewModel
class AddRecordViewModel @Inject constructor(
    private val repository: VinylRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddRecordUiState())
    val uiState: StateFlow<AddRecordUiState> = _uiState.asStateFlow()

    fun updateTitle(value: String) { _uiState.value = _uiState.value.copy(title = value) }
    fun updateArtist(value: String) { _uiState.value = _uiState.value.copy(artist = value) }
    fun updateYear(value: String) { _uiState.value = _uiState.value.copy(year = value) }
    fun updateGenre(value: String) { _uiState.value = _uiState.value.copy(genre = value) }
    fun updateLabel(value: String) { _uiState.value = _uiState.value.copy(label = value) }
    fun updateFormat(value: String) { _uiState.value = _uiState.value.copy(format = value) }

    fun saveRecord(status: RecordStatus = RecordStatus.OWNED) {
        val state = _uiState.value
        if (state.title.isBlank() || state.artist.isBlank()) {
            _uiState.value = state.copy(error = "Titel und Künstler sind Pflichtfelder")
            return
        }

        _uiState.value = state.copy(isSaving = true, error = null)

        viewModelScope.launch {
            try {
                val record = VinylRecord(
                    title = state.title.trim(),
                    artist = state.artist.trim(),
                    year = state.year.toIntOrNull(),
                    genre = state.genre.ifBlank { null },
                    label = state.label.ifBlank { null },
                    format = state.format.ifBlank { "LP" },
                    status = status
                )
                val id = repository.insertRecordWithTracks(record, emptyList())
                _uiState.value = _uiState.value.copy(isSaving = false, savedRecordId = id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Fehler beim Speichern: ${e.message}"
                )
            }
        }
    }
}
