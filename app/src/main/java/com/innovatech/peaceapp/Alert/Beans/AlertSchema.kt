package com.innovatech.peaceapp.Alert.Beans

data class AlertSchema(
    var location: String,
    var type: String,
    var description: String?, // Optional field
    var userId: Int,
    var imageUrl: String?, // Optional field for image URL
    var reportId: Int // Optional field for the foreign key to a report
)
