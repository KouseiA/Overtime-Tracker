import java.util.Properties
import java.io.FileInputStream

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.ottracker"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.ottracker"
        minSdk = 24
        targetSdk = 36
        versionCode = 12
        versionName = "1.5.2"
    }

    // ── Release signing ─────────────────────────────────────────────────────
    // Reads credentials from local.properties (which is .gitignored).
    // Add these 4 lines to your local.properties:
    //   RELEASE_STORE_FILE=../ot-tracker-release.jks
    //   RELEASE_STORE_PASSWORD=your_store_password
    //   RELEASE_KEY_ALIAS=ot-tracker
    //   RELEASE_KEY_PASSWORD=your_key_password
    val localProps = Properties().apply {
        val f = rootProject.file("local.properties")
        if (f.exists()) {
            FileInputStream(f).use { load(it) }
        }
    }

    signingConfigs {
        create("release") {
            storeFile     = file(localProps.getProperty("RELEASE_STORE_FILE", "../ot-tracker-release.jks"))
            storePassword = localProps.getProperty("RELEASE_STORE_PASSWORD", "ottracker123")
            keyAlias      = localProps.getProperty("RELEASE_KEY_ALIAS", "ot-tracker")
            keyPassword   = localProps.getProperty("RELEASE_KEY_PASSWORD", "ottracker123")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
      compose = true
      aidl = false
      buildConfig = false
      shaders = false
    }

    packaging {
      resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
      }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.extended)

  // Jetpack Glance (Compose-powered App Widgets)
  implementation(libs.androidx.glance.appwidget)
  implementation(libs.androidx.glance.material3)
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation("org.json:json:20240303")

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)
}
