repositories {
    jcenter()
    google()
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    java
    `kotlin-dsl`
    `java-gradle-plugin`
}

buildscript {
    repositories {
        jcenter()
        google()
    }
}

dependencies {
    implementation("com.android.tools.build:gradle:4.0.1")
    implementation("com.adarshr:gradle-test-logger-plugin:2.1.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.20")
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("android-extensions"))
}

gradlePlugin {
    plugins {
        register("kotlin-module") {
            id = "kotlin-module"
            implementationClass = "plugins.SetupKotlinModulePlugin"
        }

        register("android-module") {
            id = "android-module"
            implementationClass = "plugins.SetupAndroidModulePlugin"
        }
    }
}
