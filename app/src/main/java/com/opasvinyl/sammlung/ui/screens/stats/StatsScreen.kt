package com.opasvinyl.sammlung.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.opasvinyl.sammlung.ui.theme.VinylBrown
import com.opasvinyl.sammlung.ui.theme.VinylGold
import com.opasvinyl.sammlung.ui.theme.VinylGoldLight
import com.opasvinyl.sammlung.ui.theme.VinylGreen
import com.opasvinyl.sammlung.ui.theme.VinylRed
import com.opasvinyl.sammlung.util.Constants

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Statistik", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        // Overview cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Sammlung",
                count = uiState.ownedCount,
                color = VinylGreen,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Wunschliste",
                count = uiState.wishlistCount,
                color = VinylGold,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Archiv",
                count = uiState.archivedCount,
                color = VinylBrown,
                modifier = Modifier.weight(1f)
            )
        }

        // Collection value
        if (uiState.totalValue > 0) {
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = VinylGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Sammlungswert",
                        style = MaterialTheme.typography.labelMedium,
                        color = VinylBrown
                    )
                    Text(
                        "%.2f €".format(uiState.totalValue),
                        style = MaterialTheme.typography.headlineMedium,
                        color = VinylBrown
                    )
                    Text(
                        "basierend auf günstigstem Discogs-Preis",
                        style = MaterialTheme.typography.bodySmall,
                        color = VinylBrown
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Capacity bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Kapazität", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${uiState.ownedCount + uiState.wishlistCount} / ${Constants.MAX_RECORDS}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(8.dp))
                val progress = (uiState.ownedCount + uiState.wishlistCount).toFloat() / Constants.MAX_RECORDS
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = if (progress > 0.9f) VinylRed else VinylGold,
                    trackColor = VinylGoldLight
                )
            }
        }

        // Genre stats
        if (uiState.genreStats.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text("Genres", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val maxCount = uiState.genreStats.maxOf { it.count }
                    uiState.genreStats.forEachIndexed { index, stat ->
                        if (index > 0) Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stat.genre,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.width(100.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(VinylGoldLight)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(stat.count.toFloat() / maxCount)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(VinylGold)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${stat.count}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // Decade stats
        if (uiState.decadeStats.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text("Jahrzehnte", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val maxCount = uiState.decadeStats.maxOf { it.count }
                    uiState.decadeStats.forEachIndexed { index, stat ->
                        if (index > 0) Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${stat.decade}er",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.width(80.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(VinylGoldLight)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(stat.count.toFloat() / maxCount)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(VinylBrown)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${stat.count}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun StatCard(
    title: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "$count",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
