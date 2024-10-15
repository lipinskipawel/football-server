plugins {
    id("football-server.library-conventions")
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
