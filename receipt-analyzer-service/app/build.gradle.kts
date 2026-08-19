dependencies {
    implementation(project(":domain"))
    implementation(libs.pdfbox)
    implementation(libs.tess4j)
    implementation(libs.commons.text)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation("jakarta.validation:jakarta.validation-api:4.0.0-M1")
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

description = "receipt-analyzer-service-app"
