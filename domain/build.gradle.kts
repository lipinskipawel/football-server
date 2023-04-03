java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

dependencies {
    implementation(project(":api"))
    implementation("com.github.lipinskipawel:game-engine:5.0.0")

    implementation("org.slf4j:slf4j-api:1.8.0-beta4")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    testImplementation("org.assertj:assertj-core:3.20.2")
}
