rootProject.name = "football-server"
include("app", "api", "auth", "domain")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
