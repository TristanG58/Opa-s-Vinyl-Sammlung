package com.opasvinyl.sammlung.data.local.converter

import androidx.room.TypeConverter
import com.opasvinyl.sammlung.data.local.entity.RecordCondition
import com.opasvinyl.sammlung.data.local.entity.RecordStatus

class Converters {
    @TypeConverter
    fun fromRecordStatus(status: RecordStatus): String = status.name

    @TypeConverter
    fun toRecordStatus(value: String): RecordStatus = RecordStatus.valueOf(value)

    @TypeConverter
    fun fromRecordCondition(condition: RecordCondition): String = condition.name

    @TypeConverter
    fun toRecordCondition(value: String): RecordCondition = RecordCondition.valueOf(value)
}
