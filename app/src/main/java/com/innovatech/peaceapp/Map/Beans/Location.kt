package Beans

class Location {
    var id: Int
    var createdAt: String
    var updatedAt: String
    var idReport: Int
    var longitude: Double
    var latitude: Double

    constructor(id: Int, createdAt: String, updatedAt: String, idReport: Int, longitude: String, latitude: String) {
        this.id = id
        this.createdAt = createdAt
        this.updatedAt = updatedAt
        this.idReport = idReport
        this.longitude = longitude.toDouble()
        this.latitude = latitude.toDouble()
    }

    constructor(latitude: String,  longitude: String, idReport: String) {
        this.latitude = latitude.toDouble()
        this.longitude = longitude.toDouble()
        this.idReport = idReport.toInt()
        this.id = 0
        this.createdAt = ""
        this.updatedAt = ""
    }
}