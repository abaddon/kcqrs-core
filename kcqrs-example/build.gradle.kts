plugins {
    kotlin("jvm") version "1.6.0"
//    kotlin("plugin.serialization") version "1.6.0" apply true
    jacoco
    `maven-publish`
}

group = "io.github.abaddon.kcqrs"
version = "0.0.1-SNAPSHOT"

//Versions
val kcqrsCoreVersion = "0.0.1-SNAPSHOT"
val kcqrsTestVersion = "0.0.1-SNAPSHOT"
val kustomCompareVersion = "0.0.1"
val slf4jVersion = "1.7.25"
val kotlinVersion = "1.6.0"
val kotlinCoroutineVersion = "1.6.0"
//val kotlinxSerializationVersion = "1.3.0"
val jacksonModuleKotlinVersion = "2.13.0"
val junitJupiterVersion = "5.7.0"

val eventStoreDBVersion = "1.0.0"

publishing {
    publications {
        create<MavenPublication>("kcqrs-example") {
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
    implementation("io.github.abaddon:kustomCompare:$kustomCompareVersion")

    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutineVersion")
    implementation("org.junit.jupiter:junit-jupiter:${junitJupiterVersion}") // JVM dependency
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutineVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonModuleKotlinVersion")
    implementation(kotlin("test"))

    testImplementation(kotlin("test"))
    testImplementation("io.github.abaddon.kcqrs:kcqrs-test:$kcqrsTestVersion")
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


