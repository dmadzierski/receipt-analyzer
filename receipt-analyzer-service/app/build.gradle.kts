dependencies {
    implementation(project(":domain"))
    implementation(libs.pdfbox)
    implementation(libs.tess4j)
    implementation(libs.commons.text)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

description = "receipt-analyzer-service-app"
