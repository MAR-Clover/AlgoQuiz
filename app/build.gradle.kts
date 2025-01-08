import org.gradle.initialization.Environment
import java.util.Properties



plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")

}


android {
    buildFeatures {
        buildConfig = true
    }

    namespace = "com.example.algoquiz"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.algoquiz"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"



    }
    val localProperties = Properties()
    val localPropertiesFile = File(rootDir,"secret.properties")
    if(localPropertiesFile.exists() && localPropertiesFile.isFile){
        localPropertiesFile.inputStream().use{
            localProperties.load(it)
        }
    }
    buildTypes {
        release {

            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField("String","API_KEY_PROD",localProperties.getProperty("API_KEY"))


        }
        debug{
            buildConfigField("String","API_KEY",localProperties.getProperty("API_KEY"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    //-----Retro fit Dependancies-----
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    //-----Retro fit Dependancies-----

        // Firebase Firestore
    implementation("com.google.firebase:firebase-firestore")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation(libs.material.v190) // Replace with the latest version if needed
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.common.jvm)
    implementation(libs.firebase.firestore.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.testng)
    implementation (libs.gson)
    implementation("com.google.firebase:firebase-firestore-ktx")

}