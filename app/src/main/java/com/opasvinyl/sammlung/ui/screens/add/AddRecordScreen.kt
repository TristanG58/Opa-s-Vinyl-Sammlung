package com.opasvinyl.sammlung.ui.screens.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.opasvinyl.sammlung.ui.theme.VinylBrown
import com.opasvinyl.sammlung.ui.theme.VinylCream
import com.opasvinyl.sammlung.ui.theme.VinylGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordScreen(
    onBack: () -> Unit,
    onSearchDiscogs: () -> Unit,
    onRecordAdded: (Long) -> Unit,
    viewModel: AddRecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.savedRecordId) {
        uiState.savedRecordId?.let { onRecordAdded(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Platte hinzufügen", color = VinylCream) },
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
                .verticalScroll(rememberScrollState())
        ) {
            // Discogs search buttons
            Text(
                "Automatisch suchen",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onSearchDiscogs,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VinylGold, contentColor = VinylBrown)
            ) {
                Icon(Icons.Default.Search, null, modifier = Modifier.padding(end = 8.dp))
                Text("Auf Discogs suchen")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    // Navigate to search with barcode scanner
                    onSearchDiscogs()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.padding(end = 8.dp))
                Text("Barcode scannen")
            }

            Spacer(Modifier.height(24.dp))

            // Manual entry
            Text(
                "Oder manuell eingeben",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))

            if (uiState.error != null) {
                Text(
                    uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = uiState.artist,
                onValueChange = { viewModel.updateArtist(it) },
                label = { Text("Künstler *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Titel *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.year,
                onValueChange = { viewModel.updateYear(it) },
                label = { Text("Erscheinungsjahr") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.genre,
                onValueChange = { viewModel.updateGenre(it) },
                label = { Text("Genre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.label,
                onValueChange = { viewModel.updateLabel(it) },
                label = { Text("Label") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.format,
                onValueChange = { viewModel.updateFormat(it) },
                label = { Text("Format") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("z.B. LP, Single, EP") }
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { viewModel.saveRecord() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                Text(if (uiState.isSaving) "Speichert…" else "Speichern")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
