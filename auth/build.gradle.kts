java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

dependencies {
    implementation("io.netty:netty-all:4.1.73.Final")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    testImplementation("org.assertj:assertj-core:3.20.2")
}
