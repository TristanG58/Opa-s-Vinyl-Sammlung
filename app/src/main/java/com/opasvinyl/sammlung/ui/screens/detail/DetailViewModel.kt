package com.opasvinyl.sammlung.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opasvinyl.sammlung.data.local.entity.RecordCondition
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

data class DetailUiState(
    val record: VinylRecord? = null,
    val tracks: List<Track> = emptyList(),
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val isDeleted: Boolean = false
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: VinylRepository
) : ViewModel() {

    private val recordId: Long = savedStateHandle["recordId"] ?: 0L

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadRecord()
    }

    private fun loadRecord() {
        viewModelScope.launch {
            val record = repository.getRecordById(recordId)
            val tracks = if (record != null) repository.getTracksForRecord(recordId) else emptyList()
            _uiState.value = _uiState.value.copy(
                record = record,
                tracks = tracks,
                isLoading = false
            )
        }
    }

    fun archiveRecord() {
        viewModelScope.launch {
            repository.updateRecordStatus(recordId, RecordStatus.ARCHIVED)
            loadRecord()
        }
    }

    fun unarchiveRecord() {
        viewModelScope.launch {
            repository.updateRecordStatus(recordId, RecordStatus.OWNED)
            loadRecord()
        }
    }

    fun moveToWishlist() {
        viewModelScope.launch {
            repository.updateRecordStatus(recordId, RecordStatus.WISHLIST)
            loadRecord()
        }
    }

    fun markAsOwned() {
        viewModelScope.launch {
            repository.updateRecordStatus(recordId, RecordStatus.OWNED)
            loadRecord()
        }
    }

    fun updateCondition(condition: RecordCondition) {
        viewModelScope.launch {
            _uiState.value.record?.let { record ->
                val updated = record.copy(
                    condition = condition,
                    dateModified = System.currentTimeMillis()
                )
                repository.updateRecord(updated)
                loadRecord()
            }
        }
    }

    fun updateNotes(notes: String) {
        viewModelScope.launch {
            _uiState.value.record?.let { record ->
                val updated = record.copy(
                    notes = notes.ifBlank { null },
                    dateModified = System.currentTimeMillis()
                )
                repository.updateRecord(updated)
                loadRecord()
            }
        }
    }

    fun deleteRecord() {
        viewModelScope.launch {
            _uiState.value.record?.let { record ->
                repository.deleteRecord(record)
                _uiState.value = _uiState.value.copy(isDeleted = true)
            }
        }
    }

    fun toggleEditing() {
        _uiState.value = _uiState.value.copy(isEditing = !_uiState.value.isEditing)
    }
}
