package com.innovatech.peaceapp.AI.Interfaces

import com.innovatech.peaceapp.AI.Beans.ChatbotRequest
import com.innovatech.peaceapp.AI.Beans.ChatbotResponse
import com.innovatech.peaceapp.AI.Beans.ClassifyIncidentRequest
import com.innovatech.peaceapp.AI.Beans.ClassifyIncidentResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AiPlaceHolder {
    @POST("api/v1/ai/chatbot")
    fun chatbot(@Body request: ChatbotRequest): Call<ChatbotResponse>

    @POST("api/v1/ai/classify-incident")
    fun classifyIncident(@Body request: ClassifyIncidentRequest): Call<ClassifyIncidentResponse>
}
