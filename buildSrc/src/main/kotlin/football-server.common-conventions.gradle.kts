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

dependencies {
    implementation(platform("com.github.lipinskipawel:football-platform:1.1.0"))
}

testing {
    suites {
        withType<JvmTestSuite> {
            useJUnitJupiter()
            dependencies {
                // gradle supports only string-y API only for now https://github.com/gradle/gradle/issues/15383
                // workaround described in the comment https://github.com/gradle/gradle/issues/15383#issuecomment-2110443971
                implementation("org.assertj:assertj-core:3.20.2")
            }
        }
    }
}
