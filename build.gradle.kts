group = "io.github.abaddon.kcqrs"

object Meta {
    const val desc = "KCQRS Core library"
    const val license = "Apache-2.0"
    const val githubRepo = "abaddon/kcqrs-core"
    const val developerName = "Stefano Longhi"
    const val developerOrganization = ""
    const val organizationUrl = "https://github.com/abaddon"
}

object Versions {
    const val slf4jVersion = "2.0.12" // Updated from 1.7.25
    const val kotlinVersion = "1.9.22" // Updated from 1.6.0
    const val kotlinCoroutineVersion = "1.8.0" // Updated from 1.6.0
    const val junitJupiterVersion = "5.10.2" // Updated from 5.7.0
    const val jacocoToolVersion = "0.8.11" // Updated from 0.8.7
    const val jvmTarget = "21" // Updated from 11
}

plugins {
    kotlin("jvm") version "1.9.22" // Updated from 1.6.0
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0" // Updated from 1.1.0
    id("com.palantir.git-version") version "3.0.0" // Updated from 0.13.0
    jacoco
    `maven-publish`
    signing
}

val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
val details = versionDetails()

val lastTag = details.lastTag.substring(1)
val snapshotTag = {
    val list = lastTag.split(".")
    val third = (list.last().toInt() + 1).toString()
    "${list[0]}.${list[1]}.$third-SNAPSHOT"
}
version = if(details.isCleanTag) lastTag else snapshotTag()

repositories {
    mavenCentral()
}

dependencies {
    //Core
    implementation("org.slf4j:slf4j-api:${Versions.slf4jVersion}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutineVersion}")

    //Test
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junitJupiterVersion}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.kotlinCoroutineVersion}")
}

jacoco {
    toolVersion = Versions.jacocoToolVersion
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
    kotlinOptions.jvmTarget = Versions.jvmTarget
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21)) // Added to explicitly set Java toolchain
    }
    withSourcesJar()
    withJavadocJar()
}

signing {
    val signingKey = providers
        .environmentVariable("GPG_SIGNING_KEY")
    val signingPassphrase = providers
        .environmentVariable("GPG_SIGNING_PASSPHRASE")
    if (signingKey.isPresent && signingPassphrase.isPresent) {
        useInMemoryPgpKeys(signingKey.get(), signingPassphrase.get())
        val extension = extensions
            .getByName("publishing") as PublishingExtension
        sign(extension.publications)
    }
}

publishing {
    publications {
        create<MavenPublication>("kcqrs-core") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            // from(components["kotlin"]) - This line was commented out in the original
            artifact(tasks["jar"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            pom {
                name.set(project.name)
                description.set(Meta.desc)
                url.set("https://github.com/${Meta.githubRepo}")
                licenses {
                    license {
                        name.set(Meta.license)
                        url.set("https://opensource.org/licenses/Apache-2.0")
                    }
                }
                developers {
                    developer {
                        name.set(Meta.developerName)
                        organization.set(Meta.developerOrganization)
                        organizationUrl.set(Meta.organizationUrl)
                    }
                }
                scm {
                    url.set(
                        "https://github.com/${Meta.githubRepo}.git"
                    )
                    connection.set(
                        "scm:git:git://github.com/${Meta.githubRepo}.git"
                    )
                    developerConnection.set(
                        "scm:git:git://github.com/${Meta.githubRepo}.git"
                    )
                }
                issueManagement {
                    url.set("https://github.com/${Meta.githubRepo}/issues")
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        // see https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/#configuration
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username = providers.environmentVariable("SONATYPE_USERNAME")
            password = providers.environmentVariable("SONATYPE_TOKEN")
        }
    }
}
