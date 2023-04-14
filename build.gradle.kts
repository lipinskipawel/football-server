plugins {
    `java-library`
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
            vendor.set(JvmVendorSpec.ADOPTIUM)
        }
    }

    repositories {
        mavenCentral()
    }

    testing {
        suites {
            configureEach {
                if (this is JvmTestSuite) {
                    useJUnitJupiter()
                    dependencies {
                        implementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
                        implementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")
                        implementation("org.assertj:assertj-core:3.20.2")
                    }
                }
            }
        }
    }
}

tasks.wrapper {
    gradleVersion = "7.4.2"
    distributionType = Wrapper.DistributionType.ALL
}
