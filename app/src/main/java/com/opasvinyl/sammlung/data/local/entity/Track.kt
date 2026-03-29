package com.opasvinyl.sammlung.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tracks",
    foreignKeys = [
        ForeignKey(
            entity = VinylRecord::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recordId")]
)
data class Track(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recordId: Long,
    val position: String,
    val title: String,
    val duration: String? = null
)
