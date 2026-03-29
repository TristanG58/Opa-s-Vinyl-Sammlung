package com.opasvinyl.sammlung.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.opasvinyl.sammlung.data.local.entity.RecordStatus
import com.opasvinyl.sammlung.data.local.entity.Track
import com.opasvinyl.sammlung.data.local.entity.VinylRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface VinylDao {

    // === Records ===

    @Query("SELECT * FROM vinyl_records WHERE status = :status ORDER BY dateAdded DESC")
    fun getRecordsByStatus(status: RecordStatus): Flow<List<VinylRecord>>

    @Query("SELECT * FROM vinyl_records WHERE status != 'ARCHIVED' ORDER BY dateAdded DESC")
    fun getAllActiveRecords(): Flow<List<VinylRecord>>

    @Query("SELECT * FROM vinyl_records WHERE id = :id")
    suspend fun getRecordById(id: Long): VinylRecord?

    @Query("SELECT * FROM vinyl_records WHERE (title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%') AND status = :status ORDER BY dateAdded DESC")
    fun searchRecords(query: String, status: RecordStatus): Flow<List<VinylRecord>>

    @Query("SELECT * FROM vinyl_records WHERE genre = :genre AND status = :status ORDER BY dateAdded DESC")
    fun getRecordsByGenre(genre: String, status: RecordStatus): Flow<List<VinylRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: VinylRecord): Long

    @Update
    suspend fun updateRecord(record: VinylRecord)

    @Delete
    suspend fun deleteRecord(record: VinylRecord)

    @Query("UPDATE vinyl_records SET status = :status, dateModified = :now WHERE id = :id")
    suspend fun updateRecordStatus(id: Long, status: RecordStatus, now: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM vinyl_records WHERE status != 'ARCHIVED'")
    fun getActiveRecordCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM vinyl_records")
    fun getTotalRecordCount(): Flow<Int>

    @Query("SELECT DISTINCT genre FROM vinyl_records WHERE genre IS NOT NULL AND status = :status")
    fun getGenres(status: RecordStatus = RecordStatus.OWNED): Flow<List<String>>

    // === Stats ===

    @Query("SELECT COUNT(*) FROM vinyl_records WHERE status = :status")
    suspend fun getCountByStatus(status: RecordStatus): Int

    @Query("SELECT genre, COUNT(*) as count FROM vinyl_records WHERE genre IS NOT NULL AND status = 'OWNED' GROUP BY genre ORDER BY count DESC")
    suspend fun getGenreStats(): List<GenreStat>

    @Query("SELECT (year / 10) * 10 as decade, COUNT(*) as count FROM vinyl_records WHERE year IS NOT NULL AND status = 'OWNED' GROUP BY decade ORDER BY decade")
    suspend fun getDecadeStats(): List<DecadeStat>

    // === Tracks ===

    @Query("SELECT * FROM tracks WHERE recordId = :recordId ORDER BY position")
    suspend fun getTracksForRecord(recordId: Long): List<Track>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<Track>)

    @Query("DELETE FROM tracks WHERE recordId = :recordId")
    suspend fun deleteTracksForRecord(recordId: Long)

    @Transaction
    suspend fun insertRecordWithTracks(record: VinylRecord, tracks: List<Track>): Long {
        val recordId = insertRecord(record)
        val tracksWithId = tracks.map { it.copy(recordId = recordId) }
        insertTracks(tracksWithId)
        return recordId
    }
}

data class GenreStat(val genre: String, val count: Int)
data class DecadeStat(val decade: Int, val count: Int)
