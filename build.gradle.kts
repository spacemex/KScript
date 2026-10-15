plugins {
    id("dev.architectury.loom-no-remap") version "1.17-SNAPSHOT" apply false
    id("architectury-plugin") version "3.5-SNAPSHOT"
    id("com.gradleup.shadow") version "9.3.2" apply false
}

val minecraftVersion = providers.gradleProperty("minecraft_version").get()
val modName = providers.gradleProperty("archives_name").get()
val mavenGroup = providers.gradleProperty("maven_group").get()

val modVersion = providers.gradleProperty("mod_version").get()

architectury {
    minecraft = minecraftVersion
}

allprojects {
    group = mavenGroup
    version = modVersion
}

subprojects {
    apply { plugin("dev.architectury.loom-no-remap") }
    apply { plugin("architectury-plugin") }
    apply { plugin("maven-publish") }

    extensions.configure<BasePluginExtension> {
        archivesName.set("$modName-$modVersion+$minecraftVersion-${project.name}")
    }

    repositories {
        maven(url = uri("https://maven.neoforged.net/releases/"))
    }

    dependencies {
        "minecraft"("net.minecraft:minecraft:$minecraftVersion")
    }

    extensions.configure<JavaPluginExtension> {
        withSourcesJar()

        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(25)
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        archiveVersion.set("")
    }


    tasks.register("generateKScriptEditorWorkspace") {
        group = "kscript"
        description = "Generates a portable Kotlin editor workspace"

        dependsOn(":common:jar")

        doLast {
            val workspace = layout.buildDirectory.dir("kscript/editor-workspace").get().asFile

            val libs = workspace.resolve("libs")
            libs.mkdirs()

            libs.listFiles()?.filter { it.isFile && it.extension == "jar" }?.forEach { it.delete() }

            val commonProject = project(":common")
            val fabricProject = project(":fabric")

            val commonJar = commonProject.tasks.named<Jar>("jar").get().archiveFile.get().asFile

            val classpath = linkedSetOf<File>()
            classpath.add(commonJar)

            for (module in listOf(commonProject, fabricProject)) {
                val configuration = module.configurations.getByName("compileClasspath")

                classpath.addAll(configuration.resolve().filter { it.isFile && it.extension == "jar" })
            }

            classpath.forEachIndexed { index, jar ->
                jar.copyTo(
                    libs.resolve(
                        "${index.toString().padStart(3, '0')}-${jar.name}"
                    ), overwrite = true
                )
            }

            workspace.resolve("settings.gradle.kts").writeText(
                """
            rootProject.name = "kscript-editor-workspace"
            """.trimIndent()
            )

            val kotlinVersion = providers.gradleProperty("kotlin_version").get()

            workspace.resolve("build.gradle.kts").writeText(
                """
            plugins {
                kotlin("jvm") version "$kotlinVersion"
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                compileOnly(
                    fileTree("libs") {
                        include("*.jar")
                    }
                )
            }

            kotlin {
                jvmToolchain(25)
            }
            """.trimIndent()
            )

            val sources = workspace.resolve("src/main/kotlin")
            sources.mkdirs()

            val example = sources.resolve("KScriptExample.kt")

            if (!example.exists()) {
                example.writeText(
                    """
                import com.github.spacemex.kscript.api.ServerEvents

                @Suppress("unused")
                fun registerExampleHandlers() {
                    ServerEvents.PLAYER_JOIN.listen { event ->
                        val name: String = event.playerName
                        println(name)
                        event.sendMessage("Welcome!")
                    }

                    ServerEvents.PLAYER_QUIT.listen { event ->
                        println(event.playerName)
                    }
                }
                """.trimIndent()
                )
            }

            logger.lifecycle("Generated KScript editor workspace: {}", workspace.absolutePath)
            logger.lifecycle("Exported {} classpath JARs", classpath.size)
        }
    }

}
