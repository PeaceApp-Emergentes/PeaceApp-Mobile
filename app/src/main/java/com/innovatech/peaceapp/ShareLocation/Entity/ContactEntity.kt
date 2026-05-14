package com.innovatech.peaceapp.ShareLocation.Entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "favorites", indices = [Index("name", "phone", unique = true)])
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "phone")
    val phone: String?,

    @ColumnInfo(name = "image")
    val image: String?
)