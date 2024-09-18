plugins {
    id("football-server.application-conventions")
    id("org.flywaydb.flyway") version ("9.7.0")
    id("nu.studer.jooq") version "9.0"
}

val flywayMigration by configurations.creating {
}

version = "1.0.0"

dependencies {
    implementation(libs.io.javalin)
    implementation(libs.org.slf4j.api2)
    implementation(libs.com.apache.log4j.api)
    implementation(libs.com.apache.log4j.core)
    implementation(libs.com.apache.log4j.slf4j2)

    implementation(libs.org.flyway.core)
    runtimeOnly(libs.org.flyway.postgresql)
    implementation(libs.org.postgresql.driver)
    flywayMigration(libs.org.postgresql.driver)
    jooqGenerator(libs.org.postgresql.driver)

    implementation(libs.com.zaxxer.hikariCP)

    testImplementation("org.mockito:mockito-core:5.12.0")
}

val mainApplicationClassToRun = "com.github.lipinskipawel.Application"
tasks.withType<Jar> {
    manifest {
        attributes("Main-Class" to mainApplicationClassToRun)
    }
}

testing {
    suites {
        val test by getting(JvmTestSuite::class)
        val testIntegration by registering(JvmTestSuite::class) {
            dependencies {
                implementation(project)
                implementation(project(":auth:client"))
                implementation(libs.org.postgresql.driver)
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
}

//docker run -d -p 6543:5432 --name default-postgres -e POSTGRES_PASSWORD=password postgres:16.4
val pgUrl = "jdbc:postgresql://localhost:6543/"
val pgUser = "postgres"
val pgPassword = "password"

flyway {
    configurations = arrayOf("flywayMigration")
    url = pgUrl
    user = pgUser
    password = pgPassword
}

jooq {
    version.set("3.19.1") // default
    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS) // default

    configurations {
        create("main") {
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = pgUrl
                    user = pgUser
                    password = pgPassword
                    properties.add(org.jooq.meta.jaxb.Property().apply {
                        key = "ssl"
                        value = "false"
                    })
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        forcedTypes.addAll(listOf(
                            // for in-code converters see
                            // https://stackoverflow.com/questions/71576487/in-jooq-how-do-we-use-java-instant-to-map-to-postgresqls-timestamp-type
                            org.jooq.meta.jaxb.ForcedType().apply {
                                name = "Instant"
                                includeExpression = "CREATED|TERMINATED"
                                includeTypes = "timestamp"
                            }
                        ))
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "com.github.lipinskipawel.jooq"
                        directory = "build/generated-sources/jooq/main" // default
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}

tasks.named<nu.studer.gradle.jooq.JooqGenerate>("generateJooq") {
    dependsOn("flywayMigrate")

    inputs.files(fileTree("src/main/resources/db/migration"))
        .withPropertyName("migrations")
        .withPathSensitivity(PathSensitivity.RELATIVE)

    allInputsDeclared.set(true)
}
