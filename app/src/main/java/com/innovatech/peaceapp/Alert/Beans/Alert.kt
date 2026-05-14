package com.innovatech.peaceapp.Alert.Beans

class Alert {
    var id: Int = 0  // Default value for id
    var location: String
    var type: String
    var description: String?
    var idUser: Int
    var imageUrl: String? // Property for the image URL
    var idReport: Long? // Foreign key for report ID, can be nullable

    // Constructor with id and idReport (for cases where the Alert is already created)
    constructor(id: Int, location: String, type: String, description: String?, idUser: Int, imageUrl: String?, idReport: Long?) {
        this.id = id
        this.location = location
        this.type = type
        this.description = description
        this.idUser = idUser
        this.imageUrl = imageUrl
        this.idReport = idReport // Initialize idReport
    }

    // Constructor without id but with idReport (for new alerts being created)
    constructor(location: String, type: String, description: String?, idUser: Int, imageUrl: String?, idReport: Long?) {
        this.location = location
        this.type = type
        this.description = description
        this.idUser = idUser
        this.imageUrl = imageUrl
        this.idReport = idReport // Initialize idReport
    }
}
