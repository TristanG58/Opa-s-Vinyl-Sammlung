package com.opasvinyl.sammlung.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.opasvinyl.sammlung.ui.components.VinylGridItem
import com.opasvinyl.sammlung.ui.theme.VinylBrown
import com.opasvinyl.sammlung.ui.theme.VinylGold
import com.opasvinyl.sammlung.util.Constants

@Composable
fun LibraryScreen(
    onRecordClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val records by viewModel.records.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val showArchived by viewModel.showArchived.collectAsState()
    val recordCount by viewModel.recordCount.collectAsState()

    Scaffold(
        floatingActionButton = {
            if (!showArchived && recordCount < Constants.MAX_RECORDS) {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = VinylGold,
                    contentColor = VinylBrown
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Platte hinzufügen")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (showArchived) "Archiv" else "Meine Sammlung",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "$recordCount von ${Constants.MAX_RECORDS} Platten",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = { viewModel.toggleShowArchived() }) {
                    Icon(
                        if (showArchived) Icons.Default.Inventory2 else Icons.Default.Archive,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(if (showArchived) "Sammlung" else "Archiv")
                }
            }

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                placeholder = { Text("Künstler oder Titel suchen…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Löschen")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                singleLine = true
            )

            // Genre filter chips
            if (genres.isNotEmpty() && !showArchived) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(genres) { genre ->
                        FilterChip(
                            selected = selectedGenre == genre,
                            onClick = {
                                viewModel.onGenreSelected(
                                    if (selectedGenre == genre) null else genre
                                )
                            },
                            label = { Text(genre) }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Records grid
            if (records.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (showArchived) "Kein Archiv vorhanden"
                        else if (searchQuery.isNotBlank()) "Keine Treffer für \"$searchQuery\""
                        else "Noch keine Platten in der Sammlung",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!showArchived && searchQuery.isBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tippe auf + um deine erste Platte hinzuzufügen",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(records, key = { it.id }) { record ->
                        VinylGridItem(
                            record = record,
                            onClick = { onRecordClick(record.id) }
                        )
                    }
                }
            }
        }
    }
}
