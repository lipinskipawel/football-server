plugins {
    `java-library`
}

dependencies {
    implementation(project(":api"))
    implementation("com.github.lipinskipawel:game-engine:5.0.0")

    implementation("org.slf4j:slf4j-api:1.8.0-beta4")
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
