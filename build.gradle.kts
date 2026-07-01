plugins {
    id("java")
    id("dev.architectury.loom") version "1.14.476"
    id("architectury-plugin") version "3.4-SNAPSHOT"
}

group = "com.thecompanyinc"
version = "0.2.0-alpha.1"

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    silentMojangMappingsLicense()
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://artefacts.cobblemon.com/releases/")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://cursemaven.com")
    maven("https://api.modrinth.com/maven") // Easy NPC (opted out of CurseForge distribution)
}

dependencies {
    minecraft("net.minecraft:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.19.3")
    modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:0.116.12+1.21.1")
    modImplementation(fabricApi.module("fabric-command-api-v2", "0.116.12+1.21.1"))
    modImplementation(fabricApi.module("fabric-lifecycle-events-v1", "0.116.12+1.21.1"))
    modImplementation(fabricApi.module("fabric-resource-loader-v0", "0.116.12+1.21.1"))
    modImplementation(fabricApi.module("fabric-events-interaction-v0", "0.116.12+1.21.1"))
    modImplementation("com.cobblemon:mod:1.7.3+1.21.1") { isTransitive = false }
    modImplementation("com.cobblemon:fabric:1.7.3+1.21.1")
    implementation("com.google.code.gson:gson:2.10.1")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
    modImplementation("curse.maven:radical-cobblemon-trainers-api-1152792:7952421")
    modApi("me.shedaniel.cloth:cloth-config-fabric:15.0.140") {
        exclude(group = "net.fabricmc.fabric-api")
    }
    modApi("com.terraformersmc:modmenu:11.0.3")

    // ── Dev-only runtime companions for `run-client` ────────────────────────────
    // NOT compile deps — they complete the modpack stack when launching from source.
    // They use Loom 1.14, which is why the loom plugin above was bumped 1.11 ->
    // 1.14.476 (older Loom refuses to remap a mod built by a newer Loom). The
    // installed .mrpack pulls all of these from Modrinth; here we mirror the ones our
    // features touch PLUS the transitive deps the dev runtime does not auto-resolve
    // (easy_npc core + config_ui, architectury, Common Network, Forge Config API
    // Port). Easy NPC version 6.25.0 collides with a Forge/1.20.1 build, so the
    // Modrinth deps are pinned by VERSION ID, not version number.
    // Fabric Loader was bumped 0.17.2 -> 0.19.3 above: Easy NPC 6.25.0 requires >= 0.18.3.
    modRuntimeOnly("maven.modrinth:Epm6R3P2:pxt6JAIU")          // easy_npc 6.25.0 (fabric 1.21.1) — NPC entities for Sight / Map
    modRuntimeOnly("maven.modrinth:uTGjf7vA:GemJghTO")          // easy_npc_config_ui 6.25.0 (fabric 1.21.1)
    modRuntimeOnly("maven.modrinth:lhGA9TYQ:Wto0RchG")          // architectury 13.0.8 — rctapi requires >= 13.0.2
    modRuntimeOnly("maven.modrinth:HIuqnQpi:qJXqSlRN")          // Common Network 1.0.21-1.21.1 — JourneyMap dependency
    modRuntimeOnly("maven.modrinth:ohNO6lps:N5qzq0XV")          // Forge Config API Port 21.1.6 — MapFrontiers dependency
    modRuntimeOnly("curse.maven:cobbledollars-859232:6604561")  // CobbleDollars-fabric-2.0.0+Beta-5.1+1.21.1.jar
    modRuntimeOnly("curse.maven:journeymap-32274:8325589")      // journeymap-fabric-1.21.1-6.0.0.jar
    modRuntimeOnly("curse.maven:mapfrontiers-366783:7099826")   // MapFrontiers-1.21.1-2.7.0-beta.18-fabric.jar (matches the 2.7.0-beta.18 integration target)
    // Then `run-client` (run dir = ./run); copy/symlink your UPM 2 world into ./run/saves/.

    // Map Frontiers integration needs no compile dependency: its plugin API does not exist
    // on the 1.21.1 line (Cobblemon is locked to 1.21.1), so MapFrontiersIntegration reaches
    // the mod's internal FrontiersManager reflectively. See that class for the rationale.
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
