plugins {
    id("org.springframework.boot") version "3.2.5" apply false
    id("io.spring.dependency-management") version "1.1.0" apply false
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"
}

allprojects {
    group = "com.chargepoint"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "kotlin")
    apply(plugin = "kotlin-spring")

    group = "com.chargepoint"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-webflux")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.springframework.kafka:spring-kafka")
        implementation("io.projectreactor.kafka:reactor-kafka")

        testImplementation(kotlin("test"))
    }
}