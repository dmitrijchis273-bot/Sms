package com.dmitrijchis273.smsapitester.data.database

import androidx.room.TypeConverter
import com.dmitrijchis273.smsapitester.data.models.RequestStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(formatter)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, formatter) }
    }

    @TypeConverter
    fun fromRequestStatus(value: RequestStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toRequestStatus(value: String?): RequestStatus? {
        return value?.let { RequestStatus.valueOf(it) }
    }
}
