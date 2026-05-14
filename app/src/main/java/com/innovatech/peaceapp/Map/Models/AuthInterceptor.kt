package com.innovatech.peaceapp.Map.Models

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val token: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token") // Agrega el token al encabezado Authorization
            .build()
        return chain.proceed(request)
    }
}
