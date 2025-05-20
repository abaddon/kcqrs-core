import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
    const val log4j = "2.24.3"
    const val kotlinVersion = "2.1.21"
    const val kotlinCoroutineVersion = "1.10.2"
    const val junitJupiterVersion = "5.10.2"
    const val jacocoToolVersion = "0.8.11"
    const val jvmTarget = "21"
}

plugins {
    kotlin("jvm") version "2.1.21"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("com.palantir.git-version") version "3.0.0"
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
    implementation("org.apache.logging.log4j:log4j-api:${Versions.log4j}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutineVersion}")

    //Test
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junitJupiterVersion}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.kotlinCoroutineVersion}")
    testImplementation("org.apache.logging.log4j:log4j-core:${Versions.log4j}")
    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:${Versions.log4j}")
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
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(Versions.jvmTarget))
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
