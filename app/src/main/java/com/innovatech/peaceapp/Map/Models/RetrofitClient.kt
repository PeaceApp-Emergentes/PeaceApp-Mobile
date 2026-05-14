package com.innovatech.peaceapp.Map.Models
import com.innovatech.peaceapp.HttpUri
import com.innovatech.peaceapp.Map.Interfaces.PlaceHolder
import com.innovatech.peaceapp.Map.Interfaces.PlaceHolderMapbox
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    const val BASE_URL = HttpUri.url

    fun getClient(token: String): PlaceHolder {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(token)) // Agrega el token
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Usa el OkHttpClient que incluye el token
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlaceHolder::class.java)
    }
}

object RetrofitClientMapbox {
    const val BASE_URL = "https://api.mapbox.com/search/geocode/v6/"

    fun getClient(): PlaceHolderMapbox {
        val client = OkHttpClient.Builder()
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlaceHolderMapbox::class.java)
    }
}
