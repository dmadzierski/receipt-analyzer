plugins {
    id("org.springframework.boot") version "3.5.3"
}

dependencies {
    implementation(project(":adapters"))
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.data.jpa)
}

springBoot {
    mainClass.set("pl.madzierski.daniel.ReceiptAnalyzerServiceApplication")
}

description = "receipt-analyzer-service-monolith"
