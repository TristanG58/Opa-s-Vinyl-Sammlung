package com.opasvinyl.sammlung.ui.screens.photo

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.opasvinyl.sammlung.data.remote.dto.DiscogsSearchResult
import com.opasvinyl.sammlung.ui.theme.VinylBrown
import com.opasvinyl.sammlung.ui.theme.VinylBrownLight
import com.opasvinyl.sammlung.ui.theme.VinylCream
import com.opasvinyl.sammlung.ui.theme.VinylGold
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoRecognitionScreen(
    onBack: () -> Unit,
    onRecordSaved: (Long) -> Unit,
    viewModel: PhotoRecognitionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Camera permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(uiState.savedRecordId) {
        uiState.savedRecordId?.let { onRecordSaved(it) }
    }

    // Photo picker (gallery)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.onPhotoSelected(it) }
    }

    // Camera capture
    val cameraUri = remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            cameraUri.value?.let { viewModel.onPhotoSelected(it) }
        }
    }

    fun launchCamera() {
        val photoFile = File(context.cacheDir, "vinyl_photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        cameraUri.value = uri
        cameraLauncher.launch(uri)
    }

    // Pending camera launch after permission grant
    var pendingCameraLaunch by remember { mutableStateOf(false) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted && pendingCameraLaunch) {
            pendingCameraLaunch = false
            launchCamera()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Foto erkennen", color = VinylCream) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zurück", tint = VinylCream)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VinylBrown)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (uiState.step) {
                PhotoStep.CAPTURE -> {
                    Spacer(Modifier.height(32.dp))

                    Text(
                        "Fotografiere das Vinyl-Cover oder wähle ein Foto aus der Galerie",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {
                            if (hasCameraPermission) {
                                launchCamera()
                            } else {
                                pendingCameraLaunch = true
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VinylGold,
                            contentColor = VinylBrown
                        )
                    ) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Foto aufnehmen", style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Aus Galerie wählen", style = MaterialTheme.typography.titleMedium)
                    }
                }

                PhotoStep.RECOGNIZING -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (uiState.photoUri != null) {
                                AsyncImage(
                                    model = uiState.photoUri,
                                    contentDescription = "Aufgenommenes Foto",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                )
                                Spacer(Modifier.height(16.dp))
                            }
                            CircularProgressIndicator()
                            Spacer(Modifier.height(8.dp))
                            Text("Text wird erkannt…", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                PhotoStep.RESULTS -> {
                    // Show photo thumbnail
                    if (uiState.photoUri != null) {
                        AsyncImage(
                            model = uiState.photoUri,
                            contentDescription = "Aufgenommenes Foto",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    // Recognized text info
                    if (uiState.recognizedText.isNotBlank()) {
                        Text(
                            "Erkannter Text: ${uiState.recognizedText.take(80)}…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                        Spacer(Modifier.height(4.dp))
                    }

                    // Editable search query
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            label = { Text("Suchbegriff") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { viewModel.searchDiscogs() })
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { viewModel.searchDiscogs() }) {
                            Icon(Icons.Default.Search, "Suchen")
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = { viewModel.retakePhoto() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.padding(end = 8.dp))
                        Text("Neues Foto aufnehmen")
                    }

                    Spacer(Modifier.height(8.dp))

                    if (uiState.error != null) {
                        Text(
                            uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    when {
                        uiState.isSearching || uiState.isSaving -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        if (uiState.isSaving) "Platte wird gespeichert…" else "Suche auf Discogs…",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        uiState.results.isEmpty() && uiState.searchQuery.isNotBlank() && !uiState.isSearching -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Keine Ergebnisse. Versuche den Suchtext anzupassen.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        else -> {
                            LazyColumn {
                                items(uiState.results) { result ->
                                    PhotoSearchResultItem(
                                        result = result,
                                        onClick = { viewModel.selectResult(result) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoSearchResultItem(
    result: DiscogsSearchResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (result.thumbnail != null) {
                AsyncImage(
                    model = result.thumbnail,
                    contentDescription = result.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(64.dp)
                )
            } else {
                Icon(
                    Icons.Default.Album,
                    null,
                    modifier = Modifier.size(64.dp),
                    tint = VinylBrownLight
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    result.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (result.year != null) {
                    Text(
                        result.year,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (result.label.isNotEmpty()) {
                    Text(
                        result.label.first(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
