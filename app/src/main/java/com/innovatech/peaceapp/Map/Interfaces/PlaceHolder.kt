package com.innovatech.peaceapp.Map.Interfaces

import Beans.Location
import Beans.LocationSchema
import ReportSchema
import com.innovatech.peaceapp.Alert.Beans.Alert
import com.innovatech.peaceapp.Alert.Beans.AlertSchema
import com.innovatech.peaceapp.Map.Beans.Report
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PlaceHolder {

    // -------------------------------------------------------------
    // LOCATIONS
    // -------------------------------------------------------------
    @GET("api/v1/locations")
    fun getLocations(): Call<List<Location>>

    @POST("api/v1/locations")
    fun postLocation(@Body locationSchema: LocationSchema): Call<Location>


    // -------------------------------------------------------------
    // REPORTS
    // -------------------------------------------------------------
    @GET("api/v1/reports")
    fun getAllReports(): Call<List<Report>>

    @GET("api/v1/reports/user/{id}")
    fun getMyReports(@Path("id") id: Int): Call<List<Report>>

    @POST("api/v1/reports")
    fun postReport(@Body report: ReportSchema): Call<Report>

    @DELETE("api/v1/reports/{id}")
    fun deleteReport(@Path("id") id: Int): Call<Void>

    // -------------------------------------------------------------
    // GET REPORT BY ID
    // -------------------------------------------------------------
    @GET("api/v1/reports/{id}")
    fun getReportById(@Path("id") id: Int): Call<Report>

    // -------------------------------------------------------------
    // CHECK IF REPORT EXISTS
    // -------------------------------------------------------------
    @GET("api/v1/reports/{id}/exists")
    fun reportExists(@Path("id") id: Int): Call<Boolean>


    // -------------------------------------------------------------
    // GET PUBLIC REPORTS (APPROVED)
    // -------------------------------------------------------------
    @GET("api/v1/reports/public")
    fun getPublicReports(): Call<List<Report>>


    // -------------------------------------------------------------
    // CHANGE STATE → IN REVIEW
    // -------------------------------------------------------------
    @PUT("api/v1/reports/{id}/review")
    fun markInReview(@Path("id") id: Int): Call<Report>

    // -------------------------------------------------------------
    // CHANGE STATE → APPROVED
    // -------------------------------------------------------------
    @PUT("api/v1/reports/{id}/approve")
    fun approve(@Path("id") id: Int): Call<Report>

    // -------------------------------------------------------------
    // CHANGE STATE → REJECTED
    // -------------------------------------------------------------
    @PUT("api/v1/reports/{id}/reject")
    fun reject(
        @Path("id") id: Int,
        @Body reason: Map<String, String> // {"reason": "texto"}
    ): Call<Report>



    // -------------------------------------------------------------
    // ALERTS
    // -------------------------------------------------------------
    @POST("api/v1/alerts")
    fun postAlert(@Body alertSchema: AlertSchema): Call<Alert>

    @GET("api/v1/alerts")
    fun getAllAlerts(): Call<List<Alert>>

    @DELETE("api/v1/alerts/user/{userId}")
    fun deleteAlertsByUserId(@Path("userId") userId: Int): Call<Void>
}
