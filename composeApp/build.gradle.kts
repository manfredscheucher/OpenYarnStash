import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject
import java.io.ByteArrayOutputStream
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    kotlin("plugin.serialization") version "2.0.0" // Example – adapt to your version
}

// ---- Version-Info-Task vorab registrieren (wird unten verdrahtet) ----
val generateVersionInfo = tasks.register("generateVersionInfo", GenerateVersionInfo::class.java)

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

        // <<< generierte Quelle in commonMain einhängen
        named("commonMain") {
            val versionGenOut = layout.buildDirectory.dir("generated/versionInfo")
            kotlin.srcDir(versionGenOut) // Gradle kümmert sich um Provider
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

/* =========================================================================================
   CC-sichere Task: erzeugt build/generated/versionInfo/GeneratedVersionInfo.kt
   - Nur git (rev-parse, diff-index), kein Env
   - Saubere Inputs/Outputs, keine impliziten Abhängigkeiten
   ========================================================================================= */
abstract class GenerateVersionInfo @Inject constructor(
    private val execOps: ExecOperations
) : DefaultTask() {

    // Nur .git als (optionales) Input -> verhindert "liest Projektroot" & implizite Abhängigkeiten
    @get:InputDirectory
    @get:Optional
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val gitDir: DirectoryProperty

    // version & package als Inputs (cachebar, CC-freundlich)
    @get:Input
    abstract val versionName: Property<String>

    @get:Input
    abstract val packageName: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun run() {
        val version = versionName.get()

        // SHA via git rev-parse (Fallback auf "unknown" wenn .git oder git fehlt)
            val out = ByteArrayOutputStream()
            val wd = gitDir.asFile.orNull?.parentFile ?: project.rootDir
            require(gitDir.asFile.orNull?.exists() == true) { "No .git directory" }
            execOps.exec {
                workingDir = wd
                commandLine("pwd")
                //commandLine("git", "rev-parse", "--short", "HEAD")
                standardOutput = out
            }
        val sha =
            out.toString().trim().ifEmpty { "unknown1" }
         //.getOrDefault("unknown2")

        // Dirty-Flag per Exit-Code (0 clean, !=0 dirty)
        val isDirty = runCatching {
            val wd = gitDir.asFile.orNull?.parentFile ?: project.rootDir
            val result = execOps.exec {
                workingDir = wd
                commandLine("git", "diff-index", "--quiet", "HEAD", "--")
                isIgnoreExitValue = true
            }
            result.exitValue != 0
        }.getOrDefault(null)

        val dir = outputDir.get().asFile.apply { mkdirs() }
        dir.resolve("GeneratedVersionInfo.kt").writeText(
            """
            // Generated – do not edit.
            package ${packageName.get()}

            object GeneratedVersionInfo {
                const val VERSION: String = "$version"
                const val GIT_SHA: String = "$sha"
                const val IS_DIRTY: Boolean = $isDirty
            }
            """.trimIndent()
        )
    }
}

val versionGenOut = layout.buildDirectory.dir("generated/versionInfo")

generateVersionInfo.configure {
    // .git als optionales Input deklarieren
    val gitDirFile = rootProject.layout.projectDirectory.dir(".git")
    if (gitDirFile.asFile.exists()) {
        gitDir.set(gitDirFile)
    }
    versionName.set(project.provider { project.version.toString() }) // kommt aus gradle.properties (version=…)
    packageName.set("org.example.project") // <<< falls dein Package anders ist, hier anpassen
    outputDir.set(versionGenOut)
}

tasks.withType(KotlinCompilationTask::class.java).configureEach {
    dependsOn(generateVersionInfo)
}
