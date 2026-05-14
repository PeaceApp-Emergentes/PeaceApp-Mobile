package com.innovatech.peaceapp.DB.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationModel (
    @PrimaryKey(autoGenerate = true)
    val id:Int?,

    @ColumnInfo(name = "latitude")
    val latitude:Double?,

    @ColumnInfo(name = "longitude")
    val longitude:Double?,

    @ColumnInfo(name = "first_address")
    val firstAddress:String?,

    @ColumnInfo(name = "second_address")
    val secondAddress:String?,

)