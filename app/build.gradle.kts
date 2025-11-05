plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.foodmap"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.foodmap"
        minSdk = 24
        targetSdk = 36
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    // --- Dependências principais do AndroidX (antes eram libs.) ---
    // Usei versões estáveis e recentes
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.9.0")
    // Versão estável que o seu log mencionou
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // --- Testes (antes eram libs.) ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // --- Retrofit (Versões atualizadas do seu log) ---
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    // --- jbcrypt (mantido) ---
    implementation("org.mindrot:jbcrypt:0.4")

    // --- TensorFlow Lite (Versões ATUALIZADAS do seu log) ---
    implementation("org.tensorflow:tensorflow-lite:2.17.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.17.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.5.0")

    // --- CameraX (Versões atualizadas do seu log) ---
    implementation("androidx.camera:camera-core:1.5.1")
    implementation("androidx.camera:camera-camera2:1.5.1")
    implementation("androidx.camera:camera-lifecycle:1.5.1")
    implementation("androidx.camera:camera-view:1.5.1")

    // --- Gson (mantido) ---
    implementation("com.google.code.gson:gson:2.10.1")
}