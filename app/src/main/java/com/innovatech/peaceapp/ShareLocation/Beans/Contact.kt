package com.innovatech.peaceapp.ShareLocation.Beans

class Contact {
    var id: Int = 0
    var name: String
    var phone: String
    // DESCUBRIR cómo es que se almacenan la imagen del contacto en la base de datos
    var image: String
    var isSelected:Boolean = false

    constructor(id: Int, name: String, phone: String, image: String, isSelected: Boolean) {
        this.id = id
        this.name = name
        this.phone = phone
        this.image = image
    }
    constructor(name: String, phone: String, image: String) {
        this.name = name
        this.phone = phone
        this.image = image
    }
}