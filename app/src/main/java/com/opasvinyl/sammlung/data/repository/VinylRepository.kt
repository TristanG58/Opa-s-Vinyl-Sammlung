package com.opasvinyl.sammlung.data.repository

import com.opasvinyl.sammlung.data.local.DecadeStat
import com.opasvinyl.sammlung.data.local.GenreStat
import com.opasvinyl.sammlung.data.local.VinylDao
import com.opasvinyl.sammlung.data.local.entity.RecordCondition
import com.opasvinyl.sammlung.data.local.entity.RecordStatus
import com.opasvinyl.sammlung.data.local.entity.Track
import com.opasvinyl.sammlung.data.local.entity.VinylRecord
import com.opasvinyl.sammlung.data.remote.DiscogsApi
import com.opasvinyl.sammlung.data.remote.dto.DiscogsRelease
import com.opasvinyl.sammlung.data.remote.dto.DiscogsSearchResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VinylRepository @Inject constructor(
    private val dao: VinylDao,
    private val api: DiscogsApi
) {
    // === Local DB ===

    fun getRecordsByStatus(status: RecordStatus): Flow<List<VinylRecord>> =
        dao.getRecordsByStatus(status)

    fun searchRecords(query: String, status: RecordStatus): Flow<List<VinylRecord>> =
        dao.searchRecords(query, status)

    fun getActiveRecordCount(): Flow<Int> = dao.getActiveRecordCount()

    fun getGenres(): Flow<List<String>> = dao.getGenres()

    fun getRecordsByGenre(genre: String, status: RecordStatus): Flow<List<VinylRecord>> =
        dao.getRecordsByGenre(genre, status)

    suspend fun getRecordById(id: Long): VinylRecord? = dao.getRecordById(id)

    suspend fun getTracksForRecord(recordId: Long): List<Track> =
        dao.getTracksForRecord(recordId)

    suspend fun insertRecordWithTracks(record: VinylRecord, tracks: List<Track>): Long =
        dao.insertRecordWithTracks(record, tracks)

    suspend fun updateRecord(record: VinylRecord) = dao.updateRecord(record)

    suspend fun deleteRecord(record: VinylRecord) = dao.deleteRecord(record)

    suspend fun updateRecordStatus(id: Long, status: RecordStatus) =
        dao.updateRecordStatus(id, status)

    // === Stats ===

    suspend fun getCountByStatus(status: RecordStatus): Int = dao.getCountByStatus(status)

    suspend fun getGenreStats(): List<GenreStat> = dao.getGenreStats()

    suspend fun getDecadeStats(): List<DecadeStat> = dao.getDecadeStats()

    // === Discogs API ===

    suspend fun searchDiscogsByBarcode(barcode: String): Result<List<DiscogsSearchResult>> {
        return try {
            val response = api.searchByBarcode(barcode)
            Result.success(response.results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchDiscogsByQuery(query: String): Result<List<DiscogsSearchResult>> {
        return try {
            val response = api.searchByQuery(query)
            Result.success(response.results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDiscogsRelease(releaseId: Int): Result<DiscogsRelease> {
        return try {
            val release = api.getRelease(releaseId)
            Result.success(release)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun discogsReleaseToRecord(
        release: DiscogsRelease,
        status: RecordStatus = RecordStatus.OWNED
    ): Pair<VinylRecord, List<Track>> {
        val artistName = release.artists.joinToString(", ") { it.name }
        val coverUrl = release.images.firstOrNull { it.type == "primary" }?.uri
            ?: release.images.firstOrNull()?.uri
        val label = release.labels.firstOrNull()
        val barcode = release.identifiers.firstOrNull { it.type == "Barcode" }?.value
        val format = release.formats.firstOrNull()?.let { fmt ->
            val desc = fmt.descriptions.joinToString(", ")
            if (desc.isNotBlank()) "${fmt.name} ($desc)" else fmt.name
        } ?: "Vinyl"

        val totalDuration = calculateTotalDuration(release.tracklist.map { it.duration })

        val record = VinylRecord(
            discogsId = release.id,
            title = release.title,
            artist = artistName,
            year = release.year,
            genre = release.genres.firstOrNull(),
            style = release.styles.joinToString(", "),
            label = label?.name,
            catalogNumber = label?.catno,
            barcode = barcode,
            coverUrl = coverUrl,
            totalDuration = totalDuration,
            trackCount = release.tracklist.size,
            format = format,
            condition = RecordCondition.NOT_GRADED,
            notes = null,
            status = status,
            country = release.country
        )

        val tracks = release.tracklist.map { t ->
            Track(
                recordId = 0, // will be set after insert
                position = t.position,
                title = t.title,
                duration = t.duration.ifBlank { null }
            )
        }

        return record to tracks
    }

    private fun calculateTotalDuration(durations: List<String>): String? {
        var totalSeconds = 0
        var hasAny = false

        for (d in durations) {
            if (d.isBlank()) continue
            val parts = d.split(":")
            if (parts.size == 2) {
                val mins = parts[0].trim().toIntOrNull() ?: continue
                val secs = parts[1].trim().toIntOrNull() ?: continue
                totalSeconds += mins * 60 + secs
                hasAny = true
            }
        }

        if (!hasAny) return null

        val hours = totalSeconds / 3600
        val mins = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60

        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, mins, secs)
        } else {
            "%d:%02d".format(mins, secs)
        }
    }
}
