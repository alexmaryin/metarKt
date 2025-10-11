import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform") version "2.2.0"
    id("com.vanniktech.maven.publish") version "0.34.0"
    id("com.android.library") version "8.12.3"
}

group = "io.github.alexmaryin.metarkt"
version = "1.0.5"

kotlin {
    explicitApi()

    jvm()
    androidTarget {
        publishLibraryVariants("release")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib"))
                // Date-time
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = "io.github.alexmaryin.metarkt"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(group.toString(), "metarkt", version.toString())
    pom {
        name = "MetarKt library"
        description = "Provides parsing of raw METAR string to pure Kotlin data classes"
        inceptionYear = "2021"
        url = "https://github.com/alexmaryin/metarKt"
        licenses {
            license {
                name = "MIT"
                url = "https://opensource.org/licenses/MIT"
            }
        }
        developers {
            developer {
                id = "alexmaryin"
                name = "Alex Maryin"
                email = "java.ul@gmail.com"
            }
        }
        scm {
            url = "https://github.com/alexmaryin/metarKt"
        }
        properties.put("kotlin.minimum.version", "2.2.0")
    }
}
