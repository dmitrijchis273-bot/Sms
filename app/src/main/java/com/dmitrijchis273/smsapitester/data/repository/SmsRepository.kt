package com.dmitrijchis273.smsapitester.data.repository

import com.dmitrijchis273.smsapitester.data.database.SmsRequestDao
import com.dmitrijchis273.smsapitester.data.models.RequestStatus
import com.dmitrijchis273.smsapitester.data.models.SmsApiRequest
import com.dmitrijchis273.smsapitester.data.models.SmsRequest
import com.dmitrijchis273.smsapitester.data.network.SmsApiService
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject

class SmsRepository @Inject constructor(
    private val apiService: SmsApiService,
    private val dao: SmsRequestDao
) {
    fun getHistoryFlow(): Flow<List<SmsRequest>> = dao.getAllRequests()

    suspend fun sendSmsCode(phoneNumber: String): Result<String> {
        return try {
            val response = apiService.sendSmsCode(SmsApiRequest(phoneNumber))
            
            val status = if (response.isSuccessful) RequestStatus.SUCCESS else RequestStatus.FAILED
            val errorMsg = if (!response.isSuccessful) response.message() else null
            
            val request = SmsRequest(
                phoneNumber = phoneNumber,
                status = status,
                errorMessage = errorMsg,
                timestamp = LocalDateTime.now(),
                responseCode = response.code()
            )
            dao.insertRequest(request)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            val request = SmsRequest(
                phoneNumber = phoneNumber,
                status = RequestStatus.FAILED,
                errorMessage = e.message,
                timestamp = LocalDateTime.now()
            )
            dao.insertRequest(request)
            Result.failure(e)
        }
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }

    suspend fun getLastRequest(): SmsRequest? = dao.getLastRequest()
}
