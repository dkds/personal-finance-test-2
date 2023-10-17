package com.dkds.data.converters

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class LocalDateTimeTypeConverter {

    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        val zoneId: ZoneId = ZoneId.systemDefault()
        return value?.let { Instant.ofEpochMilli(it).atZone(zoneId).toLocalDateTime() }
    }

    @TypeConverter
    fun dateToTimestamp(timestamp: LocalDateTime?): Long? {
        val zoneId: ZoneId = ZoneId.systemDefault()
        return timestamp?.atZone(zoneId)?.toInstant()?.toEpochMilli()
    }
}
