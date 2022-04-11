plugins {
    application
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":api"))
    implementation(project(":auth"))
    implementation("org.java-websocket:Java-WebSocket:1.5.2")
    implementation("com.google.code.gson:gson:2.8.8")
    implementation("com.github.lipinskipawel:game-engine:5.0.0")
    // currently, org.slf4j:slf4j-api:2.0.0-alpha5 is failing https://issues.apache.org/jira/browse/LOG4J2-3139
    implementation("org.slf4j:slf4j-api:1.8.0-beta4")
    implementation("org.apache.logging.log4j:log4j-api:2.17.1")
    implementation("org.apache.logging.log4j:log4j-core:2.17.1")
    // The log4j2 binding for slf4j https://logging.apache.org/log4j/2.x/log4j-slf4j-impl/index.html
    implementation("org.apache.logging.log4j:log4j-slf4j18-impl:2.17.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    testImplementation("org.assertj:assertj-core:3.20.2")
}

application {
    mainClass.set("com.github.lipinskipawel.Main")
}

tasks.test {
    useJUnitPlatform()
}

sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
    extendsFrom(configurations.testImplementation.get())
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    integrationTestImplementation("org.awaitility:awaitility:4.1.1")
}

configurations["integrationTestRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

val integrationTest = task<Test>("integrationTest") {
    useJUnitPlatform()
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter("test")
}

tasks.check { dependsOn(integrationTest) }
