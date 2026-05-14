package com.innovatech.peaceapp.StartingPoint.Beans

data class UserAuthenticated (
    var id:Int,
    var username: String,
    var token: String,
    var message: String,
    var role: String
)