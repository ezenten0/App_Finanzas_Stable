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
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation("com.google.firebase:firebase-admin:9.3.0")

    testImplementation(libs.spring.boot.starter.test)
}

tasks.test {
    useJUnitPlatform()
}
