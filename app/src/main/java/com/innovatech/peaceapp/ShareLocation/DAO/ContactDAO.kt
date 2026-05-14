package com.innovatech.peaceapp.ShareLocation.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.innovatech.peaceapp.ShareLocation.Entity.ContactEntity

@Dao
interface ContactDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(contact: ContactEntity)

    @Query("SELECT * FROM favorites")
    fun getAll(): List<ContactEntity>

    @Delete
    fun delete(contact: ContactEntity)
}