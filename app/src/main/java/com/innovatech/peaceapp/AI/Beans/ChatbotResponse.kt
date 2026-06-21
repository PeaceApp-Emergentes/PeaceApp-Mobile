package com.innovatech.peaceapp.AI.Beans

data class ChatbotResponse(
    val answer: String,
    val suggestedActions: List<String>?,
    val mock: Boolean
)
