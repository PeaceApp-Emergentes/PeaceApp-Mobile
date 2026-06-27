package com.innovatech.peaceapp.AI.Beans

data class AnalyzeEvidenceResponse(
    val detectedType: String?,
    val validImage: Boolean?,
    val summary: String?,
    val observedSignals: List<String>?,
    val requiresHumanReview: Boolean?,
    val mock: Boolean
)
