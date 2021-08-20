plugins {
    application
    java
    id("org.springframework.boot") version ("2.5.4")
    id("io.spring.dependency-management") version ("1.0.11.RELEASE")
}

java {
    sourceCompatibility = JavaVersion.VERSION_15
    targetCompatibility = JavaVersion.VERSION_15
}

version = "1.0.0"
group = "com.github.lipinskipawel.footballServer"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(mapOf("group" to "org.junit.vintage", "module" to "junit-vintage-engine"))
    }

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

application {
    mainClass.set("com.github.lipinskipawel.FootballServer")
}

tasks.test {
    useJUnitPlatform()
}
