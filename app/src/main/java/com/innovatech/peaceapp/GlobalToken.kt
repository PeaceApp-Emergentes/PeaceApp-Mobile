package com.innovatech.peaceapp

object GlobalToken {
    var token: String = ""
    private set

    fun setToken(token: String) {
        this.token = token
    }
}