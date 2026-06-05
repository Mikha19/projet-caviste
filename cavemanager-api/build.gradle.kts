plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    id("io.ktor.plugin") version "3.0.3"
    application
}

group = "com.cavemanager"
version = "1.0.0"

dependencies {
    // Ktor Server
    implementation("io.ktor:ktor-server-core:3.0.3")
    implementation("io.ktor:ktor-server-netty:3.0.3")
    implementation("io.ktor:ktor-server-cors:3.0.3")
    implementation("io.ktor:ktor-server-content-negotiation:3.0.3")
    implementation("io.ktor:ktor-server-auth:3.0.3")
    implementation("io.ktor:ktor-server-auth-jwt:3.0.3")
    implementation("io.ktor:ktor-server-status-pages:3.0.3")

    // Serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Database (unchanged)
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Security (unchanged)
    implementation("at.favre.lib:bcrypt:0.10.1")
    implementation("com.auth0:java-jwt:4.4.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("io.ktor:ktor-server-call-logging:3.0.3")

    // Testing
    testImplementation("io.ktor:ktor-server-tests:3.0.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.0.21")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.cavemanager.api.ApplicationKt")
}