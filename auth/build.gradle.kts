plugins {
    `java-library`
}

dependencies {
    implementation("io.javalin:javalin:5.6.2")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")
                implementation("org.assertj:assertj-core:3.20.2")
            }
        }
    }
}
