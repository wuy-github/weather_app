import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")
    id("com.google.gms.google-services")
}

// Đọc file local.properties
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

val visualCrossingApiKey = localProperties.getProperty("VISUALCROSSING_API_KEY_OF_DAT") ?: ""

android {
    namespace = "com.example.weatherassistant"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.weatherassistant"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "VISUALCROSSING_API_KEY_OF_DAT", "\"$visualCrossingApiKey\"")

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
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.12"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation)

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation(libs.play.services.maps)
//    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.14.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Parse Json data
    implementation("com.google.android.gms:play-services-location:21.0.1") // Location Services Library
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // hoặc mới nhất

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    implementation("androidx.datastore:datastore-preferences:1.1.1")

    //navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    //icon
    implementation("androidx.compose.material:material-icons-extended-android:1.6.7")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.33.2-alpha")
    // MapLibre install:
    implementation("org.maplibre.gl:android-sdk:9.5.0")
    // Coroutines:
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    // Import the Firebase BoM
    implementation ( platform ( "com.google.firebase:firebase-bom:33.16.0" ))
    // Firebase Notification Pushing service:
    implementation("com.google.firebase:firebase-messaging:24.1.2")
    // Firebase Store service:
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.4")
    // Firebase Authenticating service:
    implementation("com.google.firebase:firebase-auth-ktx:23.2.1")
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation ( "com.google.firebase:firebase-analytics" )
    // Add WorkManager for Periodic Work: (Here, i use for automatically notifing merchanism)
    implementation("androidx.work:work-runtime-ktx:2.10.2")


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}