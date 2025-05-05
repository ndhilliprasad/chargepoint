dependencies {
    api(project(":common-core"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()

    doFirst {
        layout.buildDirectory.dir("tmp").get().asFile.mkdirs()
    }

    systemProperty("java.io.tmpdir", layout.buildDirectory.dir("tmp").get().asFile.absolutePath)
}

kotlin {
    jvmToolchain(21)
}