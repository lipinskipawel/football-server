plugins {
    id("football-server.application-conventions")
}

dependencies {
    implementation(project(":api"))
    implementation(project(":auth"))
    implementation(project(":domain"))
    implementation(libs.lipinskipawel.gameEngine)
    implementation(libs.io.netty)
    implementation(libs.com.google.gson)

    implementation(libs.org.slf4j.api2)
    implementation(libs.com.apache.log4j.api)
    implementation(libs.com.apache.log4j.core)
    // bindings between log4j2 and slf4j
    implementation(libs.com.apache.log4j.slf4j2)
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
                implementation(libs.com.google.gson)
                implementation(libs.org.awaitility)
                implementation(libs.org.javaWebSocket)
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

tasks {
    shadowJar {
        archiveBaseName.set("football-server")
        archiveClassifier.set("")
        archiveVersion.set(project.version.toString())
        isZip64 = true
        mergeServiceFiles()
        manifest {
            attributes("Main-Class" to mainApplicationClassToRun)
        }
    }
}
