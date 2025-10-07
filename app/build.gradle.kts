plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.example.arsip"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.arsip"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"drgrdsnif\"")
        buildConfigField("String", "CLOUDINARY_PRESET", "\"arsipbuku\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

}

dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Firebase (tanpa Storage)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)        // ⬅️ bukan -ktx
    implementation(libs.firebase.firestore)

    // Hilt
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // Lainnya
    implementation(libs.okhttp)
    implementation(libs.org.json)
    implementation(libs.coil.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.material)

    implementation(platform("androidx.compose:compose-bom:2025.08.00")) // atau versi BOM kamu
    implementation("androidx.compose.ui:ui-tooling-preview") // <-- WAJIB utk anotasi @Preview
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.maps.android:maps-compose:4.3.3")
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("androidx.compose.material:material-icons-extended")
}
