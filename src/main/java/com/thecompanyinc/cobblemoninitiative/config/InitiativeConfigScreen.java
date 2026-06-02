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

    builder.setSavingRunnable(() -> {
      config.save();
      sightConfig.save();
      NuzlockeInit.reloadConfig();
      NpcSightInit.reloadConfig();
    });

    return builder.build();
  }
}
