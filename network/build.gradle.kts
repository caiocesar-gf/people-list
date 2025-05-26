import java.io.FileInputStream
import java.util.Properties
import java.io.File


plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.android)
}

fun getPropertyFromAssets(propertyName: String): String {
    val properties = Properties()
    val assetsDir = file("src/main/assets")
    val propFile = File(assetsDir, "project.properties")

    return if (propFile.exists()) {
        properties.load(FileInputStream(propFile))
        properties.getProperty(propertyName) ?: ""
    } else {
        ""
    }
}


android {
    namespace = "com.project.network"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BASE_URL", "\"${getPropertyFromAssets("BASE_URL")}\"")

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

    buildFeatures {
        buildConfig = true
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
    implementation(project(":core"))
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.koin.android)
    implementation(libs.koin.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.firebase.firestore.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}