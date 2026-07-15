import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.devtools.ksp")
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().also { props ->
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { props.load(it) }
    }
}
val updateKeystoreFile = file(
    keystoreProperties.getProperty(
        "updateStoreFile",
        System.getenv("MOMENTA_UPDATE_STORE_FILE") ?: "../keystore/momenta-update.jks"
    )
)
val hasUpdateKeystore = updateKeystoreFile.exists()

android {
    namespace = "com.bghitech.momenta"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bghitech.momenta"
        minSdk = 24
        targetSdk = 34
        versionCode = 72
        versionName = "0.2.72"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("update") {
            storeFile = updateKeystoreFile
            storePassword = keystoreProperties.getProperty(
                "updateStorePassword",
                System.getenv("MOMENTA_UPDATE_STORE_PASSWORD") ?: "android"
            )
            keyAlias = keystoreProperties.getProperty(
                "updateKeyAlias",
                System.getenv("MOMENTA_UPDATE_KEY_ALIAS") ?: "androiddebugkey"
            )
            keyPassword = keystoreProperties.getProperty(
                "updateKeyPassword",
                System.getenv("MOMENTA_UPDATE_KEY_PASSWORD") ?: "android"
            )
        }
        create("release") {
            storeFile = file(keystoreProperties.getProperty("storeFile", "../keystore/momenta.jks"))
            storePassword = keystoreProperties.getProperty("storePassword", "momenta123")
            keyAlias = keystoreProperties.getProperty("keyAlias", "momenta")
            keyPassword = keystoreProperties.getProperty("keyPassword", "momenta123")
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "Момент Dev")
            buildConfigField("String", "DEFAULT_SERVER_URL", "\"https://momenta.bghitech.ru\"")
            buildConfigField("String", "MEDIA_BASE_URL", "\"https://momenta-media.bghitech.ru\"")
        }
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            resValue("string", "app_name", "Момент Staging")
            buildConfigField("String", "DEFAULT_SERVER_URL", "\"https://momenta.bghitech.ru\"")
            buildConfigField("String", "MEDIA_BASE_URL", "\"https://momenta-media.bghitech.ru\"")
        }
        create("prod") {
            dimension = "environment"
            resValue("string", "app_name", "Момент")
            buildConfigField("String", "DEFAULT_SERVER_URL", "\"https://momenta.bghitech.ru\"")
            buildConfigField("String", "MEDIA_BASE_URL", "\"https://momenta-media.bghitech.ru\"")
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName(if (hasUpdateKeystore) "update" else "debug")
            isMinifyEnabled = false
            buildConfigField("Boolean", "LOGGING_ENABLED", "true")
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("Boolean", "LOGGING_ENABLED", "false")
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.5"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.exifinterface:exifinterface:1.3.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Retrofit + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // CameraX
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // Coil
    implementation("io.coil-kt:coil-compose:2.5.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Accompanist
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.9")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.01.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}

tasks.register("verifyInstallableProdApk") {
    description = "Verifies the prod debug APK is installable (no testOnly, signed, correct package)"
    group = "verification"
    dependsOn("assembleProdDebug")

    doLast {
        val apkDir = layout.buildDirectory.dir("outputs/apk/prod/debug").get().asFile
        val apkFiles = apkDir.listFiles()?.filter { it.extension == "apk" }
        val apk = apkFiles?.firstOrNull()
        require(apk != null && apk.exists()) {
            "APK not found in ${apkDir.absolutePath}. Run assembleProdDebug first."
        }
        require(apk.length() > 0) { "APK is empty: ${apk.absolutePath}" }

        val manifest = file("build/intermediates/merged_manifests/prodDebug/AndroidManifest.xml")
        if (manifest.exists()) {
            val content = manifest.readText()
            require(!content.contains("""android:testOnly="true""")) {
                "FAIL: merged manifest contains android:testOnly=true. APK will not install via Package Installer."
            }
            require(content.contains("com.bghitech.momenta")) {
                "FAIL: merged manifest does not contain expected package com.bghitech.momenta"
            }
        }

        println("OK: ${apk.name} (${apk.length()} bytes)")
        println("Package: com.bghitech.momenta")
        println("APK verification passed.")
    }
}
