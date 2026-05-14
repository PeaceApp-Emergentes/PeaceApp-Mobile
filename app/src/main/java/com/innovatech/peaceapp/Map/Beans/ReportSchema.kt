data class ReportSchema(
    var title: String,
    var description: String,
    var type: String,
    var userId: Int,
    var imageUrl: String?,
    var location: String,
    var latitude: String,
    var longitude: String,
    var state: String = "PENDING",
    var rejectionReason: String? = ""
)
