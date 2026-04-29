import org.jetbrains.compose.desktop.application.dsl.TargetFormat
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    kotlin("plugin.serialization") version "2.0.0"
}


val generateVersionInfo = tasks.register("generateVersionInfo", GenerateVersionInfo::class.java)

kotlin {
    androidTarget()

    jvm()

    js {
        browser()
        binaries.executable()
    }

    // iOS Targets
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
        iosTarget.compilations.getByName("main") {
            cinterops {
                val minizip by creating {
                    defFile(project.file("src/nativeInterop/cinterop/minizip.def"))
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
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
                implementation(libs.kotlinx.io.core)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotlinx.coroutines.test)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.uiTest)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.exifinterface)
            }
        }

        val iosMain by creating {
            dependsOn(commonMain)
        }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }

        val iosTest by creating {
            dependsOn(commonTest)
        }
        val iosArm64Test by getting { dependsOn(iosTest) }
        val iosSimulatorArm64Test by getting { dependsOn(iosTest) }

        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutinesSwing)
                implementation(libs.pdfbox)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutinesSwing)
                implementation(kotlin("test-junit"))
                implementation(compose.desktop.uiTestJUnit4)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(libs.kotlin.browser)
            }
        }

        named("commonMain") {
            val versionGenOut = layout.buildDirectory.dir("generated/versionInfo")
            kotlin.srcDir(versionGenOut)
        }
    }
}

android {
    namespace = "org.example.OpenYarnStash"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.example.OpenYarnStash"
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
        mainClass = "org.example.OpenYarnStash"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "OpenYarnStash"
            packageVersion = project.version.toString()
        }
    }
}

abstract class GenerateVersionInfo @Inject constructor(
    private val execOps: ExecOperations
) : DefaultTask() {
    @get:InputDirectory @get:Optional @get:PathSensitive(PathSensitivity.NONE)
    abstract val gitDir: DirectoryProperty
    @get:Input abstract val versionName: Property<String>
    @get:Input abstract val packageName: Property<String>
    @get:Input abstract val expirationDays: Property<Int>
    @get:OutputDirectory abstract val outputDir: DirectoryProperty

    @TaskAction
    fun run() {
        val version = versionName.get()
        var sha = "unknown"
        var isDirty = "unknown"
        val wd = gitDir.asFile.orNull?.parentFile ?: project.rootDir

        var commitDate = "unknown"
        if (gitDir.asFile.orNull?.exists() == true) {
            try {
                // Get git commit hash
                val shaOutput = ByteArrayOutputStream()
                execOps.exec {
                    workingDir = wd
                    commandLine("git", "rev-parse", "--short", "HEAD")
                    standardOutput = shaOutput
                    isIgnoreExitValue = false
                }
                sha = shaOutput.toString().trim()

                // Get commit date (ISO 8601 UTC)
                val commitDateOutput = ByteArrayOutputStream()
                execOps.exec {
                    workingDir = wd
                    commandLine("git", "log", "-1", "--format=%cI", "--date=iso-strict")
                    standardOutput = commitDateOutput
                    isIgnoreExitValue = false
                }
                commitDate = commitDateOutput.toString().trim()

                // Check if repo is dirty (only tracked files)
                val statusOutput = ByteArrayOutputStream()
                execOps.exec {
                    workingDir = wd
                    commandLine("git", "status", "--porcelain", "--untracked-files=no")
                    standardOutput = statusOutput
                    isIgnoreExitValue = false
                }
                isDirty = if (statusOutput.toString().trim().isEmpty()) "clean" else "dirty"
            } catch (e: Exception) {
                // Git commands failed, keep defaults
            }
        }

        // Compile timestamp ISO 8601 UTC
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        df.timeZone = TimeZone.getTimeZone("UTC")
        val compileDate = df.format(Date())

        val expDays = expirationDays.get()
        val commitYear = commitDate.take(4).toIntOrNull() ?: 0
        val dir = outputDir.get().asFile.apply { mkdirs() }
        dir.resolve("GeneratedVersionInfo.kt").writeText(
            """
            package ${packageName.get()}

            /**
             * AUTO-GENERATED FILE - DO NOT EDIT!
             *
             * This file is automatically generated during build.
             *
             * To change the version number, edit:
             *   gradle.properties -> version=X.Y.Z
             *
             * Other values are generated from Git:
             *   COMMIT_SHA: Current Git commit hash
             *   COMMIT_DATE: Date of the last Git commit (ISO 8601)
             *   COMMIT_YEAR: Year of the last Git commit
             *   IS_DIRTY: Whether working directory has uncommitted changes
             *   BUILD_DATE: When this build was compiled (ISO 8601 UTC)
             *   EXPIRATION_DAYS: Days until app expires (0 = no expiration)
             */
            object GeneratedVersionInfo {
                const val VERSION: String = "$version"
                const val COMMIT_SHA: String = "$sha"
                const val COMMIT_DATE: String = "$commitDate"
                const val COMMIT_YEAR: Int = $commitYear
                const val IS_DIRTY: String = "$isDirty"
                const val BUILD_DATE: String = "$compileDate"
                const val EXPIRATION_DAYS: Int = $expDays
            }
            """.trimIndent()
        )
    }
}

val versionGenOut = layout.buildDirectory.dir("generated/versionInfo")

generateVersionInfo.configure {
    val gitDirFile = rootProject.layout.projectDirectory.dir(".git")
    if (gitDirFile.asFile.exists()) {
        gitDir.set(gitDirFile)
    }
    versionName.set(project.provider { project.version.toString() })
    packageName.set("org.example.project")
    outputDir.set(versionGenOut)
    expirationDays.set(project.provider { (project.findProperty("expirationDays") as? String)?.toIntOrNull() ?: 365 })
}

tasks.withType(KotlinCompilationTask::class.java).configureEach {
    dependsOn(generateVersionInfo)
}
