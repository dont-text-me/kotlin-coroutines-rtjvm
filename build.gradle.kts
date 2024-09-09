plugins {
    kotlin("jvm") version "2.0.10"
    id("com.diffplug.spotless") version "6.25.0"
}

group = "com.ivanbar"

version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

dependencies { testImplementation(kotlin("test")) }

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
