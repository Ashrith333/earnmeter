plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.earnmeter.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.earnmeter.app"
        minSdk = 26 // Android 8.0 Oreo
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Configurable feature names via BuildConfig
        buildConfigField("String", "FEATURE_SMART_ASSIST_NAME", "\"Smart Assist\"")
        buildConfigField("String", "FEATURE_TRACK_PROFITS_NAME", "\"Track Profits\"")
        
        // Supabase configuration
        buildConfigField("String", "SUPABASE_URL", "\"https://jfdcguuegggerpvvgxdc.supabase.co\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"YOUR_ANON_KEY_HERE\"")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            // Override feature names for debug if needed
            buildConfigField("String", "FEATURE_SMART_ASSIST_NAME", "\"Smart Assist\"")
            buildConfigField("String", "FEATURE_TRACK_PROFITS_NAME", "\"Track Profits\"")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    flavorDimensions += "version"
    productFlavors {
        create("standard") {
            dimension = "version"
            buildConfigField("String", "FEATURE_SMART_ASSIST_NAME", "\"Smart Assist\"")
            buildConfigField("String", "FEATURE_TRACK_PROFITS_NAME", "\"Track Profits\"")
        }
        create("custom") {
            dimension = "version"
            // Can be overridden for white-label versions
            buildConfigField("String", "FEATURE_SMART_ASSIST_NAME", "\"Ride Assistant\"")
            buildConfigField("String", "FEATURE_TRACK_PROFITS_NAME", "\"Profit Tracker\"")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    
    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    
    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.realtime)
    implementation(libs.supabase.storage)
    
    // Ktor client for Supabase
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.utils)
    
    // Kotlin Serialization
    implementation(libs.kotlinx.serialization.json)
    
    // DataStore for local preferences
    implementation(libs.androidx.datastore.preferences)
    
    // Room Database for offline caching
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    
    // WorkManager for background tasks
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    kapt(libs.androidx.hilt.compiler)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    
    // Accompanist for permissions
    implementation(libs.accompanist.permissions)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
}

