package com.innovatech.peaceapp.Profile.Models

import com.innovatech.peaceapp.HttpUri
import com.innovatech.peaceapp.Map.Models.AuthInterceptor
import com.innovatech.peaceapp.Profile.Interface.PlaceHolder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    const val BASE_URL = HttpUri.url

    // Client without token
    val placeHolder: PlaceHolder = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PlaceHolder::class.java)

    // Client with token
    fun getClient(token: String? = null): PlaceHolder {
        val clientBuilder = OkHttpClient.Builder()
        if (token != null) {
            clientBuilder.addInterceptor(AuthInterceptor(token))
        }
        val client = clientBuilder.build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlaceHolder::class.java)
    }
}