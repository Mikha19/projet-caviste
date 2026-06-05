plugins {
    kotlin("jvm") version "2.0.21"
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
    mainClass.set("com.cavemanager.MainKt") // adjust if your main file differs
}

dependencies {
    // Database
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.11")

    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.0.21")
}