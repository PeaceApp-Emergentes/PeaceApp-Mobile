package com.innovatech.peaceapp.AI.Models

import com.innovatech.peaceapp.AI.Interfaces.AiPlaceHolder
import com.innovatech.peaceapp.HttpUri
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = HttpUri.url

    // La IA (gpt-5.x) puede tardar 15-30s; sin estos timeouts OkHttp corta a los 10s.
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()

    val placeHolder: AiPlaceHolder = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AiPlaceHolder::class.java)
}
