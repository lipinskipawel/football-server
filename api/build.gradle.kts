plugins {
    `java-library`
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(libs.testing.junit.api)
                implementation(libs.testing.junit.engine)
                implementation(libs.testing.assertj)
            }
        }
    }
}
