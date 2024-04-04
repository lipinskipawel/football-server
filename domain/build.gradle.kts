plugins {
    id("football-server.library-conventions")
}

dependencies {
    implementation(project(":api"))
    implementation(libs.lipinskipawel.gameEngine)

    implementation(libs.org.slf4j.api)
}
