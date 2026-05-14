package com.innovatech.peaceapp.Profile.Beans

import java.io.Serializable

class UserProfile : Serializable {
    var id: Int
    var name: String
    var lastname: String
    var phonenumber: String
    var email: String
    var userId: String
    var profileImage: String? = null

    constructor(
        id: Int,
        name: String,
        lastname: String,
        phone: String,
        email: String,
        userId: String,
        profile_image: String?
    ) {
        this.id = id
        this.name = name
        this.lastname = lastname
        this.phonenumber = phone
        this.email = email
        this.userId = userId
        this.profileImage = profile_image
    }

    constructor(
        name: String,
        lastname: String,
        phone: String,
        email: String,
        userId: String,
        profile_image: String?
    ) {
        this.id = 0
        this.name = name
        this.lastname = lastname
        this.phonenumber = phone
        this.email = email
        this.userId = userId
        this.profileImage = profile_image
    }
}
