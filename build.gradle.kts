plugins {
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.31" apply true

}

group = "io.github.abaddon.kcqrs"
version = "1.0-SNAPSHOT"

//Versions
val slf4jVersion = "1.7.25"
val kotlinVersion = "1.5.31"
val kotlinCoroutineVersion = "1.5.1"
val junitJupiterVersion = "5.7.0"

repositories {
    mavenCentral()
}

dependencies {
    //Log
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-log4j12:$slf4jVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutineVersion")


    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:${junitJupiterVersion}") // JVM dependency
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutineVersion")

}

//tasks.test {
//    useJUnitPlatform()
//}
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

