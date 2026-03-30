package com.opasvinyl.sammlung.ui.screens.photo

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.opasvinyl.sammlung.data.local.entity.RecordStatus
import com.opasvinyl.sammlung.data.remote.dto.DiscogsSearchResult
import com.opasvinyl.sammlung.data.repository.VinylRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class PhotoUiState(
    val photoUri: Uri? = null,
    val recognizedText: String = "",
    val searchQuery: String = "",
    val results: List<DiscogsSearchResult> = emptyList(),
    val isRecognizing: Boolean = false,
    val isSearching: Boolean = false,
    val isSaving: Boolean = false,
    val savedRecordId: Long? = null,
    val error: String? = null,
    val step: PhotoStep = PhotoStep.CAPTURE
)

enum class PhotoStep {
    CAPTURE,
    RECOGNIZING,
    RESULTS
}

@HiltViewModel
class PhotoRecognitionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: VinylRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotoUiState())
    val uiState: StateFlow<PhotoUiState> = _uiState.asStateFlow()

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun onPhotoSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            photoUri = uri,
            isRecognizing = true,
            step = PhotoStep.RECOGNIZING,
            error = null
        )

        viewModelScope.launch {
            try {
                val image = InputImage.fromFilePath(context, uri)
                val result = textRecognizer.process(image).await()
                val fullText = result.text

                // Extract likely artist and album from recognized text
                val searchQuery = extractSearchQuery(fullText)

                _uiState.value = _uiState.value.copy(
                    recognizedText = fullText,
                    searchQuery = searchQuery,
                    isRecognizing = false
                )

                // Auto-search if we found text
                if (searchQuery.isNotBlank()) {
                    searchDiscogs()
                } else {
                    _uiState.value = _uiState.value.copy(
                        step = PhotoStep.RESULTS,
                        error = "Kein Text erkannt. Bitte manuell eingeben."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRecognizing = false,
                    step = PhotoStep.RESULTS,
                    error = "Texterkennung fehlgeschlagen: ${e.message}"
                )
            }
        }
    }

    private fun extractSearchQuery(fullText: String): String {
        // Filter out noise and take the most meaningful lines
        // Vinyl covers typically have artist name and album title as the largest text
        val lines = fullText.lines()
            .map { it.trim() }
            .filter { line ->
                line.length > 2 &&
                !line.all { it.isDigit() || it == '.' || it == ',' } && // skip pure numbers
                !line.contains("©") && // skip copyright
                !line.contains("℗") &&
                !line.matches(Regex("^[A-Z]{2,4}[- ]?\\d+.*")) // skip catalog numbers like "CBS 123"
            }
            .take(2) // Artist + Album title is usually enough

        return lines.joinToString(" ").take(80)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun searchDiscogs() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isBlank()) return

        _uiState.value = _uiState.value.copy(
            isSearching = true,
            error = null,
            step = PhotoStep.RESULTS
        )

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

    fun retakePhoto() {
        _uiState.value = PhotoUiState()
    }

    override fun onCleared() {
        super.onCleared()
        textRecognizer.close()
    }
}
