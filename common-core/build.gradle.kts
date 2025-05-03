plugins {
    `java-library`
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.getByName<Jar>("bootJar") {
    enabled = false
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}