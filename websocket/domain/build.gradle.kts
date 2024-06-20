plugins {
    id("football-server.library-conventions")
}

dependencies {
    implementation(project(":websocket:api"))
    implementation(libs.lipinskipawel.gameEngine)

    implementation(libs.org.slf4j.api)
}
