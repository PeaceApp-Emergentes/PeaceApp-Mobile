package com.innovatech.peaceapp.AI.Beans

data class AnalyzeEvidenceRequest(
    val evidenceUrl: String,
    val evidenceType: String,
    val description: String?
)
