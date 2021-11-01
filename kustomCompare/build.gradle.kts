plugins {
    kotlin("jvm") version "1.6.0"
    jacoco
    `maven-publish`
}

group = "io.github.abaddon"
version = "0.0.1"

//Versions
val slf4jVersion = "1.7.25"
val kotlinVersion = "1.5.31"
val junitJupiterVersion = "5.7.0"


publishing {
    publications {
        create<MavenPublication>("kustomCompare") {
            from(components["kotlin"])
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-log4j12:$slf4jVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:${junitJupiterVersion}") // JVM dependency
}

jacoco {
    toolVersion = "0.8.7"
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
