plugins {
    id("football-server.library-conventions")
}

dependencies {
    implementation(platform(libs.lipinskipawel.football.platform))

    implementation(project(":websocket:api"))
    implementation(libs.lipinskipawel.gameEngine)

    implementation(libs.org.slf4j.api)
}

testing {
    suites {
        withType<JvmTestSuite> {
            useJUnitJupiter()
            dependencies {
                implementation(platform(libs.lipinskipawel.football.platform))
                implementation(libs.testing.assertj)
            }
        }
    }
}
