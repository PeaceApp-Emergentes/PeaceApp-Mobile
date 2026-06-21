package com.innovatech.peaceapp.AI.Beans

data class ClassifyIncidentResponse(
    val incidentType: String?,
    val severity: String?,
    val summary: String?,
    val recommendedActions: List<String>?,
    val mock: Boolean
)
