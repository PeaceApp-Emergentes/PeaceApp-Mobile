package com.innovatech.peaceapp.Map.Beans

data class PropertiesPlace (
    val type: String,
    val features: List<Feature>,
    val attribution: String
)

data class Feature(
    val properties: Property
)

data class Property (
    val mapbox_id: String,
    val full_address: String,
    val name: String,
    val name_preferred: String,
    val coordinates: Coordinates
)

data class Coordinates(
    val longitude: Double,
    val latitude: Double
)