plugins {
    id("football-server.application-conventions")
}

version = "1.0.0"

dependencies {
    implementation(libs.io.javalin)
    implementation(libs.org.slf4j.api2)
    implementation(libs.com.apache.log4j.api)
    implementation(libs.com.apache.log4j.core)
    implementation(libs.com.apache.log4j.slf4j2)
}

val mainApplicationClassToRun = "com.github.lipinskipawel.Application"
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
