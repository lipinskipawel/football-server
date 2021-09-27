plugins {
    application
    java
}

java {
    sourceCompatibility = JavaVersion.VERSION_15
    targetCompatibility = JavaVersion.VERSION_15
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.java-websocket:Java-WebSocket:1.5.2")
    implementation("org.slf4j:slf4j-simple:2.0.0-alpha5")
    implementation("com.google.code.gson:gson:2.8.8")

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