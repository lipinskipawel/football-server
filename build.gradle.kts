subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }
}

tasks.wrapper {
    gradleVersion = "7.4.2"
    distributionType = Wrapper.DistributionType.ALL
}
