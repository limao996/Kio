plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    val sources = android.sourceSets.map { set -> set.java.getSourceFiles() }
    from(sources)
}

afterEvaluate {
    publishing {
        publications {
            version = "1.0.0"
            create<MavenPublication>("product") {
                from(components["release"])
                groupId = "org.limao996.kio"
                artifactId = "Kio"
                version = version
                artifact(tasks["sourcesJar"])

                pom {
                    name.set("Kio")
                    url.set("https://github.com/limao996/Kio")
                    developers {
                        developer {
                            name.set("limao996")
                            email.set("limao7325@163.com")
                        }
                    }
                }
            }
        }
    }
}


android {
    namespace = "org.limao996.kio"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
        debug {
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
        jvmTarget = "1.8"
    }
    publishing {
        singleVariant("release")
        singleVariant("debug")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
}