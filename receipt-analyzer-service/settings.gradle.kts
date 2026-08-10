pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "receipt-analyzer-service"

include(":monolith")
include(":adapters")
include(":app")
include(":domain")
