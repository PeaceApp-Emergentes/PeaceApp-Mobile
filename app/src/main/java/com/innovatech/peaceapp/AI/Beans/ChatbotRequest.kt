package com.innovatech.peaceapp.AI.Beans

data class ChatbotRequest(
    val message: String,
    val context: String = "mobile-citizen"
)
