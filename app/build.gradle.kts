import org.gradle.kotlin.dsl.androidTestImplementation
import org.gradle.kotlin.dsl.testImplementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.androidx.navigation.safeargs.kotlin)
    id("kotlin-kapt")
    id("kotlin-parcelize")
}

android {
    namespace = "id.istts.aplikasiadopsiterumbukarang"
    compileSdk = 35

    defaultConfig {
        applicationId = "id.istts.aplikasiadopsiterumbukarang"
        minSdk = 30
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}



dependencies {
    // --- Your Existing, Working Dependencies ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.cardview)

    // Networking & Image Loading (for XML)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.code.gson:gson:2.10.1")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // Google Play Services
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.libraries.places:places:3.4.0")
    implementation("com.google.android.play:integrity:1.3.0")

    // Room
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.coroutines.core)

    // --- JETPACK COMPOSE DEPENDENCIES ---

    // CORRECTED BoM VERSION: Using a real, stable version
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Core Compose Libraries (versions are managed by the BoM, so no need to specify them here)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose")

    // ViewModel and LiveData Integration
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose")

    // Image Loading for Compose
    implementation("io.coil-kt:coil-compose:2.6.0")

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

// TAMBAHKAN SEMUA DI BAWAH INI
// 1. Untuk testing LiveData (InstantTaskExecutorRule)
    testImplementation("androidx.arch.core:core-testing:2.2.0")

// 2. Untuk testing Coroutines (TestDispatcher, runTest)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

// 3. Untuk Mocking (Mockito)
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-inline:5.2.0") // Penting untuk mock class final di Kotlin
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.3.1") // Helper functions untuk Kotlin
    // 4. TAMBAHKAN INI: Library MockK untuk mocking di Kotlin
    testImplementation("io.mockk:mockk:1.13.11")
    testImplementation("io.mockk:mockk-android:1.13.11") // Penting untuk mockkStatic

    // 5. TAMBAHKAN INI: Library Turbine untuk testing Flow
    testImplementation("app.cash.turbine:turbine:1.1.0")

    implementation("com.airbnb.android:lottie:6.4.0")
}
