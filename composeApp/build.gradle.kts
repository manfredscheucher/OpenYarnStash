import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject
import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    kotlin("plugin.serialization") version "2.0.0" // Example – adapt to your version
}

kotlin {
    androidTarget()
    /*
        androidTarget {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }
        */
    /*
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    */

    
    jvm()
    
    js {
        browser()
        binaries.executable()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.activity.compose)
            implementation("androidx.exifinterface:exifinterface:1.3.7")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(compose.materialIconsExtended)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koalaplot.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("org.apache.pdfbox:pdfbox:2.0.31")
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlin.browser)
        }
    }
}

android {
    namespace = "org.example.project"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.example.project"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = project.version.toString()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "org.example.project.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.example.project"
            packageVersion = project.version.toString()
        }
    }
}

abstract class GenerateVersionInfo @Inject constructor(
    private val execOps: ExecOperations
) : DefaultTask() {

    @get:OutputDirectory
    val outputDir = project.layout.buildDirectory.dir("generated/versionInfo")

    @TaskAction
    fun run() {
        val version = project.version.toString()

        // 1) bevorzugt aus CI
        val envSha = System.getenv("GIT_SHA")?.takeIf { it.isNotBlank() }

        // 2) sonst lokal aus git, aber ohne Build-Abbruch bei Fehler
        val sha = envSha ?: runCatching {
            val out = ByteArrayOutputStream()
            execOps.exec {
                commandLine("git", "rev-parse", "--short", "HEAD")
                standardOutput = out
            }
            out.toString().trim().ifEmpty { "unknown" }
        }.getOrDefault("unknown")

        val isDirty = runCatching {
            execOps.exec {
                commandLine("git", "diff", "--quiet")
            }
            false
        }.getOrDefault(true)

        val pkg = "org.example.project"

        val dir = outputDir.get().asFile
        dir.mkdirs()
        val file = dir.resolve("GeneratedVersionInfo.kt")
        file.writeText(
            """
            // Generated – do not edit.
            package $pkg

            object GeneratedVersionInfo {
                const val VERSION = "$version"
                const val GIT_SHA = "$sha"
                const val IS_DIRTY = $isDirty
            }
            """.trimIndent()
        )
    }
}

val generateVersionInfo = tasks.register("generateVersionInfo", GenerateVersionInfo::class.java)

kotlin {
    sourceSets.named("commonMain") {
        kotlin.srcDir(generateVersionInfo)
        kotlin.srcDir(generateVersionInfo.map { it.outputDir })
    }
}