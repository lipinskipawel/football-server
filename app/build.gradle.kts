plugins {
    application
    `jvm-test-suite`
    id("com.github.johnrengelman.shadow") version ("7.1.2")
}

dependencies {
    implementation(project(":api"))
    implementation(project(":auth"))
    implementation(project(":domain"))
    implementation("io.netty:netty-all:4.1.73.Final")
    implementation("com.google.code.gson:gson:2.8.8")
    implementation("com.github.lipinskipawel:game-engine:5.0.0")

    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.apache.logging.log4j:log4j-api:2.21.0")
    implementation("org.apache.logging.log4j:log4j-core:2.21.0")
    // bindings between log4j2 and slf4j
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.21.0")
}

testing {
    suites {
        withType<JvmTestSuite> {
            useJUnitJupiter()
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")
                implementation("org.assertj:assertj-core:3.20.2")
            }
        }
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

val mainApplicationClassToRun = "com.github.lipinskipawel.Main"
tasks.withType<Jar> {
    manifest {
        attributes("Main-Class" to mainApplicationClassToRun)
    }
}

application {
    mainClass.set(mainApplicationClassToRun)
}
