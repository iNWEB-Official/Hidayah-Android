import java.io.FileInputStream
import java.util.Properties

val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) {
        FileInputStream(keystorePropsFile).use { load(it) }
    }
}
// CI: env vars (HYD_*) সরাসরি পাসিং; লোকালে keystore.properties (gitignored)
val envStoreFile = System.getenv("HYD_KEYSTORE_PATH")?.takeIf { it.isNotBlank() && file(it).exists() }
val storeFilePath: String? = envStoreFile ?: keystoreProps.getProperty("storeFile")?.takeIf { file(it).exists() }
val isReleaseSigning = storeFilePath != null

fun signProp(envKey: String, propKey: String): String =
    System.getenv(envKey)?.takeIf { it.isNotBlank() }
        ?: keystoreProps.getProperty(propKey)
        ?: ""

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.hidayah.android"
    compileSdk = 34

    // রিলিজ সাইনিং — keystore.properties (gitignored, লোকাল) বা HYD_* env (CI); না থাকলে unsigned
    signingConfigs {
        if (isReleaseSigning) {
            create("release") {
                storeFile = file(storeFilePath!!)
                storePassword = signProp("HYD_KEYSTORE_PASSWORD", "storePassword")
                keyAlias = signProp("HYD_KEY_ALIAS", "keyAlias")
                keyPassword = signProp("HYD_KEY_PASSWORD", "keyPassword")
            }
        }
    }

    defaultConfig {
        applicationId = "com.hidayah.android"
        minSdk = 26
        targetSdk = 34
        versionCode = 20260010
        versionName = "2026.00.10"
    }

    buildTypes {
        release {
            if (isReleaseSigning) signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.8" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    // per-app language (AppCompatDelegate.setApplicationLocales) — সেটিংসের ভাষা অপশন
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.navigation:navigation-compose:2.7.6")

    implementation("androidx.datastore:datastore-preferences:1.0.0")

    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
}
