plugins {
    java
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

testing {
    suites {
        withType<JvmTestSuite> {
            useJUnitJupiter()
            dependencies {
                implementation(findLibrary("testing-junit-api"))
                implementation(findLibrary("testing-junit-engine"))
                implementation(findLibrary("testing-assertj"))
            }
        }
    }
}
