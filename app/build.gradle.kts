plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.innovatech.peaceapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.innovatech.peaceapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // MAPBOX [Para mostrar mapas]
    implementation("com.mapbox.maps:android:11.6.1")
    // Search Mapbox
    implementation("com.mapbox.search:mapbox-search-android-ui:2.5.0")
    //Cloudinary
    implementation("com.cloudinary:cloudinary-android:3.0.2")

    // RETROFIT [Para consumir la api]
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // gson
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // PICASSO [Para mostrar imágenes]
    implementation("com.squareup.picasso:picasso:2.8")
    implementation(libs.play.services.location)

    // room
    val room_version = "2.5.0"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    // Views/Fragments Integration
    val nav_version = "2.8.0"
    implementation("androidx.navigation:navigation-fragment:$nav_version")
    implementation("androidx.navigation:navigation-ui:$nav_version")

    implementation("com.google.android.material:material:1.12.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}