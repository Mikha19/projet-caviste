plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    id("org.openjfx.javafxplugin") version "0.1.0"
    application
}

group = "com.cavemanager"
version = "1.0.0"

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("com.cavemanager.MainKt") // adjust if needed
}

dependencies {
    // Ktor Server
    implementation("io.ktor:ktor-server-core:3.0.3")
    implementation("io.ktor:ktor-server-netty:3.0.3")
    implementation("io.ktor:ktor-server-cors:3.0.3")
    implementation("io.ktor:ktor-server-content-negotiation:3.0.3")
    implementation("io.ktor:ktor-server-auth:3.0.3")
    implementation("io.ktor:ktor-server-auth-jwt:3.0.3")
    implementation("io.ktor:ktor-server-status-pages:3.0.3")
    implementation("io.ktor:ktor-server-call-logging:3.0.3")

    // Serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Database
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Security
    implementation("at.favre.lib:bcrypt:0.10.1")
    implementation("com.auth0:java-jwt:4.4.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.11")

    // Testing
    testImplementation("io.ktor:ktor-server-tests:3.0.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.0.21")
}