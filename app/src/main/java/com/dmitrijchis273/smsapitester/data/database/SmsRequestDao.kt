package com.dmitrijchis273.smsapitester.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dmitrijchis273.smsapitester.data.models.SmsRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsRequestDao {
    @Insert
    suspend fun insertRequest(request: SmsRequest)

    @Query("SELECT * FROM sms_requests ORDER BY timestamp DESC LIMIT 100")
    fun getAllRequests(): Flow<List<SmsRequest>>

    @Query("SELECT * FROM sms_requests ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastRequest(): SmsRequest?

    @Query("DELETE FROM sms_requests")
    suspend fun clearAll()

    @Query("DELETE FROM sms_requests WHERE timestamp < datetime('now', '-30 days')")
    suspend fun clearOldRequests()
}
