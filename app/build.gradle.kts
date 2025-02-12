plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.genc.platerate"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.genc.platerate"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.credentials:credentials:1.2.2")
    implementation(libs.firebase.firestore)

    debugImplementation("androidx.compose.ui:ui-tooling:1.6.8")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.8")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.6.8")



    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.googleid)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    implementation ("androidx.compose.material:material-icons-extended:1.0.1")
    implementation ("androidx.graphics:graphics-shapes:1.0.0-rc01")


    implementation("androidx.compose.foundation:foundation:1.0.5")
    implementation("androidx.compose.material:material:1.0.5")
    implementation("androidx.compose.ui:ui-tooling:1.0.5")


    implementation("com.google.firebase:firebase-auth-ktx:21.0.3")
    implementation("com.google.android.gms:play-services-auth:20.5.0")
    implementation("androidx.navigation:navigation-compose:2.6.0")
    implementation("com.google.firebase:firebase-firestore-ktx:24.0.0")

    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    implementation("io.coil-kt:coil-compose:2.6.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
