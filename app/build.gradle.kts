plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

application {
    // Define the main class for the application.
    mainClass.set("com.github.lipinskipawel.FootballServer")
}

tasks.test {
    // Use junit platform for unit tests.
    useJUnitPlatform()
}
