plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.daffa.adminpanel"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.daffa.adminpanel"
        minSdk = 23
        targetSdk = 36

        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {

    implementation(platform("com.google.firebase:firebase-bom:34.18.0"))

    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
}
