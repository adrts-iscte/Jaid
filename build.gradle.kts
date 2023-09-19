import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

tasks.find { it.name == "processResources"}!!.enabled = false

plugins {
    kotlin("jvm") version "1.9.0"
}

group = "org.example"
version = "v1.0"

repositories {
    mavenCentral()
}


tasks.jar {
    isZip64=true

    manifest {
        attributes["Main-Class"] = "binary.JaidMainKt"
//        attributes["Main-Class"] = "binary.IdentifyMergeScenarioMainKt"
    }
//
//    from {
//        configurations.compileClasspath.collect {
//            it.is
//        }
//    }
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
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

