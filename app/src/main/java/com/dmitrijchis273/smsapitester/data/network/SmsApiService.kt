package com.dmitrijchis273.smsapitester.data.network

import com.dmitrijchis273.smsapitester.data.models.SmsApiRequest
import com.dmitrijchis273.smsapitester.data.models.SmsApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SmsApiService {
    @POST("/api/sms/send")
    suspend fun sendSmsCode(
        @Body request: SmsApiRequest
    ): Response<SmsApiResponse>
}
