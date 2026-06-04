plugins {
    id("java")
    id("dev.architectury.loom") version "1.11-SNAPSHOT"
    id("architectury-plugin") version "3.4-SNAPSHOT"
}

group = "com.thecompanyinc"
version = "0.1.0-alpha"

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    silentMojangMappingsLicense()
}

repositories {
    mavenCentral()
    mavenLocal() // mapfrontiers-api SNAPSHOT — see setup note below
    maven("https://artefacts.cobblemon.com/releases/")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://cursemaven.com")
}

dependencies {
    minecraft("net.minecraft:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.17.2")
    modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:0.116.6+1.21.1")
    modImplementation(fabricApi.module("fabric-command-api-v2", "0.116.6+1.21.1"))
    modImplementation(fabricApi.module("fabric-lifecycle-events-v1", "0.116.6+1.21.1"))
    modImplementation(fabricApi.module("fabric-resource-loader-v0", "0.116.6+1.21.1"))
    modImplementation(fabricApi.module("fabric-events-interaction-v0", "0.116.6+1.21.1"))
    modImplementation("com.cobblemon:mod:1.7.3+1.21.1") { isTransitive = false }
    modImplementation("com.cobblemon:fabric:1.7.3+1.21.1")
    implementation("com.google.code.gson:gson:2.10.1")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
    modImplementation("curse.maven:radical-cobblemon-trainers-api-1152792:7952421")
    modApi("me.shedaniel.cloth:cloth-config-fabric:15.0.140") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    modApi("com.terraformersmc:modmenu:11.0.3")
    // Map Frontiers API — optional at runtime; compile-only so it is not bundled.
    // The API is not yet on public Maven. One-time setup:
    //   git clone https://github.com/alejandrocoria/MapFrontiers-API.git /tmp/mapfrontiers-api
    //   cd /tmp/mapfrontiers-api && ./gradlew publishToMavenLocal
    // Then 'mavenLocal()' above resolves it as games.alejandrocoria:mapfrontiers-api:0.1.0-SNAPSHOT
    modCompileOnly("games.alejandrocoria:mapfrontiers-api:0.1.0-SNAPSHOT")
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(project.properties)
        }
    }

    jar {
        from("LICENSE")
    }

    java {
        withSourcesJar()
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    compileJava {
        options.release = 21
    }
}
