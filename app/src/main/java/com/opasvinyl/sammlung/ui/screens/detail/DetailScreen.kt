package com.opasvinyl.sammlung.ui.screens.detail

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.opasvinyl.sammlung.data.local.entity.RecordCondition
import com.opasvinyl.sammlung.data.local.entity.RecordStatus
import com.opasvinyl.sammlung.ui.components.ConditionBadge
import com.opasvinyl.sammlung.ui.theme.VinylBrown
import com.opasvinyl.sammlung.ui.theme.VinylBrownLight
import com.opasvinyl.sammlung.ui.theme.VinylCream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    recordId: Long,
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showConditionDialog by remember { mutableStateOf(false) }
    var editingNotes by remember { mutableStateOf(false) }
    var notesText by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onBack()
    }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val record = uiState.record ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Platte nicht gefunden")
        }
        return
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Platte löschen?") },
            text = { Text("\"${record.artist} - ${record.title}\" wird unwiderruflich gelöscht.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRecord()
                    showDeleteDialog = false
                }) { Text("Löschen", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Abbrechen") }
            }
        )
    }

    // Condition picker dialog
    if (showConditionDialog) {
        AlertDialog(
            onDismissRequest = { showConditionDialog = false },
            title = { Text("Zustand bewerten") },
            text = {
                Column {
                    RecordCondition.entries.forEach { condition ->
                        TextButton(
                            onClick = {
                                viewModel.updateCondition(condition)
                                showConditionDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                condition.label,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details", color = VinylCream) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zurück", tint = VinylCream)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "Menü", tint = VinylCream)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            when (record.status) {
                                RecordStatus.OWNED -> {
                                    DropdownMenuItem(
                                        text = { Text("Archivieren") },
                                        leadingIcon = { Icon(Icons.Default.Archive, null) },
                                        onClick = {
                                            viewModel.archiveRecord()
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Auf Wunschliste") },
                                        leadingIcon = { Icon(Icons.Default.FavoriteBorder, null) },
                                        onClick = {
                                            viewModel.moveToWishlist()
                                            showMenu = false
                                        }
                                    )
                                }
                                RecordStatus.WISHLIST -> {
                                    DropdownMenuItem(
                                        text = { Text("Als vorhanden markieren") },
                                        leadingIcon = { Icon(Icons.Default.Album, null) },
                                        onClick = {
                                            viewModel.markAsOwned()
                                            showMenu = false
                                        }
                                    )
                                }
                                RecordStatus.ARCHIVED -> {
                                    DropdownMenuItem(
                                        text = { Text("Wiederherstellen") },
                                        leadingIcon = { Icon(Icons.Default.Unarchive, null) },
                                        onClick = {
                                            viewModel.unarchiveRecord()
                                            showMenu = false
                                        }
                                    )
                                }
                            }
                            DropdownMenuItem(
                                text = { Text("Löschen") },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showDeleteDialog = true
                                    showMenu = false
                                }
                            )
                        }
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
                .verticalScroll(rememberScrollState())
        ) {
            // Cover Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                if (record.coverUrl != null) {
                    AsyncImage(
                        model = record.coverUrl,
                        contentDescription = "${record.artist} - ${record.title}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Album,
                        null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(48.dp),
                        tint = VinylBrownLight
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Title & Artist
                Text(record.title, style = MaterialTheme.typography.headlineMedium)
                Text(
                    record.artist,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                // Condition badge (clickable)
                TextButton(onClick = { showConditionDialog = true }) {
                    ConditionBadge(record.condition)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.Edit, "Zustand ändern", modifier = Modifier.padding(start = 4.dp))
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                // Metadata rows
                DetailRow("Erscheinungsjahr", record.year?.toString() ?: "Unbekannt")
                DetailRow("Genre", record.genre ?: "Unbekannt")
                if (!record.style.isNullOrBlank()) DetailRow("Stil", record.style)
                DetailRow("Label", record.label ?: "Unbekannt")
                if (!record.catalogNumber.isNullOrBlank()) DetailRow("Katalognr.", record.catalogNumber)
                DetailRow("Format", record.format)
                if (!record.country.isNullOrBlank()) DetailRow("Land", record.country)
                if (!record.barcode.isNullOrBlank()) DetailRow("Barcode", record.barcode)
                if (record.totalDuration != null) DetailRow("Gesamtlaufzeit", record.totalDuration)

                // Status
                val statusText = when (record.status) {
                    RecordStatus.OWNED -> "In Sammlung"
                    RecordStatus.WISHLIST -> "Wunschliste"
                    RecordStatus.ARCHIVED -> "Archiviert"
                }
                DetailRow("Status", statusText)

                // Marketplace / Pricing
                if (record.lowestPrice != null || record.numForSale != null) {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Marktwert (Discogs)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))

                    if (record.lowestPrice != null) {
                        DetailRow("Günstigster Preis", "%.2f €".format(record.lowestPrice))
                    }
                    if (record.medianPrice != null) {
                        DetailRow("Median-Preis", "%.2f €".format(record.medianPrice))
                    }
                    if (record.highestPrice != null) {
                        DetailRow("Höchster Preis", "%.2f €".format(record.highestPrice))
                    }
                    if (record.numForSale != null && record.numForSale > 0) {
                        DetailRow("Angebote", "${record.numForSale} Stück")
                    }
                }

                // Tracklist
                if (uiState.tracks.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Titelliste",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))

                    uiState.tracks.forEach { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(modifier = Modifier.weight(1f)) {
                                if (track.position.isNotBlank()) {
                                    Text(
                                        track.position,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.width(40.dp)
                                    )
                                }
                                Text(track.title, style = MaterialTheme.typography.bodyMedium)
                            }
                            if (track.duration != null) {
                                Text(
                                    track.duration,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Notes
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notizen", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = {
                        if (editingNotes) {
                            viewModel.updateNotes(notesText)
                        } else {
                            notesText = record.notes ?: ""
                        }
                        editingNotes = !editingNotes
                    }) {
                        Icon(
                            Icons.Default.Edit,
                            if (editingNotes) "Speichern" else "Bearbeiten"
                        )
                    }
                }

                if (editingNotes) {
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Persönliche Notizen…") },
                        minLines = 3
                    )
                } else {
                    Text(
                        text = record.notes ?: "Keine Notizen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (record.notes != null) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
