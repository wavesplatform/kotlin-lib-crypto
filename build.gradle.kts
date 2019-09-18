plugins {
    `build-scan`
    kotlin("jvm") version "1.3.50"
    id("org.jetbrains.dokka") version "0.9.18"
    `maven-publish`
}

group = "com.wavesplatform.sdk.crypto"
version = "0.0.7"

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("commons-codec:commons-codec:1.10")
    implementation("com.madgag.spongycastle:core:1.58.0.0")
    implementation("org.whispersystems:curve25519-java:0.4.1")
    implementation("com.google.guava:guava:28.1-android")

    testImplementation("junit:junit:4.12")
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
    publishAlways()
}

// Add docs with Dokka
tasks.dokka {
    outputFormat = "html"
    outputDirectory = "$buildDir/javadoc"
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    classifier = "javadoc"
    from(tasks.dokka)
}

// Publish to local repo
publishing {
    publications {
        create<MavenPublication>("default") {
            from(components["java"])
            artifact(dokkaJar)
        }
    }
    repositories {
        maven {
            url = uri("$buildDir/repository")
        }
    }
}
