package com.innovatech.peaceapp.Map.Beans

import java.io.Serializable

data class Report(
    var id: Int,
    var createdAt: String,
    var updatedAt: String,
    var userId: Int,
    var description: String,
    var title: String,
    var type: String,
    var imageUrl: String?,
    var location: String,
    val latitude: String,
    val longitude: String,
    var state: String,
    var rejectionReason: String?
) : Serializable