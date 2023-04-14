plugins {
    application
    `jvm-test-suite`
}

application {
    mainClass.set("com.github.lipinskipawel.Main")
}

dependencies {
    implementation(project(":api"))
    implementation(project(":auth"))
    implementation(project(":domain"))
    implementation("io.netty:netty-all:4.1.73.Final")
    implementation("com.google.code.gson:gson:2.8.8")
    implementation("com.github.lipinskipawel:game-engine:5.0.0")
    // currently, org.slf4j:slf4j-api:2.0.0-alpha5 is failing https://issues.apache.org/jira/browse/LOG4J2-3139
    implementation("org.slf4j:slf4j-api:1.8.0-beta4")
    implementation("org.apache.logging.log4j:log4j-api:2.17.1")
    implementation("org.apache.logging.log4j:log4j-core:2.17.1")
    // The log4j2 binding for slf4j https://logging.apache.org/log4j/2.x/log4j-slf4j-impl/index.html
    implementation("org.apache.logging.log4j:log4j-slf4j18-impl:2.17.1")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class)
        val testIntegration by registering(JvmTestSuite::class) {
            dependencies {
                implementation(project(":api"))
                implementation(project(":app"))
                implementation(project(":auth"))
                implementation(project(":domain"))
                implementation("com.google.code.gson:gson:2.8.8")
                implementation("org.awaitility:awaitility:4.1.1")
                implementation("org.java-websocket:Java-WebSocket:1.5.2")
            }

            targets {
                all {
                    testTask.configure {
                        description = "Runs the integration test suite."
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("testIntegration"))
}
