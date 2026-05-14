package com.innovatech.peaceapp.StartingPoint.Interface

import com.innovatech.peaceapp.StartingPoint.Beans.User
import com.innovatech.peaceapp.StartingPoint.Beans.UserAuth
import com.innovatech.peaceapp.StartingPoint.Beans.UserAuthenticated
import com.innovatech.peaceapp.StartingPoint.Beans.UserSchema
import retrofit2.Call

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
interface PlaceHolder {

    @POST("api/v1/authentication/sign-up")
    fun signUp(@Body userSchema: UserSchema): Call<User>

    @POST("api/v1/authentication/sign-in")
    fun signIn(@Body userAuth: UserAuth): Call<UserAuthenticated>

    @PUT("api/v1/authentication/change-password")
    fun changePassword(@Body userAuth: UserAuth): Call<UserAuthenticated>

}
