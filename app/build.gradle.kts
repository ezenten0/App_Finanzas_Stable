@file:Suppress("DEPRECATION")

import java.util.Properties
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.api.GradleException

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("jacoco")
}

// 🔹 Leer local.properties en el módulo app
val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        load(file.inputStream())
    }
}


android {
    namespace = "com.example.app_finanzas"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.app_finanzas"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 🔹 URLs base inyectadas desde local.properties
        val ledgerUrl = localProps.getProperty("LEDGER_BASE_URL", "http://10.0.2.2:8080")
        val riskUrl   = localProps.getProperty("RISK_BASE_URL",   "http://10.0.2.2:8081")
        val notifUrl  = localProps.getProperty("NOTIF_BASE_URL",  "http://10.0.2.2:8082")

        buildConfigField("String", "LEDGER_BASE_URL", "\"$ledgerUrl\"")
        buildConfigField("String", "RISK_BASE_URL",   "\"$riskUrl\"")
        buildConfigField("String", "NOTIF_BASE_URL",  "\"$notifUrl\"")
    }
    val keystorePath = "$rootDir/keystore/finanzas-release.jks"
    val keystorePassword = "Finanzas123!"
    val keyAlias = "finanzasReleaseKey"
    val keyPassword = keystorePassword
    signingConfigs {
        create("release") {
            storeFile = file(keystorePath)
            storePassword = keystorePassword
            this.keyAlias = keyAlias
            this.keyPassword = keyPassword
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation("com.google.accompanist:accompanist-navigation-animation:0.36.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp.logging)
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")
    implementation(libs.moshi.kotlin)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.websockets)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.firebase.dataconnect)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.0.21")
    // Permite que JUnit Platform ejecute pruebas de JUnit 4
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:6.0.1") // usa la misma versión que tu JUnit 5
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    // Declare the dependency for the Cloud Firestore library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-firestore")
}

jacoco {
    toolVersion = "0.8.11"
}

val jacocoFileFilter = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*\$Companion.class"
)

// Clases compiladas del build debug de Kotlin
val jacocoDebugTree = fileTree("$buildDir/tmp/kotlin-classes/debug") {
    exclude(jacocoFileFilter)
}

// Código fuente principal (Java + Kotlin)
val jacocoMainSrc = files(
    "$projectDir/src/main/java",
    "$projectDir/src/main/kotlin"
)


// Reporte de cobertura para testDebugUnitTest
val jacocoDebugUnitTestReport by tasks.registering(JacocoReport::class) {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    classDirectories.setFrom(jacocoDebugTree)
    sourceDirectories.setFrom(files(jacocoMainSrc))
    executionData.setFrom(files("$buildDir/jacoco/testDebugUnitTest.exec"))
}

// Verificación de cobertura mínima
val jacocoDebugCoverageVerification by tasks.registering(JacocoCoverageVerification::class) {
    dependsOn(jacocoDebugUnitTestReport)

    classDirectories.setFrom(jacocoDebugTree)
    sourceDirectories.setFrom(jacocoMainSrc)
    executionData.setFrom(files("$buildDir/jacoco/testDebugUnitTest.exec"))

    violationRules {
        rule {
            limit {
                minimum = "0.02".toBigDecimal() // Debería ser 80% de cobertura mínima
            }
        }
    }
}

// Cuando se ejecute testDebugUnitTest, al final corre la verificación de Jacoco
tasks.withType<Test>().configureEach {
    // Esto ya lo tenías
    if (name == "testDebugUnitTest") {
        finalizedBy(jacocoDebugCoverageVerification)
    }

    // 🔍 Log detallado de los tests
    testLogging {
        // Qué eventos registrar
        events(
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT,
            org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
        )

        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showCauses = true
        showExceptions = true
        showStackTraces = true
        showStandardStreams = true
    }
}


// La tarea check también depende de la verificación de cobertura
tasks.named("check") {
    dependsOn(jacocoDebugCoverageVerification)
}
