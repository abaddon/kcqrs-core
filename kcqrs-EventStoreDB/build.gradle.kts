plugins {
    kotlin("jvm") version "1.6.0"
    kotlin("plugin.serialization") version "1.5.31" apply true
    jacoco
    `maven-publish`
}

group = "io.github.abaddon.kcqrs"
version = "0.0.1-SNAPSHOT"

//Versions
val kcqrsCoreVersion = "0.0.1-SNAPSHOT"
val slf4jVersion = "1.7.25"
val kotlinVersion = "1.5.31"
val kotlinCoroutineVersion = "1.5.1"
val kotlinxSerializationVersion = "1.3.0"
val jacksonModuleKotlinVersion = "2.13.0"
val junitJupiterVersion = "5.7.0"

val eventStoreDBVersion = "1.0.0"

publishing {
    publications {
        create<MavenPublication>("kcqrs-eventStoreDB") {
            from(components["kotlin"])
        }
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    //KCQRS Modules
    implementation("io.github.abaddon.kcqrs:kcqrs-core:$kcqrsCoreVersion")

    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-log4j12:$slf4jVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutineVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonModuleKotlinVersion")

    //EventStoreDB
    implementation("com.eventstore:db-client-java:$eventStoreDBVersion")


    testImplementation(project(":kcqrs-core","default"))
    testImplementation(project(":kcqrs-test","default"))
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:${junitJupiterVersion}") // JVM dependency
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutineVersion")
    testImplementation("org.testcontainers:testcontainers:1.16.2")
    testImplementation("org.testcontainers:junit-jupiter:1.16.2")
    //testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    //testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

}

jacoco {
    toolVersion = "0.8.7"
    //reportsDirectory.set(layout.buildDirectory.dir("customJacocoReportDir"))
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco"))
    }
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}