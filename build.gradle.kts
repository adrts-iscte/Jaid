
tasks.find { it.name == "processResources"}!!.enabled = false

plugins {
    kotlin("jvm") version "2.3.20"
}

group = "pt.iscte"
version = "1.1"

repositories {
    mavenCentral()
}

tasks.jar {
    isZip64=true

    manifest {
        attributes["Main-Class"] = "binary.JaidMainKt"
    }
    exclude("META-INF/*.RSA", "META-INF/*.SF","META-INF/*.DSA")

    // To avoid the duplicate handling strategy error
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // To add all of the dependencies
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

tasks.withType<org.gradle.jvm.tasks.Jar>() {
    exclude("META-INF/BC1024KE.RSA", "META-INF/BC1024KE.SF", "META-INF/BC1024KE.DSA")
    exclude("META-INF/BC2048KE.RSA", "META-INF/BC2048KE.SF", "META-INF/BC2048KE.DSA")
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.25.3")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.5.0.202303070854-r")
    implementation("com.github.gumtreediff:core:3.0.0")
    implementation("com.github.gumtreediff:client:3.0.0")
    implementation("com.github.gumtreediff:gen.javaparser:3.0.0")
    implementation(kotlin("stdlib-jdk8"))
    //api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
}

tasks.test {
    useJUnitPlatform()
}

