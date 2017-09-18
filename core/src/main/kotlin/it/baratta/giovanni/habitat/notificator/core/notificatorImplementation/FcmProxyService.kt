package it.baratta.giovanni.habitat.notificator.core.notificatorImplementation

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface FcmProxyService{

    @POST("/send")
    fun sendNotification(@Header("Content-Type") value : String, @Body jsonData : String) : Call<String>
}