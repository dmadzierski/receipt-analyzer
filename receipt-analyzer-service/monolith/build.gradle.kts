plugins {
    id("org.springframework.boot") version "3.5.3"
}

dependencies {
    implementation(project(":adapters"))
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.data.jpa)

    testImplementation(project(":app"))
    testImplementation(project(":domain"))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

springBoot {
    mainClass.set("pl.madzierski.daniel.ReceiptAnalyzerServiceApplication")
}

description = "receipt-analyzer-service-monolith"
