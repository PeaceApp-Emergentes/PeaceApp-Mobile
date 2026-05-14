package com.innovatech.peaceapp.Profile.Interface

import com.innovatech.peaceapp.Profile.Beans.UserEditSchema
import com.innovatech.peaceapp.Profile.Beans.UserPasswordSchema
import com.innovatech.peaceapp.Profile.Beans.UserProfile
import com.innovatech.peaceapp.Profile.Beans.UserProfileSchema
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PlaceHolder {
    //PUT
    ///api/v1/users/{id}
    @PUT("api/v1/users/{id}")
    fun updateUser(@Path("id") id: Int, @Body userProfile: UserEditSchema): Call<UserProfile>

    @PUT("api/v1/users/change-password/{id}")
    fun changeUserPassword(@Path("id") id: Int, @Body userPassword: UserPasswordSchema): Call<UserProfile>

    //POST
    ///api/v1/users
    @POST("api/v1/users")
    fun createUser(@Body userProfileSchema: UserProfileSchema): Call<UserProfile>

    //GET
    ///api/v1/users/{email}
    @GET("api/v1/users/email/{email}")
    fun getUserByEmail(@Path("email") email: String): Call<UserProfile>

    //DELETE
    ///api/v1/users/{id}
    @DELETE("api/v1/users/{id}")
    fun deleteUser(@Path("id") id: Long): Call<Void>

}