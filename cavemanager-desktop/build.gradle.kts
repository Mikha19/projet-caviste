plugins {
    kotlin("jvm") version "2.0.21"
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "com.cavemanager"
version = "1.0.0"

dependencies {
    implementation("com.mysql:mysql-connector-j:8.3.0")
    implementation("org.slf4j:slf4j-simple:2.0.12")
    testImplementation(kotlin("test"))
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics")
}

application {
    mainClass = "com.cavemanager.MainKt"
    applicationDefaultJvmArgs = listOf(
        "--add-opens", "javafx.graphics/com.sun.javafx.application=ALL-UNNAMED"
    )
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "21"
    targetCompatibility = "21"
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Jar>("fatJar") {
    archiveClassifier = "all"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "com.cavemanager.MainKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get())
}
tasks.register<Exec>("jpackage") {
    dependsOn("fatJar")
    commandLine(
        "jpackage",
        "--input", "build/libs",
        "--main-jar", "cavemanager-desktop-1.0.0-all.jar",
        "--main-class", "com.cavemanager.MainKt",
        "--name", "CaveManager",
        "--app-version", "1.0.0",
        "--type", "exe",
        "--dest", "build/installer",
        "--win-menu",
        "--win-shortcut"
    )
}