package com.thecompanyinc.cobblemoninitiative.config;

import com.thecompanyinc.cobblemoninitiative.NuzlockeInit;
import com.thecompanyinc.cobblemoninitiative.npcsight.NpcSightConfig;
import com.thecompanyinc.cobblemoninitiative.npcsight.NpcSightInit;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class InitiativeConfigScreen {

  public static Screen create(Screen parent) {
    NuzlockeConfig config = NuzlockeConfig.load();
    NuzlockeConfig defaults = new NuzlockeConfig();

    ConfigBuilder builder = ConfigBuilder.create()
      .setParentScreen(parent)
      .setTitle(Component.literal("The Cobblemon Initiative"));

    ConfigEntryBuilder entryBuilder = builder.entryBuilder();

    // -------------------------------------------------------------------------
    // General Settings
    // -------------------------------------------------------------------------
    ConfigCategory general = builder.getOrCreateCategory(
      Component.literal("General Settings")
    );

    general.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Scale Damage by Party Size"),
          config.isScaleDamageByPartySize()
        )
        .setDefaultValue(defaults.isScaleDamageByPartySize())
        .setTooltip(Component.literal("If enabled, damage = maxHealth / partySize."))
        .setSaveConsumer(config::setScaleDamageByPartySize)
        .build()
    );

    general.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Use Max Health"),
          config.isUseMaxHealth()
        )
        .setDefaultValue(defaults.isUseMaxHealth())
        .setTooltip(
          Component.literal("Use max health instead of current health for damage calculation.")
        )
        .setSaveConsumer(config::setUseMaxHealth)
        .build()
    );

    general.addEntry(
      entryBuilder
        .startFloatField(
          Component.literal("Minimum Damage Percent"),
          config.getMinimumDamagePercent()
        )
        .setDefaultValue(defaults.getMinimumDamagePercent())
        .setMin(0.0f)
        .setMax(1.0f)
        .setTooltip(
          Component.literal("Minimum damage as a percentage of max health (0.0 - 1.0).")
        )
        .setSaveConsumer(config::setMinimumDamagePercent)
        .build()
    );

    general.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Enable Safe Zones"),
          config.isEnableSafeZones()
        )
        .setDefaultValue(defaults.isEnableSafeZones())
        .setTooltip(
          Component.literal(
            "Enable protection zones where mob spawning is restricted. "
            + "Use /safezone add to create zones."
          )
        )
        .setSaveConsumer(config::setEnableSafeZones)
        .build()
    );

    // -------------------------------------------------------------------------
    // Nuzlocke Rules
    // -------------------------------------------------------------------------
    ConfigCategory nuzlocke = builder.getOrCreateCategory(
      Component.literal("Nuzlocke Rules")
    );

    nuzlocke.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Remove Fainted Pokémon"),
          config.isRemoveFaintedPokemon()
        )
        .setDefaultValue(defaults.isRemoveFaintedPokemon())
        .setTooltip(
          Component.literal("Permanently remove Pokémon from party when they faint.")
        )
        .setSaveConsumer(config::setRemoveFaintedPokemon)
        .build()
    );

    nuzlocke.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Sacrifice on Flee"),
          config.isSacrificeOnFlee()
        )
        .setDefaultValue(defaults.isSacrificeOnFlee())
        .setTooltip(
          Component.literal("Must sacrifice a Pokémon when fleeing from battle.")
        )
        .setSaveConsumer(config::setSacrificeOnFlee)
        .build()
    );

    nuzlocke.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Mystery Sacrifice"),
          config.isMysterySacrifice()
        )
        .setDefaultValue(defaults.isMysterySacrifice())
        .setTooltip(
          Component.literal(
            "Obfuscate Pokémon names when choosing a sacrifice (like enchantment text)."
          )
        )
        .setSaveConsumer(config::setMysterySacrifice)
        .build()
    );

    // -------------------------------------------------------------------------
    // Capture Rules
    // -------------------------------------------------------------------------
    ConfigCategory capture = builder.getOrCreateCategory(
      Component.literal("Capture Rules")
    );

    capture.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Send Caught to PC"),
          config.isSendCaughtToPC()
        )
        .setDefaultValue(defaults.isSendCaughtToPC())
        .setTooltip(
          Component.literal("Send caught Pokémon directly to PC instead of party.")
        )
        .setSaveConsumer(config::setSendCaughtToPC)
        .build()
    );

    capture.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Caught Pokémon Start Fainted"),
          config.isSetCaughtToZeroHP()
        )
        .setDefaultValue(defaults.isSetCaughtToZeroHP())
        .setTooltip(Component.literal("Set caught Pokémon to 0 HP (fainted)."))
        .setSaveConsumer(config::setSetCaughtToZeroHP)
        .build()
    );

    capture.addEntry(
      entryBuilder
        .startEnumSelector(
          Component.literal("Duplicate Handling"),
          NuzlockeConfig.DuplicateHandling.class,
          config.getDuplicateHandling()
        )
        .setDefaultValue(defaults.getDuplicateHandling())
        .setTooltip(
          Component.literal(
            "OFF: Keep all\n"
            + "RELEASE_IF_OWNED: Release if species in party or PC\n"
            + "RELEASE_IF_EVER_CAUGHT: Release if ever caught before"
          )
        )
        .setSaveConsumer(config::setDuplicateHandling)
        .build()
    );

    // -------------------------------------------------------------------------
    // Battle Types
    // -------------------------------------------------------------------------
    ConfigCategory battleTypes = builder.getOrCreateCategory(
      Component.literal("Battle Types")
    );

    battleTypes.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Apply in Wild Battles"),
          config.isApplyInWildBattles()
        )
        .setDefaultValue(defaults.isApplyInWildBattles())
        .setTooltip(
          Component.literal("Take damage when Pokémon faint in wild battles.")
        )
        .setSaveConsumer(config::setApplyInWildBattles)
        .build()
    );

    battleTypes.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Apply in Trainer Battles"),
          config.isApplyInTrainerBattles()
        )
        .setDefaultValue(defaults.isApplyInTrainerBattles())
        .setTooltip(
          Component.literal("Take damage when Pokémon faint in NPC trainer battles.")
        )
        .setSaveConsumer(config::setApplyInTrainerBattles)
        .build()
    );

    // -------------------------------------------------------------------------
    // Messages
    // -------------------------------------------------------------------------
    ConfigCategory messages = builder.getOrCreateCategory(
      Component.literal("Messages")
    );

    messages.addEntry(
      entryBuilder
        .startStrField(
          Component.literal("Damage Message"),
          config.getDamageMessage()
        )
        .setDefaultValue(defaults.getDamageMessage())
        .setTooltip(
          Component.literal(
            "Message shown when taking damage. Use %pokemon% for the Pokémon name."
          )
        )
        .setSaveConsumer(config::setDamageMessage)
        .build()
    );

    // -------------------------------------------------------------------------
    // Area Announcements
    // -------------------------------------------------------------------------
    ConfigCategory announce = builder.getOrCreateCategory(
      Component.literal("Area Announcements")
    );

    announce.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Enable Area Announcements"),
          config.isEnableAreaAnnouncements()
        )
        .setDefaultValue(defaults.isEnableAreaAnnouncements())
        .setTooltip(
          Component.literal(
            "Show a notification when entering a named safe zone with 'announce' enabled. "
            + "Zones are configured via /safezone add or /cobblemon-initiative install run."
          )
        )
        .setSaveConsumer(config::setEnableAreaAnnouncements)
        .build()
    );

    announce.addEntry(
      entryBuilder
        .startEnumSelector(
          Component.literal("Announcement Style"),
          NuzlockeConfig.AnnouncementStyle.class,
          config.getAnnouncementStyle()
        )
        .setDefaultValue(defaults.getAnnouncementStyle())
        .setTooltip(
          Component.literal(
            "TITLE: Large screen title with optional subtitle\n"
            + "ACTIONBAR: Small text above the hotbar\n"
            + "CHAT: Message in the chat log"
          )
        )
        .setSaveConsumer(config::setAnnouncementStyle)
        .build()
    );

    announce.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Announce on Exit"),
          config.isAnnounceOnExit()
        )
        .setDefaultValue(defaults.isAnnounceOnExit())
        .setTooltip(Component.literal("Also announce when leaving a zone (ACTIONBAR and CHAT only)."))
        .setSaveConsumer(config::setAnnounceOnExit)
        .build()
    );

    announce.addEntry(
      entryBuilder
        .startIntSlider(
          Component.literal("Title Fade In (ticks)"),
          config.getAnnouncementFadeIn(),
          5,
          40
        )
        .setDefaultValue(defaults.getAnnouncementFadeIn())
        .setTooltip(Component.literal("How long the title fades in (TITLE style only). 20 ticks = 1 second."))
        .setSaveConsumer(config::setAnnouncementFadeIn)
        .build()
    );

    announce.addEntry(
      entryBuilder
        .startIntSlider(
          Component.literal("Title Stay (ticks)"),
          config.getAnnouncementStay(),
          20,
          200
        )
        .setDefaultValue(defaults.getAnnouncementStay())
        .setTooltip(Component.literal("How long the title stays on screen (TITLE style only)."))
        .setSaveConsumer(config::setAnnouncementStay)
        .build()
    );

    announce.addEntry(
      entryBuilder
        .startIntSlider(
          Component.literal("Title Fade Out (ticks)"),
          config.getAnnouncementFadeOut(),
          5,
          40
        )
        .setDefaultValue(defaults.getAnnouncementFadeOut())
        .setTooltip(Component.literal("How long the title fades out (TITLE style only)."))
        .setSaveConsumer(config::setAnnouncementFadeOut)
        .build()
    );

    // -------------------------------------------------------------------------
    // NPC Sight
    // -------------------------------------------------------------------------
    NpcSightConfig sightConfig = NpcSightConfig.load();
    NpcSightConfig sightDefaults = new NpcSightConfig();

    ConfigCategory npcSight = builder.getOrCreateCategory(
      Component.literal("NPC Sight")
    );

    npcSight.addEntry(
      entryBuilder
        .startIntSlider(
          Component.literal("Default Sight Range"),
          sightConfig.getDefaultSightRange(),
          1,
          256
        )
        .setDefaultValue(sightDefaults.getDefaultSightRange())
        .setTooltip(
          Component.literal(
            "Default sight range (blocks) for NPCs without a per-entity override."
          )
        )
        .setSaveConsumer(sightConfig::setDefaultSightRange)
        .build()
    );

    npcSight.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Debug Mode"),
          sightConfig.isDebugMode()
        )
        .setDefaultValue(sightDefaults.isDebugMode())
        .setTooltip(
          Component.literal(
            "Show colored dust particles along NPC raycasts (green = clear, red = blocked)."
          )
        )
        .setSaveConsumer(sightConfig::setDebugMode)
        .build()
    );

    npcSight.addEntry(
      entryBuilder
        .startStrField(
          Component.literal("Default Dialog Name"),
          sightConfig.getDefaultDialogName()
        )
        .setDefaultValue(sightDefaults.getDefaultDialogName())
        .setTooltip(
          Component.literal(
            "Easy NPC dialog opened when an NPC sees a player and has no per-entity dialog set. "
            + "Leave blank to disable the fallback."
          )
        )
        .setSaveConsumer(sightConfig::setDefaultDialogName)
        .build()
    );

    npcSight.addEntry(
      entryBuilder
        .startIntSlider(
          Component.literal("Dialog Trigger Range"),
          (int) sightConfig.getDialogRange(),
          1,
          20
        )
        .setDefaultValue((int) sightDefaults.getDialogRange())
        .setTooltip(
          Component.literal(
            "Player must be within this many blocks for the NPC dialog to open (when the NPC can see them)."
          )
        )
        .setSaveConsumer(v -> sightConfig.setDialogRange(v.doubleValue()))
        .build()
    );

    // -------------------------------------------------------------------------
    // Ice Trial (shrine floor hazard)
    // -------------------------------------------------------------------------
    ShrineConfig shrineConfig = ShrineConfig.load();
    ShrineConfig shrineDefaults = new ShrineConfig();

    ConfigCategory iceTrial = builder.getOrCreateCategory(
      Component.literal("Ice Trial")
    );

    iceTrial.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Enable Ice Floor Hazard"),
          shrineConfig.isIceFloorEnabled()
        )
        .setDefaultValue(shrineDefaults.isIceFloorEnabled())
        .setTooltip(
          Component.literal(
            "Master switch for the ice trial's floor hazard. When off, touching ice "
            + "off the safe path no longer hurts or teleports you (the timed parkour "
            + "still runs). Useful for accessibility or testing the layout."
          )
        )
        .setSaveConsumer(shrineConfig::setIceFloorEnabled)
        .build()
    );

    iceTrial.addEntry(
      entryBuilder
        .startFloatField(
          Component.literal("Ice Floor Damage"),
          shrineConfig.getIceFloorDamage()
        )
        .setDefaultValue(shrineDefaults.getIceFloorDamage())
        .setMin(0.0f)
        .setMax(40.0f)
        .setTooltip(
          Component.literal(
            "Damage taken when stepping on the wrong ice (2 = 1 heart). "
            + "Lower this if the trial feels too lethal for a hardcore run; 0 = no damage "
            + "(still teleports you back to the start)."
          )
        )
        .setSaveConsumer(shrineConfig::setIceFloorDamage)
        .build()
    );

    iceTrial.addEntry(
      entryBuilder
        .startIntSlider(
          Component.literal("Ice Hit Cooldown (ticks)"),
          shrineConfig.getIceFloorHitCooldownTicks(),
          0,
          100
        )
        .setDefaultValue(shrineDefaults.getIceFloorHitCooldownTicks())
        .setTooltip(
          Component.literal(
            "Immunity window after a hit so a single misstep only fires once. "
            + "20 ticks = 1 second."
          )
        )
        .setSaveConsumer(shrineConfig::setIceFloorHitCooldownTicks)
        .build()
    );

    // -------------------------------------------------------------------------
    // Unplaced Chests (loot dispensers)
    // -------------------------------------------------------------------------
    LootChestConfig lootChestConfig = LootChestConfig.load();
    LootChestConfig lootChestDefaults = new LootChestConfig();

    ConfigCategory lootChests = builder.getOrCreateCategory(
      Component.literal("Unplaced Chests")
    );

    lootChests.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Enable Unplaced-Chest Loot"),
          lootChestConfig.isEnabled()
        )
        .setDefaultValue(lootChestDefaults.isEnabled())
        .setTooltip(
          Component.literal(
            "When on, opening a chest you did NOT place yourself (map / structure "
            + "chests) stocks it with random items scaled to your badge count, then "
            + "opens it. Chests you place by hand open normally."
          )
        )
        .setSaveConsumer(lootChestConfig::setEnabled)
        .build()
    );

    lootChests.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Give Minecraft Pool"),
          lootChestConfig.isGiveMinecraftPool()
        )
        .setDefaultValue(lootChestDefaults.isGiveMinecraftPool())
        .setTooltip(
          Component.literal("Include the Minecraft-resource loot pool (metals / naturals / minerals).")
        )
        .setSaveConsumer(lootChestConfig::setGiveMinecraftPool)
        .build()
    );

    lootChests.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Give Cobblemon Pool"),
          lootChestConfig.isGiveCobblemonPool()
        )
        .setDefaultValue(lootChestDefaults.isGiveCobblemonPool())
        .setTooltip(
          Component.literal("Include the Cobblemon-resource loot pool (apricorns / evo & held items / healing).")
        )
        .setSaveConsumer(lootChestConfig::setGiveCobblemonPool)
        .build()
    );

    lootChests.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("One-Time Per Chest"),
          lootChestConfig.isOneTimePerChest()
        )
        .setDefaultValue(lootChestDefaults.isOneTimePerChest())
        .setTooltip(
          Component.literal(
            "Each unplaced chest dispenses loot only once, then behaves as a normal "
            + "chest. Turn off to re-roll on every open (loot-table testing)."
          )
        )
        .setSaveConsumer(lootChestConfig::setOneTimePerChest)
        .build()
    );

    lootChests.addEntry(
      entryBuilder
        .startIntSlider(
          Component.literal("Empty Chest Chance"),
          (int) Math.round(lootChestConfig.getEmptyChestChance() * 100),
          0,
          100
        )
        .setDefaultValue((int) Math.round(lootChestDefaults.getEmptyChestChance() * 100))
        .setTextGetter(v -> Component.literal(v + "%"))
        .setTooltip(
          Component.literal(
            "Chance an unplaced chest rolls EMPTY on first open (still claimed, never "
            + "re-rolls). High by default — finding a stocked cache should feel like luck."
          )
        )
        .setSaveConsumer(v -> lootChestConfig.setEmptyChestChance(v / 100.0))
        .build()
    );

    lootChests.addEntry(
      entryBuilder
        .startBooleanToggle(
          Component.literal("Announce Supply Caches"),
          lootChestConfig.isAnnounceUnplacedChests()
        )
        .setDefaultValue(lootChestDefaults.isAnnounceUnplacedChests())
        .setTooltip(
          Component.literal(
            "Send the \"[Supply Cache]\" chat line when an unplaced chest is stocked. "
            + "Off by default to keep stream chat clean."
          )
        )
        .setSaveConsumer(lootChestConfig::setAnnounceUnplacedChests)
        .build()
    );

    lootChests.addEntry(
      entryBuilder
        .startIntSlider(
          Component.literal("Loot Stacks (×)"),
          (int) Math.round(lootChestConfig.getStackMultiplier() * 10),
          0,
          30
        )
        .setDefaultValue((int) Math.round(lootChestDefaults.getStackMultiplier() * 10))
        .setTextGetter(v -> Component.literal(String.format("%.1f×", v / 10.0)))
        .setTooltip(
          Component.literal(
            "Scales the NUMBER of item stacks an unplaced chest is stocked with. "
            + "1.0× = the loot tables' default, 0.5× = half, 0× = none, 3.0× = triple."
          )
        )
        .setSaveConsumer(v -> lootChestConfig.setStackMultiplier(v / 10.0))
        .build()
    );

    lootChests.addEntry(
      entryBuilder
        .startIntSlider(
          Component.literal("Items Per Stack (×)"),
          (int) Math.round(lootChestConfig.getItemMultiplier() * 10),
          0,
          30
        )
        .setDefaultValue((int) Math.round(lootChestDefaults.getItemMultiplier() * 10))
        .setTextGetter(v -> Component.literal(String.format("%.1f×", v / 10.0)))
        .setTooltip(
          Component.literal(
            "Scales the number of ITEMS within each stack. 1.0× = the loot tables' "
            + "default counts, 0.5× = half. Capped at each item's max stack size."
          )
        )
        .setSaveConsumer(v -> lootChestConfig.setItemMultiplier(v / 10.0))
        .build()
    );

    // -------------------------------------------------------------------------
    // Dark Urge (whisper-on-faint flavour)
    // -------------------------------------------------------------------------
    ConfigCategory darkUrge = builder.getOrCreateCategory(Component.literal("Dark Urge"));

    darkUrge.addEntry(
      entryBuilder
        .startBooleanToggle(Component.literal("Enable Dark Urge Whispers"), config.isEnableDarkUrgeWhispers())
        .setDefaultValue(defaults.isEnableDarkUrgeWhispers())
        .setTooltip(Component.literal("Intrusive shadow-self lines on Pokémon faint. Disable for a lore-free / spoiler-free run."))
        .setSaveConsumer(config::setEnableDarkUrgeWhispers)
        .build()
    );
    darkUrge.addEntry(
      entryBuilder
        .startFloatField(Component.literal("Whisper Chance"), config.getDarkUrgeChance())
        .setDefaultValue(defaults.getDarkUrgeChance())
        .setMin(0.0f).setMax(1.0f)
        .setTooltip(Component.literal("Probability (0.0-1.0) that an eligible faint fires a whisper."))
        .setSaveConsumer(config::setDarkUrgeChance)
        .build()
    );
    darkUrge.addEntry(
      entryBuilder
        .startIntSlider(Component.literal("Whisper Cooldown (ticks)"), config.getDarkUrgeCooldownTicks(), 0, 24000)
        .setDefaultValue(defaults.getDarkUrgeCooldownTicks())
        .setTooltip(Component.literal("Minimum gap between whispers per player. 20 ticks = 1s (6000 = 5 min)."))
        .setSaveConsumer(config::setDarkUrgeCooldownTicks)
        .build()
    );
    darkUrge.addEntry(
      entryBuilder
        .startIntSlider(Component.literal("Tier 1 Level-Cap Threshold"), config.getDarkUrgeTier1LevelCap(), 1, 100)
        .setDefaultValue(defaults.getDarkUrgeTier1LevelCap())
        .setTooltip(Component.literal("Level cap at which whispers escalate to tier 1 (gyms 1-3)."))
        .setSaveConsumer(config::setDarkUrgeTier1LevelCap)
        .build()
    );
    darkUrge.addEntry(
      entryBuilder
        .startIntSlider(Component.literal("Tier 2 Level-Cap Threshold"), config.getDarkUrgeTier2LevelCap(), 1, 100)
        .setDefaultValue(defaults.getDarkUrgeTier2LevelCap())
        .setTooltip(Component.literal("Level cap at which whispers escalate to tier 2 (gyms 4-7)."))
        .setSaveConsumer(config::setDarkUrgeTier2LevelCap)
        .build()
    );
    darkUrge.addEntry(
      entryBuilder
        .startIntSlider(Component.literal("Tier 3 Level-Cap Threshold"), config.getDarkUrgeTier3LevelCap(), 1, 100)
        .setDefaultValue(defaults.getDarkUrgeTier3LevelCap())
        .setTooltip(Component.literal("Level cap at which the founder speaks plainly (tier 3, gym 8+)."))
        .setSaveConsumer(config::setDarkUrgeTier3LevelCap)
        .build()
    );

    // Wilderness announcements (Area Announcements category)
    announce.addEntry(
      entryBuilder
        .startBooleanToggle(Component.literal("Announce Wilderness"), config.isAnnounceWilderness())
        .setDefaultValue(defaults.isAnnounceWilderness())
        .setTooltip(Component.literal("Announce 'the wild' when leaving a named zone into undefined territory."))
        .setSaveConsumer(config::setAnnounceWilderness)
        .build()
    );
    announce.addEntry(
      entryBuilder
        .startStrField(Component.literal("Wilderness Name"), config.getWildernessName())
        .setDefaultValue(defaults.getWildernessName())
        .setTooltip(Component.literal("Display name for undefined territory."))
        .setSaveConsumer(config::setWildernessName)
        .build()
    );
    announce.addEntry(
      entryBuilder
        .startStrField(Component.literal("Wilderness Subtitle"), config.getWildernessSubtitle())
        .setDefaultValue(defaults.getWildernessSubtitle())
        .setTooltip(Component.literal("Optional subtitle beneath the wilderness name (TITLE style only)."))
        .setSaveConsumer(config::setWildernessSubtitle)
        .build()
    );
    announce.addEntry(
      entryBuilder
        .startStrField(Component.literal("Wilderness Color"), config.getWildernessColor())
        .setDefaultValue(defaults.getWildernessColor())
        .setTooltip(Component.literal("Hex color for the wilderness announcement, e.g. #88AA88."))
        .setSaveConsumer(config::setWildernessColor)
        .build()
    );

    // Zone-check cadence (General Settings category)
    general.addEntry(
      entryBuilder
        .startIntSlider(Component.literal("Zone Check Cadence (ticks)"), config.getZoneCheckCadenceTicks(), 1, 100)
        .setDefaultValue(defaults.getZoneCheckCadenceTicks())
        .setTooltip(Component.literal("How often zone-transition announcements are polled. 20 = 1s."))
        .setSaveConsumer(config::setZoneCheckCadenceTicks)
        .build()
    );

    // NPC Sight internals (NPC Sight category)
    npcSight.addEntry(
      entryBuilder
        .startIntSlider(Component.literal("Field of View (degrees)"), sightConfig.getFovDegrees(), 30, 360)
        .setDefaultValue(sightDefaults.getFovDegrees())
        .setTooltip(Component.literal("Vision cone an NPC can see. 120 = default; 360 = omnidirectional."))
        .setSaveConsumer(sightConfig::setFovDegrees)
        .build()
    );
    npcSight.addEntry(
      entryBuilder
        .startIntSlider(Component.literal("Sight Check Interval (ticks)"), sightConfig.getTickInterval(), 1, 40)
        .setDefaultValue(sightDefaults.getTickInterval())
        .setTooltip(Component.literal("Run the full line-of-sight sweep every N ticks. Higher = cheaper, less responsive."))
        .setSaveConsumer(sightConfig::setTickInterval)
        .build()
    );
    npcSight.addEntry(
      entryBuilder
        .startDoubleField(Component.literal("Debug Ray Step"), sightConfig.getDebugRayStep())
        .setDefaultValue(sightDefaults.getDebugRayStep())
        .setMin(0.1).setMax(2.0)
        .setTooltip(Component.literal("Debug mode: block spacing between raycast particles."))
        .setSaveConsumer(sightConfig::setDebugRayStep)
        .build()
    );
    npcSight.addEntry(
      entryBuilder
        .startIntSlider(Component.literal("Debug Ray Max Particles"), sightConfig.getDebugRayMaxSteps(), 32, 2048)
        .setDefaultValue(sightDefaults.getDebugRayMaxSteps())
        .setTooltip(Component.literal("Debug mode: cap on particles drawn per ray."))
        .setSaveConsumer(sightConfig::setDebugRayMaxSteps)
        .build()
    );

    // Ice crack SFX (Ice Trial category)
    iceTrial.addEntry(
      entryBuilder
        .startFloatField(Component.literal("Ice Crack Sound Volume"), shrineConfig.getIceCrackSoundVolume())
        .setDefaultValue(shrineDefaults.getIceCrackSoundVolume())
        .setMin(0.0f).setMax(1.0f)
        .setTooltip(Component.literal("Volume of the glass-break sound on a floor-hazard hit."))
        .setSaveConsumer(shrineConfig::setIceCrackSoundVolume)
        .build()
    );
    iceTrial.addEntry(
      entryBuilder
        .startFloatField(Component.literal("Ice Crack Sound Pitch"), shrineConfig.getIceCrackSoundPitch())
        .setDefaultValue(shrineDefaults.getIceCrackSoundPitch())
        .setMin(0.5f).setMax(2.0f)
        .setTooltip(Component.literal("Pitch of the glass-break sound on a floor-hazard hit."))
        .setSaveConsumer(shrineConfig::setIceCrackSoundPitch)
        .build()
    );

    // -------------------------------------------------------------------------
    // Ground Shrine (The Buried Maze — dark gauntlet)
    // -------------------------------------------------------------------------
    ConfigCategory groundShrine = builder.getOrCreateCategory(Component.literal("Ground Shrine"));

    groundShrine.addEntry(
      entryBuilder
        .startFloatField(Component.literal("Start Health Fraction"), shrineConfig.getDarkGauntletStartHealthFraction())
        .setDefaultValue(shrineDefaults.getDarkGauntletStartHealthFraction())
        .setMin(0.1f).setMax(1.0f)
        .setTooltip(Component.literal("Fraction of max health the player starts the ground shrine at (0.5 = half)."))
        .setSaveConsumer(shrineConfig::setDarkGauntletStartHealthFraction)
        .build()
    );
    groundShrine.addEntry(
      entryBuilder
        .startIntSlider(Component.literal("Blindness Duration (ticks)"), shrineConfig.getDarkGauntletBlindnessDurationTicks(), 20, 400)
        .setDefaultValue(shrineDefaults.getDarkGauntletBlindnessDurationTicks())
        .setTooltip(Component.literal("How long each blindness application lasts. 20 = 1s."))
        .setSaveConsumer(shrineConfig::setDarkGauntletBlindnessDurationTicks)
        .build()
    );
    groundShrine.addEntry(
      entryBuilder
        .startIntSlider(Component.literal("Blindness Refresh (ticks)"), shrineConfig.getDarkGauntletBlindnessRefreshTicks(), 20, 200)
        .setDefaultValue(shrineDefaults.getDarkGauntletBlindnessRefreshTicks())
        .setTooltip(Component.literal("How often blindness is re-applied so it never fades. Keep below the duration."))
        .setSaveConsumer(shrineConfig::setDarkGauntletBlindnessRefreshTicks)
        .build()
    );
    groundShrine.addEntry(
      entryBuilder
        .startIntSlider(Component.literal("Earthquake Nausea (ticks)"), shrineConfig.getEarthquakeNauseaTicks(), 0, 200)
        .setDefaultValue(shrineDefaults.getEarthquakeNauseaTicks())
        .setTooltip(Component.literal("Nausea applied on each earthquake teleport. 0 disables (viewer comfort)."))
        .setSaveConsumer(shrineConfig::setEarthquakeNauseaTicks)
        .build()
    );
    groundShrine.addEntry(
      entryBuilder
        .startFloatField(Component.literal("Earthquake Sound Volume"), shrineConfig.getEarthquakeSoundVolume())
        .setDefaultValue(shrineDefaults.getEarthquakeSoundVolume())
        .setMin(0.0f).setMax(1.0f)
        .setTooltip(Component.literal("Volume of the earthquake rumble."))
        .setSaveConsumer(shrineConfig::setEarthquakeSoundVolume)
        .build()
    );
    groundShrine.addEntry(
      entryBuilder
        .startFloatField(Component.literal("Earthquake Sound Pitch"), shrineConfig.getEarthquakeSoundPitch())
        .setDefaultValue(shrineDefaults.getEarthquakeSoundPitch())
        .setMin(0.5f).setMax(2.0f)
        .setTooltip(Component.literal("Pitch of the earthquake rumble."))
        .setSaveConsumer(shrineConfig::setEarthquakeSoundPitch)
        .build()
    );

    // -------------------------------------------------------------------------
    // Progression
    // -------------------------------------------------------------------------
    ProgressionConfig progressionConfig = ProgressionConfig.load();
    ProgressionConfig progressionDefaults = new ProgressionConfig();
    ConfigCategory progression = builder.getOrCreateCategory(Component.literal("Progression"));

    progression.addEntry(
      entryBuilder
        .startIntSlider(Component.literal("Base Level Cap"), progressionConfig.getBaseLevelCap(), 5, 50)
        .setDefaultValue(progressionDefaults.getBaseLevelCap())
        .setTooltip(Component.literal("Starting level cap before any gym badge is earned."))
        .setSaveConsumer(progressionConfig::setBaseLevelCap)
        .build()
    );
    progression.addEntry(
      entryBuilder
        .startIntSlider(Component.literal("Champion Level Cap"), progressionConfig.getChampionLevelCap(), 50, 100)
        .setDefaultValue(progressionDefaults.getChampionLevelCap())
        .setTooltip(Component.literal("Cap reported once every badge/champion milestone is earned."))
        .setSaveConsumer(progressionConfig::setChampionLevelCap)
        .build()
    );
    progression.addEntry(
      entryBuilder
        .startIntSlider(Component.literal("Spawn-on-Defeat Y Offset"), progressionConfig.getSpawnOnDefeatYOffset(), -5, 5)
        .setDefaultValue(progressionDefaults.getSpawnOnDefeatYOffset())
        .setTooltip(Component.literal("Default vertical offset for a Pokémon spawned when a trainer is defeated."))
        .setSaveConsumer(progressionConfig::setSpawnOnDefeatYOffset)
        .build()
    );

    builder.setSavingRunnable(() -> {
      config.save();
      sightConfig.save();
      shrineConfig.save();
      lootChestConfig.save();
      progressionConfig.save();
      NuzlockeInit.reloadConfig();
      NpcSightInit.reloadConfig();
      ShrineConfig.reload();
      LootChestConfig.reload();
      ProgressionConfig.reload();
    });

    return builder.build();
  }
}
