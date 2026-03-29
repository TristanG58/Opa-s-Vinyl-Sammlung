package com.opasvinyl.sammlung.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.opasvinyl.sammlung.data.local.converter.Converters
import com.opasvinyl.sammlung.data.local.entity.Track
import com.opasvinyl.sammlung.data.local.entity.VinylRecord

@Database(
    entities = [VinylRecord::class, Track::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class VinylDatabase : RoomDatabase() {
    abstract fun vinylDao(): VinylDao
}
