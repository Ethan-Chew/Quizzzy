plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "sg.edu.np.mad.quizzzy"
    compileSdk = 34

    defaultConfig {
        applicationId = "sg.edu.np.mad.quizzzy"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        renderscriptTargetApi = 21
        renderscriptSupportModeEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.gson)
    implementation(libs.firebase.auth)
    implementation(libs.fragment)
    implementation(libs.zxing.android.embedded)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.messaging)
    implementation(libs.okhttp)
    implementation(libs.lifecycle.extensions)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")
    coreLibraryDesugaring("com.android.tools.desugar_jdk_libs:2.0.2")
    implementation("androidx.multidex:multidex:2.0.0")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition-chinese:16.0.0")
    implementation ("com.google.guava:guava:31.0.1-android")
    implementation ("androidx.concurrent:concurrent-futures:1.2.0")
    implementation ("androidx.camera:camera-core:1.3.4")
    implementation ("androidx.camera:camera-camera2:1.3.4")
    implementation ("androidx.camera:camera-lifecycle:1.3.4")
    implementation ("androidx.camera:camera-video:1.3.4")
    implementation ("androidx.camera:camera-view:1.3.4")
    implementation ("androidx.camera:camera-extensions:1.3.4")
    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")
    implementation("com.google.guava:guava:31.0.1-android")
    implementation("org.reactivestreams:reactive-streams:1.0.4")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("commons-codec:commons-codec:1.15")
    implementation("com.google.zxing:core:3.4.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}