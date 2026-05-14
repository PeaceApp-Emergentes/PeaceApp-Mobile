package com.innovatech.peaceapp

object GlobalUserEmail {
    var email: String = ""
        private set

    fun setEmail(email: String) {
        this.email = email
    }
}