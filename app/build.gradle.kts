plugins {
    alias(libs.plugins.android.application)
        //id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.rekoro"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.rekoro"
        minSdk = 24
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
    buildFeatures{
        viewBinding = true
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))
    implementation("com.google.firebase:firebase-analytics")


    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.firebase:firebase-firestore:24.9.0")

    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
        // implementation("com.rengwuxian.materialedittext:library:2.1.4")
   // implementation("uk.co.chrisjenx:calligraphy:2.3.0")
    implementation("com.google.android.material:material")





}
