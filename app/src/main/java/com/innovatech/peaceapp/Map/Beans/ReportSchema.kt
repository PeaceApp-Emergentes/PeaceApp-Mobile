data class ReportSchema(
    var title: String,
    var description: String,
    var type: String,
    var userId: Int,
    var imageUrl: String?,
    var videoUrl: String? = null,
    var audioUrl: String? = null,
    var location: String,
    var district: String? = null,
    var latitude: String,
    var longitude: String,
    var state: String = "PENDING",
    var rejectionReason: String? = "",
    var isEmergency: Boolean = false
)
