plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "org.limao996.kiotest"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.limao996.kio.test"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.1.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
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
}

dependencies {
    implementation(project(":kio"))
    api("androidx.core:core-ktx:1.13.1")
    api("androidx.appcompat:appcompat:1.6.1")
    api("com.google.android.material:material:1.12.0")
}