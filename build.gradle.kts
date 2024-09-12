plugins {
    kotlin("jvm") version "2.0.10"
    id("com.diffplug.spotless") version "6.25.0"
}

group = "com.ivanbar"

version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation(kotlin("test"))
}

tasks.test { useJUnitPlatform() }

kotlin { jvmToolchain(21) }

spotless {
    kotlin {
        target("**/*.kt")
        ktfmt("0.51").kotlinlangStyle()
        ktlint("1.3.1")
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        ktfmt("0.51").kotlinlangStyle()
    }
}
