package com.opasvinyl.sammlung.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vinyl_records")
data class VinylRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val discogsId: Int? = null,
    val title: String,
    val artist: String,
    val year: Int? = null,
    val genre: String? = null,
    val style: String? = null,
    val label: String? = null,
    val catalogNumber: String? = null,
    val barcode: String? = null,
    val coverUrl: String? = null,
    val localPhotoPath: String? = null,
    val totalDuration: String? = null,
    val trackCount: Int = 0,
    val format: String = "LP",
    val condition: RecordCondition = RecordCondition.NOT_GRADED,
    val notes: String? = null,
    val status: RecordStatus = RecordStatus.OWNED,
    val country: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val dateModified: Long = System.currentTimeMillis()
)

enum class RecordStatus {
    OWNED,
    WISHLIST,
    ARCHIVED
}

enum class RecordCondition(val label: String) {
    MINT("Mint (M)"),
    NEAR_MINT("Near Mint (NM)"),
    VERY_GOOD_PLUS("Very Good Plus (VG+)"),
    VERY_GOOD("Very Good (VG)"),
    GOOD_PLUS("Good Plus (G+)"),
    GOOD("Good (G)"),
    FAIR("Fair (F)"),
    POOR("Poor (P)"),
    NOT_GRADED("Nicht bewertet")
}
