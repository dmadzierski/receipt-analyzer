dependencies {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.commons.text)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testRuntimeOnly(libs.junit.platform.launcher)
}

description = "receipt-analyzer-service-domain"
