plugins {
    id("java")
    // Kotlin plugin applied ONLY so Loom engages its Kotlin-@Metadata remapper for mod
    // deps: without it the remapped Cobblemon dev jar keeps INTERMEDIARY names
    // (net.minecraft.class_2960) inside Kotlin @Metadata and kotlin-reflect crashes the
    // 'cobblemon' entrypoint (ClassNotFoundException in SpeciesAdditions.<clinit>).
    // There are NO Kotlin sources; kotlin.stdlib.default.dependency=false in
    // gradle.properties keeps the build pure-Java (no stdlib in any configuration).
    // Version must be >= the deps' metadata version: 2.0.21 failed ("cannot write
    // metadata for future compiler versions ... 2.2.0" — Cobblemon), 2.2.20 failed the
    // same way at 2.4.0 (fabric-language-kotlin bundles Kotlin 2.4.0) — so pin 2.4.0.
    kotlin("jvm") version "2.4.0"
    id("dev.architectury.loom") version "1.14.476"
    id("architectury-plugin") version "3.4-SNAPSHOT"
}

group = "com.thecompanyinc"
version = "0.6.0-alpha.12"

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    silentMojangMappingsLicense()
    runs {
        // Dev-runtime entrypoint ORDER differs from the launcher pack: classpath mods
        // (Cobblemon, via loom) hit their `main` entrypoints before folder mods from
        // run/mods. Almanac (folder mod, pack-parity stack) mixes into
        // ItemStack.setCount and needs its own `main` to have registered its AutoConfig
        // first — Cobblemon's villager-trade registration otherwise trips it
        // ("Config ... AlmanacConfigFabric has not been registered"). In the real pack
        // Almanac-*.jar sorts before Cobblemon-*.jar, so this is dev-only: push
        // cobblemon (and us, keeping cobblemon-before-initiative order) to the end.
        configureEach {
            vmArg("-Dfabric.debug.loadLate=cobblemon,cobblemon-initiative")
        }
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://artefacts.cobblemon.com/releases/")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://cursemaven.com") { content { includeGroup("curse.maven") } } // scope: else its 500s on maven.modrinth paths abort resolution
    maven("https://api.modrinth.com/maven") { content { includeGroup("maven.modrinth") } } // Easy NPC + Carpet (opted out of CurseForge distribution)
    maven("https://maven.blamejared.com") // JourneyMap API
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
    modImplementation(fabricApi.module("fabric-key-binding-api-v1", "0.116.12+1.21.1"))
    // Producer's Tool chat-note capture (devtools/DevWandTool — dev-only user, but the
    // module rides the full fabric-api runtime bundle either way).
    modImplementation(fabricApi.module("fabric-message-api-v1", "0.116.12+1.21.1"))
    // JourneyMap soft dep: compile against the API only (byte-identical to the jar-in-jar
    // the pack's JM build ships); the runtime comes from the modRuntimeOnly line below.
    modCompileOnly("info.journeymap:journeymap-api-fabric:2.0.0-1.21.1")
    // …but in DEV the API must also be a runtime mod: Loom strips the `jars` entry from
    // remapped mods' fabric.mod.json, so JM's nested api jar never loads and JM's main
    // entrypoint dies (NoClassDefFoundError: journeymap/api/v2/common/CommonAPI). The
    // standalone jar's mod id is `journeymap-api-fabric` — distinct from `journeymap` and
    // the (unloaded) nested copy, so the loader sees exactly one provider. Dev-only, never published.
    modLocalRuntime("info.journeymap:journeymap-api-fabric:2.0.0-1.21.1")
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
    modRuntimeOnly("net.fabricmc:fabric-language-kotlin:1.13.12+kotlin.2.4.0") // Cobblemon is Kotlin — needs the Kotlin runtime + language adapter at launch (matches the .mrpack). Without it run-client crashes: NoClassDefFoundError kotlin/jvm/internal/Intrinsics.
    modRuntimeOnly("maven.modrinth:Epm6R3P2:pxt6JAIU")          // easy_npc 6.25.0 (fabric 1.21.1) — NPC entities for Sight / Map
    modRuntimeOnly("maven.modrinth:uTGjf7vA:GemJghTO")          // easy_npc_config_ui 6.25.0 (fabric 1.21.1)
    modRuntimeOnly("maven.modrinth:lhGA9TYQ:Wto0RchG")          // architectury 13.0.8 — rctapi requires >= 13.0.2
    modRuntimeOnly("maven.modrinth:HIuqnQpi:qJXqSlRN")          // Common Network 1.0.21-1.21.1 — JourneyMap dependency
    modRuntimeOnly("maven.modrinth:ohNO6lps:N5qzq0XV")          // Forge Config API Port 21.1.6 — MapFrontiers dependency
    modRuntimeOnly("curse.maven:cobbledollars-859232:6604561")  // CobbleDollars-fabric-2.0.0+Beta-5.1+1.21.1.jar
    modRuntimeOnly("curse.maven:journeymap-32274:8325589")      // journeymap-fabric-1.21.1-6.0.0.jar
    runtimeOnly("com.electronwill.night-config:core:3.8.1")     // JourneyMap/MapFrontiers server entrypoints need night-config >= 3.8 in dev (runServer NoClassDefFoundError otherwise; the launcher pack provides it via jar-in-jar)
    runtimeOnly("com.electronwill.night-config:toml:3.8.1")
    runtimeOnly("org.graalvm.js:js:22.3.0")                     // Cobblemon's Showdown battle engine: the maven dev artifact references plain org.graalvm.polyglot (the release jar relocates+bundles it as com.cobblemon.mod.relocations.graalvm) — without this the Showdown thread dies (NoClassDefFoundError: org/graalvm/polyglot/HostAccess) and boot hangs. Version = Cobblemon 1.7.3's own graal pin (libs.versions.toml @ release commit)
    modRuntimeOnly("curse.maven:mapfrontiers-366783:7099826")   // MapFrontiers-1.21.1-2.7.0-beta.18-fabric.jar (matches the 2.7.0-beta.18 integration target)
    // Carpet (fake players via /player spawn) — TEST-ONLY, never shipped. Lets the headless
    // harness spawn a bot ServerPlayer to trigger latch-NPC spawns, run player-scoped commands,
    // and read LIVE placement positions (config coords are only nominal). See docs/TESTING_TOOLKIT.md.
    modRuntimeOnly("maven.modrinth:carpet:1.4.147")            // fabric-carpet 1.4.147 (1.21.1); Modrinth-maven versions carpet by number, not id
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
