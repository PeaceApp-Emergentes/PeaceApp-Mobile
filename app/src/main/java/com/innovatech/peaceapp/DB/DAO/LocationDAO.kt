package com.innovatech.peaceapp.DB.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.innovatech.peaceapp.DB.Entities.LocationModel

@Dao
interface LocationDAO {
    @Insert
    fun insert(report: LocationModel)

    @Query("select * from locations")
    fun listLocations():List<LocationModel>

    @Delete
    fun delete(report: LocationModel)
}