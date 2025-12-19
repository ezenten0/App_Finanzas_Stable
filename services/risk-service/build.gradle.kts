plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation("com.google.firebase:firebase-admin:9.3.0")
    implementation(libs.firebase.common)
    runtimeOnly(libs.h2)
    runtimeOnly(libs.postgresql)
    implementation("org.flywaydb:flyway-core")
    testImplementation(libs.spring.boot.starter.test)
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}

tasks.test {
    useJUnitPlatform()
}
