plugins {
    id("com.android.application") version "8.10.1" apply false
    id("org.jetbrains.kotlin.android") version "2.2.0-RC" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.0-RC"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}