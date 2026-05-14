package com.innovatech.peaceapp.Map.Interfaces

import com.innovatech.peaceapp.Map.Beans.PropertiesPlace
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaceHolderMapbox {
    @GET("reverse")
    fun getPlace(
        @Query("longitude") longitude: Double,
        @Query("latitude") latitude: Double,
        @Query("access_token") access_token: String
    ) : Call<PropertiesPlace>
}