plugins {
    id("groovy")
    id("football-server.application-conventions")
    id("com.revolut.jooq-docker") version "0.3.12"
}

version = "1.0.0"

dependencies {
    implementation(platform(libs.lipinskipawel.football.platform))

    implementation(libs.io.javalin)
    implementation(libs.org.slf4j.api2)
    implementation(libs.com.apache.log4j.api)
    implementation(libs.com.apache.log4j.core)
    implementation(libs.com.apache.log4j.slf4j2)
    implementation(libs.org.postgresql.driver)
    implementation(libs.com.zaxxer.hikariCP)

    implementation(libs.org.jooq)
    implementation(libs.org.flyway.core)

    runtimeOnly(libs.org.flyway.postgresql)
    jdbc(libs.org.postgresql.driver)

    testImplementation(libs.testing.mockito)
}

val mainApplicationClassToRun = "com.github.lipinskipawel.Application"
tasks.withType<Jar> {
    manifest {
        attributes("Main-Class" to mainApplicationClassToRun)
    }
}

testing {
    suites {
        withType<JvmTestSuite> {
            useJUnitJupiter()
            dependencies {
                implementation(platform(libs.lipinskipawel.football.platform))
                implementation(libs.testing.assertj)
            }
        }
        val test by getting(JvmTestSuite::class)
        val testIntegration by registering(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation(project(":auth:client"))

                implementation(libs.org.postgresql.driver)
                implementation(libs.testing.tcontainers)
                implementation(libs.testing.tcontainers.postgresql)

                implementation(libs.com.apache.groovy)
                implementation(libs.org.spock)
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

application {
    mainClass.set(mainApplicationClassToRun)
}

tasks {
    shadowJar {
        archiveBaseName.set("football-auth-app")
        archiveClassifier.set("")
        archiveVersion.set(project.version.toString())
        isZip64 = true
        mergeServiceFiles()
        manifest {
            attributes("Main-Class" to mainApplicationClassToRun)
        }
    }

    generateJooqClasses {
        schemas = arrayOf("public")
        basePackageName = "com.github.lipinskipawel.jooq"
        outputDirectory.set(project.layout.buildDirectory.dir("generated-sources"))
        excludeFlywayTable = true
        customizeGenerator {
            database.withForcedTypes(
                // for in-code converters see
                // https://stackoverflow.com/questions/71576487/in-jooq-how-do-we-use-java-instant-to-map-to-postgresqls-timestamp-type
                org.jooq.meta.jaxb.ForcedType()
                    .withName("Instant")
                    .withIncludeExpression("CREATED_DATE|UPDATED_DATE")
                    .withIncludeTypes("timestamp")
            )
        }
    }
}
