package com.example.criminalintent.database

import androidx.room.TypeConverter
import java.util.*

class CrimeTypeConverters {  //Helps database to convert non-primitive classes
    @TypeConverter
    fun fromDate(date: Date?) : Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(millis: Long?) : Date? {
        return millis?.let {
            Date(it)
        }
    }

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun toUUID(uuid: String?): UUID? {
        return UUID.fromString(uuid)
    }
}