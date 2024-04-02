plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":api"))
    implementation(libs.lipinskipawel.gameEngine)

    implementation(libs.org.slf4j.api)
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
