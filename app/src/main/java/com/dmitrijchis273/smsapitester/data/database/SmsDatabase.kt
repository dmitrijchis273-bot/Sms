package com.dmitrijchis273.smsapitester.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dmitrijchis273.smsapitester.data.models.SmsRequest

@Database(entities = [SmsRequest::class], version = 1)
@TypeConverters(Converters::class)
abstract class SmsDatabase : RoomDatabase() {
    abstract fun smsRequestDao(): SmsRequestDao

    companion object {
        const val DATABASE_NAME = "sms_api_tester.db"
    }
}
