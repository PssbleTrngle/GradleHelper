plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
    maven { url = uri("https://maven.minecraftforge.net") }
    maven { url = uri("https://maven.fabricmc.net/") }
    maven { url = uri("https://plugins.gradle.org/m2/") }
}

val plugin_id: String by extra
val artifact_id: String by extra
val plugin_version: String by extra

val cursegradle_version: String by extra
val minotaur_version: String by extra
val kotlin_version: String by extra
val kotlin_dsl_version: String by extra
val fabric_loom_version: String by extra
val forge_gradle_version: String by extra
val vanilla_gradle_version: String by extra
val mixin_version: String by extra

dependencies {
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlin_version}")

    api("net.darkhax.curseforgegradle:CurseForgeGradle:${cursegradle_version}")
    api("com.modrinth.minotaur:Minotaur:${minotaur_version}")

    api("fabric-loom:fabric-loom.gradle.plugin:${fabric_loom_version}")
    api("net.minecraftforge.gradle:ForgeGradle:${forge_gradle_version}")
    api("org.spongepowered:vanillagradle:${vanilla_gradle_version}")

    api("org.spongepowered:mixingradle:${mixin_version}")

    api("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:4.2.1.3168")
}

gradlePlugin {
    isAutomatedPublishing = false
    plugins {
        create("gradle-helper") {
            id = plugin_id
            implementationClass = "${plugin_id}.GradleHelperPlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("plugin") {
            groupId = plugin_id
            artifactId = "${plugin_id}.gradle.plugin"
            version = plugin_version

            from(components["java"])
        }
    }

    repositories {
        System.getenv()["LOCAL_MAVEN"]?.let { localMaven ->
            maven {
                url = uri(localMaven)
            }
        }
    }
}