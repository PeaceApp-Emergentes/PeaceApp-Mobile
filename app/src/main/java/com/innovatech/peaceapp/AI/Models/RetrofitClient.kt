package com.innovatech.peaceapp.AI.Models

import com.innovatech.peaceapp.AI.Interfaces.AiPlaceHolder
import com.innovatech.peaceapp.HttpUri
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = HttpUri.url

    val placeHolder: AiPlaceHolder = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AiPlaceHolder::class.java)
}
